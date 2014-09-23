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

var db sql.DB

func handler(w http.ResponseWriter, r *http.Request) {
    url := r.URL.Path
    if (strings.Contains(url, ":")) {
        fmt.Printf("\nurl=%v",url)
        ids := strings.Split(url, ":")
        fmt.Printf("\nid=%v",ids[1])
        //23672
        id, err := strconv.Atoi(ids[1])
        if err != nil {
		log.Fatal(err)
		}

        dbquery(id)
       	output := fmt.Sprintf("%v.txt",id)
        t, _ := template.ParseFiles(output)
        t.Execute(w, "testi")
    } else {
        t, _ := template.ParseFiles("zab.html")
	t.Execute(w, "testi")
    }
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

func dbquery(id int) {
	db, err := sql.Open("postgres", "user=ohtu dbname=zabbix sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}
	query := fmt.Sprintf("SELECT clock, value FROM history WHERE itemid = %v ORDER BY clock",id)
	rows, err := db.Query(query)
	if err != nil {
            log.Fatal(err)
    	}
	defer rows.Close()
	
	output := fmt.Sprintf("%v.txt",id)
	fo, err := os.Create(output)
	if err != nil { panic(err) }		
	defer fo.Close();
	w := bufio.NewWriter(fo) 

	// close fo on exit and check for its returned error

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
	//	fmt.Printf("");
       		if err != nil { panic(err) }
          	
        	if _, err := w.Write(b); err != nil {
        	    panic(err)
        	}
    	}

 	if err = w.Flush(); err != nil { panic(err) }
 	
	if err != nil { panic(err) }	
}

func main() {
	http.HandleFunc("/data_sample.txt", dhandler)	
	http.HandleFunc("/graph.js", ghandler)		
        http.HandleFunc("/", handler)
        http.ListenAndServe(":8080", nil)
}
