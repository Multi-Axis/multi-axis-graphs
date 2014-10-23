package main

import (
	"bytes"
	"database/sql"
	"encoding/json"
	_ "github.com/lib/pq"
	"html/template"
	"log"
	"net/http"
	"fmt"
	"strconv"
	"strings"
	"io/ioutil"
	"os/exec"
)

//	hopefully makes constant new db connections unnecessary, yay?
var db *sql.DB

/* {{{ Handlers --------------------------------------------------------- */

func itemHandler(w http.ResponseWriter, r *http.Request) {
	parts := strings.Split(r.URL.Path, "/")
	id, _ := strconv.Atoi(parts[len(parts)-1])

	var wantsJson bool
	if len(r.Header["Accept"]) > 0 {
		wantsJson = strings.Contains(r.Header["Accept"][0], "json")
	}
	if r.Method == "POST" {
		r.ParseForm()
		params := r.FormValue("params")
		threshold := r.FormValue("threshold")
		db.Exec(`UPDATE item_future SET params = $1 WHERE id = $2`, params, id)

      // threshold: update, or insert if non exists
      // TODO: client should specify which threshold.id to use (or create new
      // threshold)
		res, _ := db.Exec(`UPDATE threshold SET value = $1 WHERE itemid = $2`, threshold, id)
		affected, _ := res.RowsAffected()
		if affected == 0 {
			db.Exec(`INSERT INTO threshold VALUES (default, $1, true, $2)`, id, threshold)
		}
		updateFuture(id)
	} 
	if wantsJson {
		deliverItemByItemFutureId(w, id)
	} else {
		graphViewHTML(w)
	}
}

// handles graph drawing thingy requests...
func staticHandler(w http.ResponseWriter, r *http.Request) {
	url  := r.URL.Path
	path := strings.TrimPrefix(url, "/")

	if strings.HasSuffix(path, ".js") {
		w.Header().Set("Content-Type", "application/javascript")
	} else if strings.HasSuffix(path, ".css") {
		w.Header().Set("Content-Type", "text/css")
	}

	b, _ := ioutil.ReadFile(path)
	w.Write(b)
}

/* /dashboard */
func dashboardHandler(w http.ResponseWriter, r *http.Request) {
        futureIds := getFutureIds(w)
        for i := range futureIds {
            fmt.Println(futureIds[i])
        }
        t, _ := template.ParseFiles("templates/dashboard.html")
	t.Execute(w, futureIds)
}

/* }}} */

/* {{{ Templates ---------------------------------------------------------------- */
func graphViewHTML(w http.ResponseWriter) {
	t, _ := template.ParseFiles("templates/graphview.html")
	t.Execute(w, "testi")
}

/* }}} */

func updateFuture(id int) {
	fmt.Printf("\nStarting sync...")
	out, err := exec.Command("habbix","sync-db","-i",strconv.Itoa(id)).CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("%s", out)
	fmt.Printf("\nDB Synced.")
}

/* {{{ Querying graph JSON ------------------------------------------------------ */
type ClockValue struct {
	Clock int64   `json:"time"`
	Value float32 `json:"val"`
}

func parseValueJSON(rows *sql.Rows) string {
	defer rows.Close()
	var buffer bytes.Buffer
	var value float32
	var clock int64

	first := true
	buffer.WriteString("[")
	for rows.Next() {
		if err := rows.Scan(&clock, &value); err != nil {
			log.Fatal(err)
		}
		m := ClockValue{clock, value}
		b, err := json.Marshal(m)
		if err != nil {
			panic(err)
		}
		if !first {
			buffer.WriteString(",")
		}
		buffer.Write(b)
		first = false
	}
	buffer.WriteString("]")
	return buffer.String()
}

/* Deliver graph JSON data based on an item_future.id */
func deliverItemByItemFutureId(w http.ResponseWriter, ifId int) {
	var itemId int
	var host string
	var params string
	var details string
	var metric string
	var threshold float32
	var lower bool

	db.QueryRow(`SELECT item_future.itemid, items.name, host, params, details
	FROM item_future
	LEFT JOIN items on items.itemid = item_future.itemid
	LEFT JOIN hosts on hosts.hostid = items.hostid
	WHERE item_future.id = $1`,
	ifId).Scan(&itemId, &metric, &host, &params, &details)

	db.QueryRow(`SELECT value, lower FROM threshold WHERE itemid = $1`, ifId).Scan(&threshold, &lower)

	rows, _ := db.Query(`SELECT * FROM
	(SELECT DISTINCT ON (clock / 10800) clock, value FROM history WHERE itemid = $1) q
	ORDER BY clock`, itemId)
	history := parseValueJSON(rows)

	rows, _ = db.Query(`SELECT clock, value FROM future WHERE itemid = $1 ORDER BY clock`, ifId)
	future := parseValueJSON(rows)

	output := fmt.Sprintf(
		`{ "host":"%s", "params":%s, "metric":"%s", "details":%s, "threshold":%s, "history":%s, "future":%s }`,
		host, params, metric, details,
		fmt.Sprintf(`{ "value":%f }`, threshold),
		history, future)

	w.Write([]byte(output))
}


func getFutureIds(w http.ResponseWriter) []int {
    rows, err := db.Query("select id FROM item_future")
    if err != nil {
            log.Fatal(err)
    }
    defer rows.Close()

    var results []int
    
    for rows.Next() {
        var id int
        if err := rows.Scan(&id); err != nil {
                log.Fatal(err)
        }
        results = append(results, id)
    }
    if err := rows.Err(); err != nil {
            log.Fatal(err)
    }
    
    return results

}

/*}}}*/

/* {{{ main routing etc. -------------------------------------------- */
func main() {
	var err error
	db, err = sql.Open("postgres", "user=ohtu dbname=multi-axis sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}
	http.HandleFunc("/static/", staticHandler)
	http.HandleFunc("/", dashboardHandler)
	http.HandleFunc("/item/", itemHandler) // Unintuitively, this is the default handler!(?)
	http.ListenAndServe(":8080", nil)
}

/* }}} */
