package main

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	_ "github.com/lib/pq"
	"html/template"
	"log"
	"net/http"
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

	//	fmt.Printf("\nurl=%v",r.URL.Path)
	//	fmt.Printf("\nid=%v",id)

	var wantsJson bool
	if len(r.Header["Accept"]) > 0 {
		wantsJson = strings.Contains(r.Header["Accept"][0], "json")
	}
	if r.Method == "POST" {
		r.ParseForm()
		params := r.FormValue("params")
		threshold := r.FormValue("threshold")
		//		fmt.Printf(params)
		db.Exec(`UPDATE item_future SET params = $1 WHERE id = $2`, params, id)
		db.Exec(`UPDATE threshold SET value = $1 WHERE itemid = $2`, threshold, id)
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
	t, _ := template.ParseFiles("templates/dashboard.html")
	t.Execute(w, "testi")
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
	err := exec.Command("habbix","sync-db","-i",strconv.Itoa(id)).Run()
	if err != nil {
		log.Fatal(err)
	}
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
	var params string
	var threshold float32
	
	db.QueryRow(`SELECT itemid, params FROM item_future WHERE id = $1`, ifId).Scan(&itemId, &params)
	
	db.QueryRow(`SELECT value FROM threshold WHERE itemid = $1`, ifId).Scan(&threshold)

//	fmt.Printf("%f",threshold)
	
	rows, _ := db.Query(`SELECT lower, value FROM threshold WHERE itemid = $1`, ifId);
	defer rows.Close() // thresholds := TODO parse to list?

	rows, _ = db.Query(`SELECT * FROM (SELECT DISTINCT ON (clock / 10800) clock, value FROM history WHERE itemid = $1) q ORDER BY clock`, itemId)
	history := parseValueJSON(rows)

	rows, _ = db.Query(`SELECT clock, value FROM future WHERE itemid = $1 ORDER BY clock`, ifId)
	future := parseValueJSON(rows)

	output := fmt.Sprintf(`{ "params": %s, "threshold": %s, "history": %s, "future": %s }`,
		params, fmt.Sprintf("%f",threshold), history, future)
	w.Write([]byte(output))
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
