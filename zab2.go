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
	"math"
	"sort"

)

//	Makes constant new db connections unnecessary.
var db *sql.DB

func isJSON(s string) bool {
    var js map[string]interface{}
    return json.Unmarshal([]byte(s), &js) == nil

}

func Scalefmt(s int, x float32) float32 {
	return x / float32(s)
}

/* {{{ /item ---------------------------------------------------------------- */

type GraphView struct {
	Id string
	Models []Model
}

type Model struct {
	Id int
	Name string
}

// handles requests/updates for specific items
func itemHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("itemHandler")

	parts := strings.Split(r.URL.Path, "/")

	if len(parts) < 4 {
		http.NotFound(w, r)
		return
	}

	host := parts[len(parts)-2]
	metric := parts[len(parts)-1]

	var id = getItemFutureIdByHostMetric(host, metric)
	if id < 0 {
		http.NotFound(w, r)
		return
	}

	layout(w, itemTmpl, GraphView{strconv.Itoa(id), getModels()})
}

func getModels() []Model {
	var models []Model
	var id int
	var name string

	rows, _ := db.Query(`SELECT id, name FROM future_model`)
	for rows.Next() {
		rows.Scan(&id, &name)
		models = append(models, Model{id, name})
	}
	return models
}



/* }}} */

/* {{{ /api */
func apiHandler(w http.ResponseWriter, r *http.Request) {
	parts := strings.Split(r.URL.Path, "/")
	id, err := strconv.Atoi(parts[len(parts)-1])
	if err != nil {
		http.Error(w, "invalid futid", 400)
		return
	}

	r.ParseForm()
	params := r.FormValue("params")
	model  := r.FormValue("modelSelect")
	
	if r.Method == "POST" {
		if (!isJSON(params)) {
			http.Error(w, "Invalid params, not json", 400)
			return
		}

		threshold := r.FormValue("threshold")
		lower := r.FormValue("threshold_type")
		fmt.Printf("type: %s", lower)
		db.Exec(`UPDATE item_future SET params = $1, model = $3 WHERE id = $2`, params, id, model)
		// threshold: update, or insert if non exists
		// TODO: client should specify which threshold.id to use (or create new
		// threshold)
		res, err := db.Exec(`UPDATE threshold SET lower = $1, value = $2 WHERE itemid = $3`, lower, threshold, id)
		if err == nil {
			affected, _ := res.RowsAffected()
			if affected == 0 {
				db.Exec(`INSERT INTO threshold VALUES (default, $1, $2, $3)`, id, lower, threshold)
			}
		}
		updateFuture(id)
		http.Redirect(w, r, r.Header["Referer"][0], 302)

	} else {
		deliverItemByItemFutureId(w, id, params)
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
	Hosts []Host
	ErrorHosts []ErrorHost
}

type ByCondition struct { Hosts []Host }
func (h ByCondition) Len() int {return len(h.Hosts) }
func (h ByCondition) Swap(i, j int) { h.Hosts[i], h.Hosts[j] = h.Hosts[j], h.Hosts[i] }
func (h ByCondition) Less(i, j int) bool { return h.Hosts[i].ConditionNum > h.Hosts[j].ConditionNum }

// normal = 0
// high = 1
// warn = 2
// critical = 3
func getCondition(value float32, threshold float32, lower bool) (float64, string) {
	if (lower && value < threshold) || (!lower && value > threshold) {
		return 3, "critical"
	} else if (lower && value < threshold*0.5) || (!lower && value > threshold*0.5) {
		return 2, "warn"
	} else if (lower && value < threshold*0.8) || (!lower && value > threshold*0.8) {
		return 1, "high"
	} else {
		return 0, "normal" // Huh?
	}
}

func setCondition(i *Item) float64 {
	var c1 float64
	var c2 float64
	var c3 float64
	c1, i.Color_past_7d  = getCondition(i.Max_past_7d, i.Threshold, i.ThresholdLow)
	c2, i.Color_next_24h = getCondition(i.Max_next_24h, i.Threshold, i.ThresholdLow)
	c3, i.Color_next_7d  = getCondition(i.Max_next_7d, i.Threshold, i.ThresholdLow)
	c := math.Max(c1, math.Max(c2, c3))
	i.Condition = showCondition(c)
	return c
}

func showCondition(cond float64) string {
	if cond == 0 {
		return "normal"
	} else if cond == 1 {
		return "high"
	} else if cond == 2 {
		return "warn"
	} else {
		return "critical"
	}
}

func dashboardHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("dashboardHandler")
	hosts, error_hosts := getHosts(w)

	//Analysoidaan liikennevalot ja määritetään serverikohtainen danger tai normal -luokittelu, sen perusteella syttyykö valot
	for i := range hosts {
		// cpu
		c1 := setCondition(&hosts[i].Cpu)
		c2 := setCondition(&hosts[i].Mem)
		hosts[i].ConditionNum = math.Max(c1, c2)
		hosts[i].Condition = showCondition(hosts[i].ConditionNum)
	}
	sort.Sort(ByCondition{hosts})
	dashboard := Dashboard{hosts, error_hosts}
	layout(w, dashboardTmpl, dashboard)
}

/* }}} */

/* {{{ Templates ------------------------------------------------------------ */

var dashboardTmpl = template.Must(template.New("dashboard").ParseFiles(
	"templates/default-layout.tmpl",
	"templates/dashboard.tmpl"))

var itemTmpl = template.Must(template.New("item").ParseFiles(
	"templates/default-layout.tmpl",
	"templates/graphview.html"))

func layout(w http.ResponseWriter, t *template.Template, data interface{}) {
	err := t.ExecuteTemplate(w, "default-layout", data)
	if err != nil {
		log.Fatal(err)
	}
}

/* }}} */

/* {{{ interfacing habbix --------------------------------------------------- */

// tells habbix to re-sync database after parameter update
func updateFuture(id int) {
	fmt.Printf("\nStarting sync...")
	out, err := exec.Command("habbix", "sync", "-i", strconv.Itoa(id)).CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("%s", out)
	fmt.Printf("\nDB Synced.")
}

// gets future data from habbix without changing stored parameters
func getFutureNoUpdate(params string, id int) string {
	fmt.Printf("params=%s,id=%d\n", params, id)
	fmt.Printf(fmt.Sprintf("'%s'\n", params))
	cmd := exec.Command("habbix", "execute", "--outcombine", "-p", params, strconv.Itoa(id))
	fmt.Println(cmd.Args)
	out, err := cmd.Output()
	var newJSON = string(out)
	fmt.Printf("%s", newJSON)
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

	err := db.QueryRow(`SELECT item_future.id FROM hosts, metric, items, item_future
	WHERE hosts.name = $1
	AND hosts.hostid = items.hostid
	AND metric.name = $2
	AND items.key_ = metric.key_
	AND item_future.itemid = items.itemid;`, host, metric).Scan(&fid)

	if err != nil {
		fmt.Println(err, host, metric)
		return -1
	}
	fmt.Println(host, metric, fid)
	return fid
}

/* Deliver graph JSON data based on an item_future.id */
func deliverItemByItemFutureId(w http.ResponseWriter, ifId int, noUpdateParams string) {
	var vtype int
	var modelId int
	var itemId int        // unique id of item
	var host string       // name of host server (?)
	var params string     // current parameters used by forecast calculation
	var details string    // forecast-specific details
	var metric string     // name of forecast metric
	var threshold float32 // value of current treshold
	var lower bool        // true if treshold is lower limit rather than upper

	db.QueryRow(`SELECT item_future.modelid, items.value_type, item_future.itemid, items.name, host, params, details
	FROM item_future
	LEFT JOIN items on items.itemid = item_future.itemid
	LEFT JOIN hosts on hosts.hostid = items.hostid
	WHERE item_future.id = $1`,
		ifId).Scan(&modelId, &vtype, &itemId, &metric, &host, &params, &details)

	db.QueryRow(`SELECT value, lower FROM threshold WHERE itemid = $1`, ifId).Scan(&threshold, &lower)

	// rows, _ := db.Query(`SELECT * FROM
	// (SELECT DISTINCT ON (clock / 10800) clock, value FROM history WHERE itemid = $1) q
	// ORDER BY clock`, itemId)
	rows, _ := db.Query(`
	SELECT clock, value_avg FROM trend as trend WHERE itemid = $1 UNION
	SELECT * FROM (SELECT clock, value FROM history WHERE itemid = $1 ORDER BY clock DESC LIMIT 1) as h
	ORDER BY clock`, itemId)
	history := parseValueJSON(rows)
	var future string
	if len(noUpdateParams) > 0 {
		future = getFutureNoUpdate(noUpdateParams, ifId)
	} else {
		rows, _ = db.Query(`SELECT clock, value FROM future WHERE itemid = $1 ORDER BY clock`, ifId)
		future = parseValueJSON(rows)
	}
	output := fmt.Sprintf(
		`{ "host":"%s", "params":%s, "metric":"%s", "details":%s,
		"threshold":%s, "history":%s, "future":%s, "model":%d }`,
		host, params, metric, details,
		fmt.Sprintf(`{ "value":%f, "lower":%s }`, threshold, boolToJson(lower)),
		history, future, modelId)

	w.Write([]byte(output))
}

func boolToJson(b bool) string {
	if b {
		return "true"
	} else {
		return "false"
	}
}

/* }}} */

/* {{{ Get items id and names from item_future and items -------------------- */

// dashboard item
type Item struct {
	Id             int
	Name           string
	ItemId         int
	Threshold      float32
	ThresholdLow   bool
	Max_past_7d    float32
	Max_next_24h   float32
	Max_next_7d    float32
	Color_past_7d  string
	Color_next_24h string
	Color_next_7d  string
	Condition      string
	Scale          int
}

type Host struct {
	Name      string
	Cpu       Item
	Mem       Item
	Condition string
	ConditionNum float64
}

type ErrorHost struct {
	Name	string
	Err 	error
}

func getHosts(w http.ResponseWriter) ([]Host, []ErrorHost) {
	rows, err := db.Query(`SELECT name FROM hosts WHERE hostid IN
		(SELECT DISTINCT hosts_groups.hostid FROM hosts_groups INNER
		JOIN items ON hosts_groups.hostid = items.hostid
		WHERE hosts_groups.groupid > 1)`) // >1 discards templates
	if err != nil {
		log.Fatal(err)
	}
	defer rows.Close()

	var hosts []Host
	var error_hosts []ErrorHost
	var host Host

	for rows.Next() {
		if err := rows.Scan(&host.Name); err != nil {
			log.Fatal(err)
		}
		err, host.Cpu = getItem(getItemFutureIdByHostMetric(host.Name, "cpu"))
		if err != nil {
			error_hosts = append(error_hosts, ErrorHost{host.Name, err})
			continue
		}
		err, host.Mem = getItem(getItemFutureIdByHostMetric(host.Name, "mem"))
		if err != nil {
			error_hosts = append(error_hosts, ErrorHost{host.Name, err})
			continue
		}
		hosts = append(hosts, host)
	}
	return hosts, error_hosts
}
func getItem(ifid int) (error, Item) {

   if ifid < 0 {
      return fmt.Errorf("Item does not exist"), Item{}
   }

	var res Item
	var vtype int

	row := db.QueryRow(`
		SELECT i.value_type, if.id, i.name, i.itemid, m.scale
		FROM item_future if
		INNER JOIN items i on i.itemid = if.itemid
		INNER JOIN metric m on m.key_ = i.key_
		WHERE if.id = $1`, ifid)

	if err := row.Scan(&vtype, &res.Id, &res.Name, &res.ItemId, &res.Scale); err != nil {
		return fmt.Errorf("fetching info failed (%g): %s", ifid, err), Item{}
	}

	db.QueryRow(`SELECT value, lower FROM threshold WHERE itemid = $1`,
		ifid).Scan(&res.Threshold, &res.ThresholdLow)

	row = db.QueryRow(`
	SELECT max(h.value) as max_past_7d, max(f1.value) as max_next_24h,
			max(f2.value) as max_next_7d
	FROM history h, future f1, future f2
	WHERE h.itemid  = $1
	AND   f1.itemid = $2
	AND   f2.itemid = f1.itemid
	AND h.clock  > EXTRACT(EPOCH FROM current_timestamp) - 7*86400
	AND f1.clock > EXTRACT(EPOCH FROM current_timestamp)
	AND f1.clock < EXTRACT(EPOCH FROM current_timestamp) + 86400
	AND f2.clock > EXTRACT(EPOCH FROM current_timestamp)
	AND f2.clock < EXTRACT(EPOCH FROM current_timestamp) + 7*86400`, res.ItemId, ifid)

	if err := row.Scan(&res.Max_past_7d, &res.Max_next_24h, &res.Max_next_7d); err != nil {
		log.Fatal("Could not find item_future ", ifid, " ", err)
	}

	res.Threshold = res.Threshold / float32(res.Scale)
	res.Max_past_7d = res.Max_past_7d / float32(res.Scale)
	res.Max_next_24h = res.Max_next_24h / float32(res.Scale)
	res.Max_next_7d = res.Max_next_7d / float32(res.Scale)

	return nil, res
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

	// establish db connection
	err = db.Ping()
	if err != nil {
		log.Fatal(err)
	}

	// check that habbix is present
	_, err = exec.Command("habbix", "--version").CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}

	http.HandleFunc("/static/", staticHandler)
	http.HandleFunc("/dashboard", dashboardHandler)
	http.HandleFunc("/item/", itemHandler)
	http.HandleFunc("/api/", apiHandler)

	log.Print("Listening at port 8080 ( http://localhost:8080 )")

	http.ListenAndServe(":8080", nil)
}

/* }}} */
