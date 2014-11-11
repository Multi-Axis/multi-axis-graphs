package main

import (
	"bytes"
	"database/sql"
	"encoding/json"
	_ "github.com/lib/pq"
	"html/template"
	"log"
	"net/http"
	"fmt"
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

	var wantsJson bool
	if len(r.Header["Accept"]) > 0 {
		wantsJson = strings.Contains(r.Header["Accept"][0], "json")
	}
	if r.Method == "POST" {
		r.ParseForm()
		params := r.FormValue("params")
		threshold := r.FormValue("threshold")
		db.Exec(`UPDATE item_future SET params = $1 WHERE id = $2`, params, id)

      // threshold: update, or insert if non exists
      // TODO: client should specify which threshold.id to use (or create new
      // threshold)
		res, _ := db.Exec(`UPDATE threshold SET value = $1 WHERE itemid = $2`, threshold, id)
		affected, _ := res.RowsAffected()
		if affected == 0 {
			db.Exec(`INSERT INTO threshold VALUES (default, $1, true, $2)`, id, threshold)
		}
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
type Dashboard struct {
    Danger []Host
    Normal []Host
}


func dashboardHandler(w http.ResponseWriter, r *http.Request) {
        var hosts []Host
        hosts = getHosts(w)
        

        //Analysoidaan liikennevalot ja määritetään serverikohtainen danger tai normal -luokittelu, sen perusteella syttyykö valot
        var danger []Host
        var normal []Host

        for i := range hosts {
            //CPU
            if (hosts[i].Cpu.Max_past_7d-hosts[i].Cpu.Threshold<0) {
                hosts[i].Cpu.Color_past_7d="critical"
                hosts[i].Condition="issue"
            } else if (hosts[i].Cpu.Max_past_7d-hosts[i].Cpu.Threshold<hosts[i].Cpu.Threshold*0.5) {
                hosts[i].Cpu.Color_past_7d="warn"
                hosts[i].Condition="issue"
                hosts[i].Condition="issue"
            } else if (hosts[i].Cpu.Max_past_7d-hosts[i].Cpu.Threshold<hosts[i].Cpu.Threshold*0.8) {
                hosts[i].Cpu.Color_past_7d="high"
                hosts[i].Condition="issue"
            }            
            if (hosts[i].Cpu.Max_next_24h-hosts[i].Cpu.Threshold<0) {
                hosts[i].Cpu.Color_next_24h="critical"
                hosts[i].Condition="issue"
            } else if (hosts[i].Cpu.Max_next_24h-hosts[i].Cpu.Threshold<hosts[i].Cpu.Threshold*0.5) {
                hosts[i].Cpu.Color_next_24h="warn"
                hosts[i].Condition="issue"
            } else if (hosts[i].Cpu.Max_next_24h-hosts[i].Cpu.Threshold<hosts[i].Cpu.Threshold*0.8) {
                hosts[i].Cpu.Color_next_24h="high"
                hosts[i].Condition="issue"
            }            
            if (hosts[i].Cpu.Max_next_7d-hosts[i].Cpu.Threshold<0) {
                hosts[i].Cpu.Color_next_7d="critical"
                hosts[i].Condition="issue"
            } else if (hosts[i].Cpu.Max_next_7d-hosts[i].Cpu.Threshold<hosts[i].Cpu.Threshold*0.5) {
                hosts[i].Cpu.Color_next_7d="warn"
                hosts[i].Condition="issue"
            } else if (hosts[i].Cpu.Max_next_7d-hosts[i].Cpu.Threshold<hosts[i].Cpu.Threshold*0.8) {
                hosts[i].Cpu.Color_next_7d="high"
                hosts[i].Condition="issue"
            }                    

            //MEM
            if (hosts[i].Mem.Max_past_7d-hosts[i].Mem.Threshold<0) {
                hosts[i].Mem.Color_past_7d="critical"
                hosts[i].Condition="issue"
            } else if (hosts[i].Mem.Max_past_7d-hosts[i].Mem.Threshold<hosts[i].Mem.Threshold*0.5) {
                hosts[i].Mem.Color_past_7d="warn"
                hosts[i].Condition="issue"
                hosts[i].Condition="issue"
            } else if (hosts[i].Mem.Max_past_7d-hosts[i].Mem.Threshold<hosts[i].Mem.Threshold*0.8) {
                hosts[i].Mem.Color_past_7d="high"
                hosts[i].Condition="issue"
            }            
            if (hosts[i].Mem.Max_next_24h-hosts[i].Mem.Threshold<0) {
                hosts[i].Mem.Color_next_24h="critical"
                hosts[i].Condition="issue"
            } else if (hosts[i].Mem.Max_next_24h-hosts[i].Mem.Threshold<hosts[i].Mem.Threshold*0.5) {
                hosts[i].Mem.Color_next_24h="warn"
                hosts[i].Condition="issue"
            } else if (hosts[i].Mem.Max_next_24h-hosts[i].Mem.Threshold<hosts[i].Mem.Threshold*0.8) {
                hosts[i].Mem.Color_next_24h="high"
                hosts[i].Condition="issue"
            }            
            if (hosts[i].Mem.Max_next_7d-hosts[i].Mem.Threshold<0) {
                hosts[i].Mem.Color_next_7d="critical"
                hosts[i].Condition="issue"
            } else if (hosts[i].Mem.Max_next_7d-hosts[i].Mem.Threshold<hosts[i].Mem.Threshold*0.5) {
                hosts[i].Mem.Color_next_7d="warn"
                hosts[i].Condition="issue"
            } else if (hosts[i].Mem.Max_next_7d-hosts[i].Mem.Threshold<hosts[i].Mem.Threshold*0.8) {
                hosts[i].Mem.Color_next_7d="high"
                hosts[i].Condition="issue"
            }

            //
            if (hosts[i].Condition=="issue") { 
                danger = append(danger, hosts[i])
            } else {
                normal = append(normal, hosts[i])
            }
        }

        dashboard := Dashboard{danger, normal}
        fmt.Println("test")
//        fmt.Println(dashboard.Host[4].Cpu.Color_next_7d)

        t, _ := template.ParseFiles("templates/dashboard.html")
	t.Execute(w, dashboard)
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
	out, err := exec.Command("habbix","sync-db","-i",strconv.Itoa(id)).CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("%s", out)
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
	var host string
	var params string
	var details string
	var metric string
	var threshold float32
	var lower bool

	db.QueryRow(`SELECT item_future.itemid, items.name, host, params, details
	FROM item_future
	LEFT JOIN items on items.itemid = item_future.itemid
	LEFT JOIN hosts on hosts.hostid = items.hostid
	WHERE item_future.id = $1`,
	ifId).Scan(&itemId, &metric, &host, &params, &details)

	db.QueryRow(`SELECT value, lower FROM threshold WHERE itemid = $1`, ifId).Scan(&threshold, &lower)

	rows, _ := db.Query(`SELECT * FROM
	(SELECT DISTINCT ON (clock / 10800) clock, value FROM history WHERE itemid = $1) q
	ORDER BY clock`, itemId)
	history := parseValueJSON(rows)

	rows, _ = db.Query(`SELECT clock, value FROM future WHERE itemid = $1 ORDER BY clock`, ifId)
	future := parseValueJSON(rows)

	output := fmt.Sprintf(
		`{ "host":"%s", "params":%s, "metric":"%s", "details":%s, "threshold":%s, "history":%s, "future":%s }`,
		host, params, metric, details,
		fmt.Sprintf(`{ "value":%f }`, threshold),
		history, future)

	w.Write([]byte(output))
}

//Get items id and names from item_future and items

type Item struct {
    Id int
    Name string
    ItemId int
    Threshold float32
    ThresholdLow string
    Max_past_7d float32
    Max_next_24h float32
    Max_next_7d float32
    Color_past_7d string
    Color_next_24h string
    Color_next_7d string
}

type Host struct {
    Name string 
    Cpu Item
    Mem Item
    Condition string 
}

func getHosts(w http.ResponseWriter) []Host {
    rows, err := db.Query("SELECT hostid, name from hosts where hostid in (10101, 10102, 10103, 10104, 10105)")
    if err != nil {
            log.Fatal(err)
    }
    defer rows.Close()
    var hosts []Host
    for rows.Next() {
        var hostid int
        var name string
        var host Host
        var cpu Item
        var mem Item
        if err := rows.Scan(&hostid, &name); err != nil {
                log.Fatal(err)
        }
        cpu = getItem(w, hostid, 2)
        mem = getItem(w, hostid, 4)
        
        host.Name = name
        host.Cpu = cpu
        host.Mem = mem
        hosts = append(hosts, host)
    }
    return hosts
}
func getItem(w http.ResponseWriter, hostid, ifid int) Item {
    rows, err := db.Query("SELECT if.id, i.name, i.itemid, max(t.value) as threshold, t.lower, max(h.value) as max_past_7d, max(f1.value) as max_next_24h, max(f2.value) as max_next_7d FROM hosts ho, item_future if, items i, threshold t, history h, future f1, future f2 WHERE ho.hostid=i.hostid and if.itemid = i.itemid and i.itemid = h.itemid and if.id=t.itemid and h.clock > EXTRACT(EPOCH FROM current_timestamp) - 7*86400 and if.id = f1.itemid and f1.clock > EXTRACT(EPOCH FROM current_timestamp) and f1.clock < EXTRACT(EPOCH FROM current_timestamp) + 86400 and if.id = f2.itemid and f2.clock > EXTRACT(EPOCH FROM current_timestamp) and f2.clock < EXTRACT(EPOCH FROM current_timestamp) + 7*86400 AND if.id=$1 and ho.hostid = $2 GROUP by if.id, i.name, i.itemid, t.lower;", ifid, hostid)
    if err != nil {
            log.Fatal(err)
    }
    defer rows.Close()

    var results Item
    
    for rows.Next() {
        var res Item
        var id int
        var name string
        var itemid int
        var threshold float32
        var lower string
        var max_past_7d float32
        var max_next_24h float32
        var max_next_7d float32
        if err := rows.Scan(&id, &name, &itemid, &threshold, &lower, &max_past_7d, &max_next_24h, &max_next_7d); err != nil {
                log.Fatal(err)
        }
        res.Id = id
        res.Name = name
        res.ItemId = itemid
        res.Threshold = threshold
        res.ThresholdLow = lower
        res.Max_past_7d = max_past_7d
        res.Max_next_24h = max_next_24h
        res.Max_next_7d = max_next_7d
        return res;
//        results = append(results, res)

    }
    if err := rows.Err(); err != nil {
            log.Fatal(err)
    }
    
    return results


}

func getFutureIds(w http.ResponseWriter) []int {
    rows, err := db.Query("select id FROM item_future")
    if err != nil {
            log.Fatal(err)
    }
    defer rows.Close()

    var results []int
    
    for rows.Next() {
        var id int
        if err := rows.Scan(&id); err != nil {
                log.Fatal(err)
        }
        results = append(results, id)
    }
    if err := rows.Err(); err != nil {
            log.Fatal(err)
    }
    
    return results

}


func getFutureItems(w http.ResponseWriter) []Item {
    rows, err := db.Query("select a.id, b.name, b.itemid FROM item_future a, items b where a.itemid = b.itemid")
    if err != nil {
            log.Fatal(err)
    }
    defer rows.Close()

    var results []Item
    
    for rows.Next() {
        var res Item
        var id int
        var name string
        var itemid int
        if err := rows.Scan(&id, &name, &itemid); err != nil {
                log.Fatal(err)
        }
        res.Name = name
        res.Id = id
        res.ItemId = itemid
        results = append(results, res)
    }
    if err := rows.Err(); err != nil {
            log.Fatal(err)
    }
    
    return results

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
