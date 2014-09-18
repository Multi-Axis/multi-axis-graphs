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
	clock int64
	value float32
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
	
	rows, err := db.Query("SELECT clock, value FROM history WHERE itemid = 23692 ORDER BY clock")
	if err != nil {
            log.Fatal(err)
    	}
    	
	defer rows.Close()
	fo, err := os.Create("testout.txt")
	if err != nil { panic(err) }		
	
	w := bufio.NewWriter(fo) 

	// close fo on exit and check for its returned error

	for rows.Next() {
		var value float32
		var clock int64
           	if err := rows.Scan(&clock,&value); err != nil {
                    log.Fatal(err)
          	}
          	fmt.Printf("%f\n", value)
          	m := TestZab{clock,value}
          	b, err := json.Marshal(m)
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
