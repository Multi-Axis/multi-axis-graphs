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
)

func handler(w http.ResponseWriter, r *http.Request) {
    t, _ := template.ParseFiles("zab.html")
    t.Execute(w, "testi")
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
	
	rows, err := db.Query("SELECT name FROM hosts")
	if err != nil {
            log.Fatal(err)
    	}
    	
	defer rows.Close()
	fo, err := os.Create("testout.txt")
	if err != nil { panic(err) }		
	
	w := bufio.NewWriter(fo) 

	// close fo on exit and check for its returned error

	for rows.Next() {
		var name string
           	if err := rows.Scan(&name); err != nil {
                    log.Fatal(err)
          	}
          	m := TestJson{name,12345}
          	b, err := json.Marshal(m)
       		if err != nil { panic(err) }
          	
        	if _, err := w.Write(b); err != nil {
        	    panic(err)
        	}
    	}

 	if err = w.Flush(); err != nil { panic(err) }
 	
	defer fo.Close(); 
	if err != nil { panic(err) }
		
        http.HandleFunc("/", handler)
        http.ListenAndServe(":8080", nil)
}
