package main

import (
	_ "github.com/lib/pq"
	"database/sql"
	"log"
	"net/http"
	"html/template"
	"bufio"
	"bytes"
	"os"
	"encoding/json"
	"fmt"
	"strings"
	"strconv"
)

// non-empty ids: 23672, 23690, 23685, 23715, 23710

var db sql.DB 
var id int
var id2 string

func handler(w http.ResponseWriter, r *http.Request) {
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

func handler1(w http.ResponseWriter, r *http.Request) {
    url := r.URL.Path
    if (strings.Contains(url, ":")) {
      fmt.Printf("\nurl=%v",url)
      ids := strings.Split(url, ":")
      fmt.Printf("\nid=%v",ids[1])
      fmt.Printf("\nid2=%v",ids[2])
       id, err := strconv.Atoi(ids[1])
        
//        id2, err2 := strconv.Atoi(ids[2])
        if (err == nil) {
        	dbquery(id,ids[2])
        }
//	output := fmt.Sprintf("%v.txt",id) // this comment mess is alternate redirect to [id].txt
//	t, _ := template.ParseFiles(output)
//	t.Execute(w, "testi")
    } //else {
	t, _ := template.ParseFiles("zab.html")
	t.Execute(w, "testi")
    //}
}

func dhandler(w http.ResponseWriter, r *http.Request) {
	q := dbquery(id,id2)
	t := template.New("test")
	t, _ = t.Parse(q)
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
        	q := dbquery(id,ids[2])
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


type TestZab struct {
	Clock int64		`json:"time"`
	Value float32	`json:"val"`
}

func dbquery1(id int, id2 string) {

	db, err := sql.Open("postgres", "user=ohtu dbname=multi-axis sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}
	
	// query := fmt.Sprintf("SELECT clock, value FROM history WHERE itemid = %v ORDER BY clock",id)
	// select value,clock from history where history.itemid in (select itemid from items where hostid = 10105 and key_ = 'vfs.fs.inode[/,pfree]');

	rows, err := db.Query(`SELECT clock, value FROM history where history.itemid IN (SELECT itemid FROM items WHERE hostid = $1 and key_ = $2) ORDER BY clock`, id, id2)
//	rows, err := db.Query(query)
	if err != nil {
            log.Fatal(err)
    	}

// close fo on exit and check for its returned error
	defer rows.Close()
	
//	output := fmt.Sprintf("%v.txt",id) 	// will instead output
//	fo, err := os.Create(output) 		// into [id].txt

	fo, err := os.Create("data_sample.txt") //output file
	if err != nil { panic(err) }	

// close fo on exit and check for its returned error	
	defer fo.Close(); 
	
// file writer with a buffer cuz hueg!!! database
	w := bufio.NewWriter(fo) 
       	if _, err := w.Write([]byte("[")); err != nil {
       	    panic(err)
       	}
       	
       	var value float32
	var clock int64
        first := true;
	for rows.Next() {
		if err := rows.Scan(&clock,&value); err != nil {
                    log.Fatal(err)
          	}
			// fmt.Printf("clock:%v\nvalue:%v\n", clock, value)
          	m := TestZab{clock,value}
          	b, err := json.Marshal(m)
       		if err != nil { panic(err) }
       		if (!first) {
	          	if _, err := w.Write([]byte(",")); err != nil {
          		panic(err)
       			}
       		}
        	if _, err := w.Write(b); err != nil {
        	    	panic(err)
        	}    
        	first = false;  	
    	}
	if _, err := w.Write([]byte("]")); err != nil {
       	    panic(err)
       	}
// buffer emptiness check thingy
 	if err = w.Flush(); err != nil { panic(err) } 
 	
	if err != nil { panic(err) }	
}

func dbquery (id int, id2 string) string {
	db, err := sql.Open("postgres", "user=ohtu dbname=multi-axis sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}
	rows, err := db.Query(`SELECT clock, value FROM history where history.itemid IN (SELECT itemid FROM items WHERE hostid = $1 and key_ = $2) ORDER BY clock`, id, id2)
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
	http.HandleFunc("/data_sample.txt", dhandler)	
//	http.HandleFunc("/metric/graph.js", ghandler)		
	http.HandleFunc("/graph.js", ghandler)		
//	http.HandleFunc("/metric/", handler)
	http.HandleFunc("/", handler)
	http.HandleFunc("/dashboard", bhandler)
	http.ListenAndServe(":8080", nil)
}
