package main

import (
	_ "github.com/lib/pq"
	"database/sql"
	"log"
	"net/http"
	"html/template"
	"bytes"
	"encoding/json"
	"fmt"
	"strings"
	"strconv"
)

// non-empty ids: 23672, 23690, 23685, 23715, 23710

//	hopefully makes constant new db connections unnecessary, yay?
var db *sql.DB 

// handles single server and json requests
// long and ugly, needs some serious refactoring at some point
func handler(w http.ResponseWriter, r *http.Request) {
	var check bool
	if (len(r.Header["Accept"]) > 0) {
		check = strings.Contains(r.Header["Accept"][0],"json")
	}
	if (check) {
	    url := r.URL.Path
    	var id int
		var id2 string
    	if (strings.Contains(url, ":")) {
    		fmt.Printf("\nurl=%v",url)
			ids := strings.Split(url, ":")
    	  	idT, err := strconv.Atoi(ids[1])
    	  	if (err == nil) {
    	    	id = idT
    	    } else {
    	    	id = 0
    	    }
    	  	if (len(ids) > 2) {
				id2 = ids[2]
			} else {
				id2 = ""
			}
		fmt.Printf("\nid=%v",id)
		fmt.Printf("\nid2=%v",id2)
   		} else {
    		id = 0
    		id2 = ""
    	}
    	query := "SELECT clock, value FROM history where history.itemid IN (SELECT itemid FROM items WHERE hostid = $1 and key_ = $2) ORDER BY clock"
	
		history := dbquery(id,id2,query)
	
		query = "SELECT clock, value FROM item_future AS i RIGHT JOIN future AS f ON f.itemid = i.id WHERE i.itemid IN (SELECT itemid FROM items WHERE hostid = $1 and key_ = $2) ORDER BY clock"
	
		future := dbquery(id,id2,query)
	
		data := "{history:"+history+",future:"+future+"}"
	
		t := template.New("test")
		t, _ = t.Parse(data)
		t.Execute(w, "testi") } else {
		t, _ := template.ParseFiles("zab.html")
		t.Execute(w, "testi")
	}
}

// handles graph drawing thingy requests...
func jsHandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("graph.js")
    t.Execute(w, "testi")
}

// temp test thingy
func dashBoardHandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("dash.html")
    t.Execute(w, "testi")
}

// temp test testing thingy (...)
func testHandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("test.html")
    t.Execute(w, "testi")
}

// used by dbquery
type ClockValue struct {
	Clock int64		`json:"time"`
	Value float32	`json:"val"`
}


// db query yo
func dbquery(id int, id2 string, query string) string {
	rows, err := db.Query(query, id, id2)
	if err != nil {
		log.Fatal(err)
    }
	defer rows.Close()
	
	var buffer bytes.Buffer 
    var value float32
	var clock int64
    first := true;
    buffer.WriteString("[")
	for rows.Next() {
		if err := rows.Scan(&clock,&value); err != nil {
                    log.Fatal(err)
          	}
          	m := ClockValue{clock,value}
          	b, err := json.Marshal(m)
       		if err != nil { panic(err) }
       		if (!first) {
	          	buffer.WriteString(",")
       		}
        	buffer.Write(b) 
        	first = false;  	
    	}
	buffer.WriteString("]")
	return buffer.String()
}

// go, go server, go, go! 
func main() {
	var err error
	db, err = sql.Open("postgres", "user=ohtu dbname=multi-axis sslmode=disable")
	if err != nil { 
		log.Fatal(err)
		}
	http.HandleFunc("/graph.js", jsHandler)		
	http.HandleFunc("/", handler)
	http.HandleFunc("/test", testHandler)
	http.HandleFunc("/dashboard", dashBoardHandler)
	http.ListenAndServe(":8080", nil)
}
