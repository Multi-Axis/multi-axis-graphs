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
		//		fmt.Printf(params)
		db.Exec(`UPDATE item_future SET params = $1 WHERE id = $2`, params, id)
		graphViewHTML(w) // TODO output json if wantsJson
	} else if wantsJson {
		deliverItemByItemFutureId(w, id)
	} else {
		graphViewHTML(w)
	}
}

// handles graph drawing thingy requests...
func jsHandler(w http.ResponseWriter, r *http.Request) {
	url := r.URL.Path
	var id string

	id = strings.TrimPrefix(url, "/")

	//	fmt.Printf("\nurl=%v",url)
	//	fmt.Printf("\nid=%v",id)
	t, _ := template.ParseFiles(id)
	t.Execute(w, "testi")
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

/* Query graph data based on (items.)hostid, (items.)key_ and a query */
func dbquery(id int, id2 string, query string) string {
	rows, err := db.Query(query, id, id2)
	if err != nil {
		log.Fatal(err)
	}
	return parseValueJSON(rows)
}

/* Deliver graph JSON data based on an item_future.id */
func deliverItemByItemFutureId(w http.ResponseWriter, ifId int) {
	var itemId int
	var params string

	db.QueryRow(`SELECT itemid, params FROM item_future WHERE id = $1`, ifId).Scan(&itemId, &params)

	rows, _ := db.Query(`SELECT clock, value FROM history WHERE history.itemid = $1 ORDER BY clock`, itemId)
	history := parseValueJSON(rows)

	rows, _ = db.Query(`SELECT clock, value FROM future WHERE itemid = $1 ORDER BY clock`, ifId)
	future := parseValueJSON(rows)

	output := "{params:" + params + ", history:" + history + ", future:" + future + "}"
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
	http.HandleFunc("/static/", jsHandler)
	http.HandleFunc("/", dashboardHandler)
	http.HandleFunc("/item/", itemHandler) // Unintuitively, this is the default handler!(?)
	http.ListenAndServe(":8080", nil)
}

/* }}} */
