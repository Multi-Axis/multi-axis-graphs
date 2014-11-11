package main

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	_ "github.com/lib/pq"
	"html/template"
	"io/ioutil"
	"log"
	"net/http"
	"os/exec"
	"strconv"
	"strings"

)

//	Makes constant new db connections unnecessary.
var db *sql.DB

/* {{{ /item ---------------------------------------------------------------- */

// handles requests/updates for specific items
func itemHandler(w http.ResponseWriter, r *http.Request) {

	var wantsJson bool
	if len(r.Header["Accept"]) > 0 {
		wantsJson = strings.Contains(r.Header["Accept"][0], "json")
	}
	
	parts := strings.Split(r.URL.Path, "/")
	
	if len(parts) < 4 && !wantsJson {
		http.NotFound(w, r)
		return
	}
	host := parts[len(parts)-2]
	metric := parts[len(parts)-1]
	
	var id = getItemFutureIdByHostMetric(host, metric)
	if id < 0  {
		http.NotFound(w, r)
		return
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
		params := r.FormValue("params")
		deliverItemByItemFutureId(w, id, params)
	} else {
		graphViewHTML(w)
	}
}
/* }}} */

/* {{{ /static -------------------------------------------------------------- */

// handles graph drawing thingy requests, css files, that sort of thing.
func staticHandler(w http.ResponseWriter, r *http.Request) {
	url := r.URL.Path
	path := strings.TrimPrefix(url, "/")

	if strings.HasSuffix(path, ".js") {
		w.Header().Set("Content-Type", "application/javascript")
	} else if strings.HasSuffix(path, ".css") {
		w.Header().Set("Content-Type", "text/css")
	}

	b, _ := ioutil.ReadFile(path)
	w.Write(b)
}
/* }}} */

/* {{{ /dashboard ----------------------------------------------------------- */
type Dashboard struct {
	Danger []Host
	Normal []Host 
}

// normal = 0
// issua = 1
func getCondition(value float32, threshold float32) (int, string) {
	if value - threshold < 0 {
		return 1, "critical"
	} else if value < threshold * 0.5 {
		return 1, "warn"
	} else if value < threshold * 0.8 {
		return 1, "high"
	} else {
		return 0, "normal" // Huh?
	}
}

func dashboardHandler(w http.ResponseWriter, r *http.Request) {

	var hosts []Host
	hosts = getHosts(w)

	//Analysoidaan liikennevalot ja määritetään serverikohtainen danger tai normal -luokittelu, sen perusteella syttyykö valot
	var danger []Host
	var normal []Host

	cond := 0
	for i := range hosts {

		var condx int

		// cpu
		condx, hosts[i].Cpu.Color_past_7d =
			getCondition(hosts[i].Cpu.Max_past_7d, hosts[i].Cpu.Threshold)
		if condx > cond { cond = condx }

		condx, hosts[i].Cpu.Color_next_24h =
			getCondition(hosts[i].Cpu.Max_next_24h, hosts[i].Cpu.Threshold)
		if condx > cond { cond = condx }

		condx, hosts[i].Cpu.Color_next_7d =
			getCondition(hosts[i].Cpu.Max_next_7d, hosts[i].Cpu.Threshold)
		if condx > cond { cond = condx }

		// mem
		condx, hosts[i].Mem.Color_past_7d =
			getCondition(hosts[i].Mem.Max_past_7d, hosts[i].Mem.Threshold)
		if condx > cond { cond = condx }

		condx, hosts[i].Mem.Color_next_24h =
			getCondition(hosts[i].Mem.Max_next_24h, hosts[i].Mem.Threshold)
		if condx > cond { cond = condx }

		condx, hosts[i].Mem.Color_next_7d =
			getCondition(hosts[i].Mem.Max_next_7d, hosts[i].Mem.Threshold)
		if condx > cond { cond = condx }


		if cond == 0 {
			hosts[i].Condition = "normal"
			normal = append(normal, hosts[i])
		} else {
			hosts[i].Condition = "issue"
			danger = append(danger, hosts[i])
		}
	}
	dashboard := Dashboard{danger, normal}
	//fmt.Println(dashboard)
    t, err := template.ParseFiles("templates/dashboard.tmpl", "templates/metric_table.tmpl", "templates/metric_table_end.tmpl",
    							"templates/mem_usage.tmpl", "templates/cpu_load.tmpl")
    	if err != nil {
		log.Fatal(err)
	}
	t.ExecuteTemplate(w, "dashboard", dashboard)
}
/* }}} */

/* {{{ Templates ------------------------------------------------------------ */
func graphViewHTML(w http.ResponseWriter) {
	t, _ := template.ParseFiles("templates/graphview.html")
	t.Execute(w, "testi")
}
/* }}} */


/* {{{ interfacing habbix --------------------------------------------------- */

// tells habbix to re-sync database after parameter update
func updateFuture(id int) {
	fmt.Printf("\nStarting sync...")
	out, err := exec.Command("habbix", "sync-db", "-i", strconv.Itoa(id)).CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("%s", out)
	fmt.Printf("\nDB Synced.")
}

// gets future data from habbix without changing stored parameters
func getFutureNoUpdate(params string, id int) string {
	fmt.Printf("params=%s,id=%d\n", params, id)
	fmt.Printf(fmt.Sprintf("'%s'\n",params))
	cmd := exec.Command("habbix", "execute", "--outcombine", "-p", params, strconv.Itoa(id))
	fmt.Println(cmd.Args)
	out, err := cmd.CombinedOutput()
	var newJSON = string(out)
	fmt.Printf("%s",newJSON)
	if err != nil {
		log.Fatal(err)
	}
	return newJSON
}

/* }}} */

/* {{{ Querying graph JSON -------------------------------------------------- */
type ClockValue struct {
	Clock int64   `json:"time"`
	Value float32 `json:"val"`
}

// makes a JSON string from rows returned by db.Query
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

func getItemFutureIdByHostMetric(host string, metric string) int {
	var fid int
	err := db.QueryRow(`SELECT item_future.id
	FROM hosts, metric, items, item_future
	WHERE hosts.name = $1
	AND metric.name = $2
	AND items.key_ = metric.key_ AND item_future.itemid = items.itemid`, host,
	metric).Scan(&fid)
	if err != nil { return -1 }
	return fid
}

/* Deliver graph JSON data based on an item_future.id */
func deliverItemByItemFutureId(w http.ResponseWriter, ifId int, noUpdateParams string) {
	var itemId 		int	// unique id of item
	var host 		string // name of host server (?)
	var params 		string // current parameters used by forecast calculation
	var details 	string // forecast-specific details
	var metric 		string // name of forecast metric
	var threshold	float32 // value of current treshold
	var lower 		bool // true if treshold is lower limit rather than upper

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
	var future string;
	if (len(noUpdateParams) > 0) {
		future = getFutureNoUpdate(noUpdateParams, ifId)
	} else {
		rows, _ = db.Query(`SELECT clock, value FROM future WHERE itemid = $1 ORDER BY clock`, ifId)
		future = parseValueJSON(rows)
	}
	output := fmt.Sprintf(
		`{ "host":"%s", "params":%s, "metric":"%s", "details":%s, "threshold":%s, "history":%s, "future":%s }`,
		host, params, metric, details,
		fmt.Sprintf(`{ "value":%f }`, threshold),
		history, future)

	w.Write([]byte(output))
}
/* }}} */

/* {{{ Get items id and names from item_future and items -------------------- */

// dashboard item
type Item struct {
	Id             int
	Name           string
	ItemId         int
	Threshold      float32
	ThresholdLow   string
	Max_past_7d    float32
	Max_next_24h   float32
	Max_next_7d    float32
	Color_past_7d  string
	Color_next_24h string
	Color_next_7d  string
}

type Host struct {
	Name      string
	Cpu       Item
	Mem       Item
	Condition string
}

func getHosts(w http.ResponseWriter) []Host {
	rows, err := db.Query(`SELECT hostid, name
	FROM hosts WHERE hostid in (10101, 10102, 10103, 10104, 10105)`) // TODO hey! no hardcode here
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var hosts []Host
	var hostid int
	var host Host

	for rows.Next() {
		if err := rows.Scan(&hostid, &host.Name); err != nil {
			log.Fatal(err)
		}
		host.Cpu = getItem(w, hostid, 2)
		host.Mem = getItem(w, hostid, 4)
		hosts = append(hosts, host)
	}
	return hosts
}
func getItem(w http.ResponseWriter, hostid, ifid int) Item {
	rows, err := db.Query(`SELECT
		if.id, i.name, i.itemid, max(t.value) as threshold,
		t.lower, max(h.value) as max_past_7d, max(f1.value) as max_next_24h,
		max(f2.value) as max_next_7d
	FROM hosts ho, item_future if, items i, threshold t, history h, future f1, future f2
	WHERE ho.hostid=i.hostid
	AND if.itemid = i.itemid
	AND i.itemid = h.itemid
	AND if.id=t.itemid
	AND h.clock > EXTRACT(EPOCH FROM current_timestamp) - 7*86400
	AND if.id = f1.itemid
	AND f1.clock > EXTRACT(EPOCH FROM current_timestamp)
	AND f1.clock < EXTRACT(EPOCH FROM current_timestamp) + 86400
	AND if.id = f2.itemid
	AND f2.clock > EXTRACT(EPOCH FROM current_timestamp)
	AND f2.clock < EXTRACT(EPOCH FROM current_timestamp) + 7*86400
	AND if.id = $1 AND ho.hostid = $2
	GROUP by if.id, i.name, i.itemid, t.lower`, ifid, hostid)
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
		return res
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

/* {{{ main() --------------------------------------------------------------- */

// initializes db connection and uses standard http.HandleFunc for routing
func main() {
	var err error
	db, err = sql.Open("postgres", "user=ohtu dbname=multi-axis sslmode=disable")
	if err != nil {
		log.Fatal(err)
	}
	http.HandleFunc("/static/", staticHandler)
	http.HandleFunc("/", dashboardHandler)
	http.HandleFunc("/item/", itemHandler)
	http.ListenAndServe(":8080", nil)
}
/* }}} */
