package main

import (
	_ "github.com/lib/pq"
	"database/sql"
	"log"
	"net/http"
	"html/template"
	"bufio"
	"os"
	"encoding/json"
	"fmt"
	"strings"
	"strconv"
)

// non-empty ids: 23672, 23690, 23685, 23715, 23710

var db sql.DB 

func handler(w http.ResponseWriter, r *http.Request) {
    url := r.URL.Path
    if (strings.Contains(url, ":")) {
        fmt.Printf("\nurl=%v",url)
        ids := strings.Split(url, ":")
        fmt.Printf("\nid=%v",ids[1])
        id, err := strconv.Atoi(ids[1])
        if (err == nil) {
        	dbquery(id)
        }
//	output := fmt.Sprintf("%v.txt",id) // this comment mess is alternate redirect to [id].txt
//	t, _ := template.ParseFiles(output)
//	t.Execute(w, "testi")
    } //else {
        t, _ := template.ParseFiles("zab.html")
	t.Execute(w, "testi")
    //}
}

func ghandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("graph.js")
    t.Execute(w, "testi")
}

func dhandler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("data_sample.txt")
    t.Execute(w, "testi")
}

type TestZab struct {
	Clock int64	`json:"time"`
	Value float32	`json:"val"`
}

func dbquery(id int) {

	db, err := sql.Open("postgres", "user=ohtu dbname=multi-axis sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}
	
	query := fmt.Sprintf("SELECT clock, value FROM history WHERE itemid = %v ORDER BY clock",id)
	rows, err := db.Query(query)
	if err != nil {
            log.Fatal(err)
    	}
    	
	defer rows.Close() // close fo on exit and check for its returned error
	
//	output := fmt.Sprintf("%v.txt",id) 	// will instead output
//	fo, err := os.Create(output) 		// into [id].txt

	fo, err := os.Create("data_sample.txt") //output file
	if err != nil { panic(err) }	
	
	defer fo.Close(); // close fo on exit and check for its returned error
	
	w := bufio.NewWriter(fo) // file writer with a buffer cuz hueg!!! database
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
//		fmt.Printf("clock:%v\nvalue:%v\n", clock, value)
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
 	if err = w.Flush(); err != nil { panic(err) } // buffer emptiness check thingy
 	
	if err != nil { panic(err) }	
}

func main() {
	http.HandleFunc("/data_sample.txt", dhandler)	
	http.HandleFunc("/graph.js", ghandler)		
        http.HandleFunc("/", handler)
        http.ListenAndServe(":8080", nil)
}
