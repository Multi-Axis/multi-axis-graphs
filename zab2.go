package main

import (
	_ "github.com/lib/pq"
	"database/sql"
	"log"
	"net/http"
	"html/template"
//	"bufio"
	"bytes"
//	"os"
	"encoding/json"
	"fmt"
	"strings"
	"strconv"
)

// non-empty ids: 23672, 23690, 23685, 23715, 23710

var db sql.DB 
var id int
var id2 string

func handler1(w http.ResponseWriter, r *http.Request) {
    url := r.URL.Path
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
	t, _ := template.ParseFiles("zab.html")
	t.Execute(w, "testi")
}

func handler(w http.ResponseWriter, r *http.Request) {
	var check bool
	fmt.Printf("%v", len(r.Header["Accept"]))
	if (len(r.Header["Accept"]) > 0) {
		check = strings.Contains(r.Header["Accept"][0],"json")
		fmt.Printf("blah.")
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

func dhandler(w http.ResponseWriter, r *http.Request) {
	query := "SELECT clock, value FROM history where history.itemid IN (SELECT itemid FROM items WHERE hostid = $1 and key_ = $2) ORDER BY clock"
	
	history := dbquery(id,id2,query)
	
	query = "SELECT clock, value FROM item_future AS i RIGHT JOIN future AS f ON f.itemid = i.id WHERE i.itemid IN (SELECT itemid FROM items WHERE hostid = $1 and key_ = $2) ORDER BY clock"
	
	future := dbquery(id,id2,query)
	
	data := "{history:"+history+",future:"+future+"}"
	
	t := template.New("test")
	t, _ = t.Parse(data)
	t.Execute(w, "testi")
}

func dhandler1(w http.ResponseWriter, r *http.Request) {
	url := r.URL.Path
    if (strings.Contains(url, ":")) {
        fmt.Printf("\nurl=%v",url)
        ids := strings.Split(url, ":")
        fmt.Printf("\nid=%v",ids[1])
        fmt.Printf("\nid2=%v",ids[2])
        id, err := strconv.Atoi(ids[1])
        if (err == nil) {
        	q := dbquery(id,ids[2],url)
        	t := template.New("test")
   			t, _ = t.Parse(q)
   			t.Execute(w, "testi")
        }
    } 
}

func ghandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("graph.js")
    t.Execute(w, "testi")
}

func bhandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("dash.html")
    t.Execute(w, "testi")
}

func testHandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("test.html")
    t.Execute(w, "testi")
}

type TestZab struct {
	Clock int64		`json:"time"`
	Value float32	`json:"val"`
}

func dbquery (id int, id2 string, query string) string {
	db, err := sql.Open("postgres", "user=ohtu dbname=multi-axis sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}

	rows, err := db.Query(query, id, id2)
	if err != nil {
		log.Fatal(err)
    }
//	ensure rows are closed
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
          	m := TestZab{clock,value}
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


func main() {
	http.HandleFunc("/metric/", dhandler)	
//	http.HandleFunc("/metric/graph.js", ghandler)		
	http.HandleFunc("/graph.js", ghandler)		
//	http.HandleFunc("/metric/", handler)
	http.HandleFunc("/", handler)
	http.HandleFunc("/test", testHandler)
	http.HandleFunc("/dashboard", bhandler)
	http.ListenAndServe(":8080", nil)
}
