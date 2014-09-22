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
)

func handler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("zab.html")
    t.Execute(w, "testi")
    fmt.Printf("%v testi",r.URL.Path)
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
	Value string	`json:"value"`
	Clock int64	`json:"clock"`
}

type TestJson struct {
	Name string
	Test int64
}

func main() {

	db, err := sql.Open("postgres", "user=ohtu dbname=zabbix sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}
	
	rows, err := db.Query("SELECT clock, value FROM history WHERE itemid = 23672 ORDER BY clock")
	if err != nil {
            log.Fatal(err)
    	}
    	
	defer rows.Close()
	fo, err := os.Create("testout.txt")
	if err != nil { panic(err) }		
	
	w := bufio.NewWriter(fo) 

	//ssh -L 5432:localhost:5432 ohtu@85.23.130.197

	// close fo on exit and check for its returned error
	//fmt.Printf("test")
	for rows.Next() {
		var value string
		var clock int64
                
           	if err := rows.Scan(&clock,&value); err != nil {
                    log.Fatal(err)
          	}
          //	fmt.Printf("clock:%v\nvalue:%v\n", clock, value)
          	m := TestZab{value,clock}
          //	fmt.Printf("%v", m)
          	b, err := json.Marshal(m)
		fmt.Printf("");
       		if err != nil { panic(err) }
          	
        	if _, err := w.Write(b); err != nil {
        	    panic(err)
        	}
    	}

 	if err = w.Flush(); err != nil { panic(err) }
 	
	defer fo.Close(); 
	if err != nil { panic(err) }
	
	http.HandleFunc("/data_sample.txt", dhandler)	
	http.HandleFunc("/graph.js", ghandler)		
        http.HandleFunc("/", handler)
        http.ListenAndServe(":8080", nil)
}
