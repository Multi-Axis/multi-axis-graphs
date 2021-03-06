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
	"flag"
        "time"

)

//	Makes constant new db connections unnecessary.
var db *sql.DB
var habbixCfg string

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
	fmt.Printf("itemHandler, url:%s\n",r.URL.Path)

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

/**
 * 'apiHandler' is used to update the 'item_future' table.
 *
 * Updated item_future.id is the last part of the URL.  We parse updatable info
 * from POST request body.
 */
func apiHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Printf("apiHandler, url:%s\n",r.URL.Path)
	parts := strings.Split(r.URL.Path, "/")
	id, err := strconv.Atoi(parts[len(parts)-1])
	if err != nil {
		http.Error(w, "invalid futid", 400)
		return
	}

	r.ParseForm()
	params := r.FormValue("params")
	fmt.Printf(r.Method+"\n")
	if r.Method == "POST" {

		model, err := strconv.Atoi(r.FormValue("model"))
		if err != nil {
			fmt.Printf("model value fail")
			http.Error(w, err.Error(), 400)
			return
		}
		lower, _    := strconv.ParseBool(r.FormValue("tr_lower"))
		high, _     := strconv.ParseFloat(r.FormValue("tr_high"), 32)
		warning, _  := strconv.ParseFloat(r.FormValue("tr_warning"), 32)
		critical, _ := strconv.ParseFloat(r.FormValue("tr_critical"), 32)

		tr := Threshold { lower, float32(high), float32(warning), float32(critical), 0 }

		if (!isJSON(params)) {
			http.Error(w, "Invalid params, not json", 400)
			return
		}

		res, err := db.Exec(`UPDATE item_future SET params = $1, modelid = $3 WHERE id = $2`, params, id, model)
		if err != nil {
			log.Fatal(err)
		}

		fmt.Printf("type: %s", lower)


		// threshold: update, or insert if non exists
		// TODO: client should specify which threshold.id to use (or create new
		// threshold)
		res, err = db.Exec(`UPDATE threshold SET lower = $1, high = $2, warning = $3, critical = $4
		WHERE itemid = $5`, tr.Lower, tr.High, tr.Warning, tr.Critical, id)
		if err != nil {
			http.Error(w, "Update failed, check thresholds", 400)
		}
		affected, _ := res.RowsAffected()
		log.Print(affected)
		if affected == 0 {
			db.Exec(`INSERT INTO threshold (id, itemid, lower, high, warning,
			critical) VALUES (default, $1, $2, $3, $4, $5)`,
			id, tr.Lower, tr.High, tr.Warning, tr.Critical)
		}
		if updateFuture(id) {
			http.Redirect(w, r, r.Header.Get("Referer"), 302)
		} else { 
			http.Error(w, "Update failed, check params", 400)
		}

	} else {
		deliverItemByItemFutureId(w, id, params)
	}

}
/* }}} */

/* {{{ /static -------------------------------------------------------------- */

// handles graph drawing thingy requests, css files, that sort of thing.
func staticHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Printf("staticHandler, url:%s\n",r.URL.Path)
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

type Threshold struct {
	Lower bool
	High float32
	Warning float32
	Critical float32
	CriticalScaled float32
}

type ByCondition struct { Hosts []Host }
func (h ByCondition) Len() int { return len(h.Hosts) }
func (h ByCondition) Swap(i, j int) { h.Hosts[i], h.Hosts[j] = h.Hosts[j], h.Hosts[i] }
func (h ByCondition) Less(i, j int) bool { return h.Hosts[i].ConditionNum > h.Hosts[j].ConditionNum }

// normal = 0
// high = 1
// warn = 2
// critical = 3
func getCondition(value float32, tr Threshold, scale float32) (float64, string) {
	if comp(tr.Lower, value, tr.Critical / scale) {
		return 3, "critical"
	} else if comp(tr.Lower, value, tr.Warning / scale) {
		return 2, "warn"
	} else if comp(tr.Lower, value, tr.High / scale) {
		return 1, "high"
	} else {
		return 0, "normal"
	}
}

/* (lower && x < y) || (!lower && x > y) */
func comp(lower bool, x float32, y float32) bool {
	if lower {
		return x < y
	} else {
		return x > y
	}
}

func setCondition(i *Item) float64 {
	var c1 float64
	var c2 float64
	var c3 float64
	c1, i.Color_past_7d  = getCondition(i.Max_past_7d, i.Threshold, float32(i.Scale))
	c2, i.Color_next_24h = getCondition(i.Max_next_24h, i.Threshold, float32(i.Scale))
	c3, i.Color_next_7d  = getCondition(i.Max_next_7d, i.Threshold, float32(i.Scale))
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
	fmt.Printf("dashboardHandler, url:%s\n",r.URL.Path)
	hosts, error_hosts := getHosts(w)

	//Analysoidaan liikennevalot ja määritetään serverikohtainen danger tai normal -luokittelu, sen perusteella syttyykö valot
	for i := range hosts {
        	fmt.Println(hosts[i].Cpu.ItemId)
        	fmt.Println(hosts[i].Cpu.DaysInForecastRange)
        	fmt.Println(hosts[i].Mem.ItemId)
        	fmt.Println(hosts[i].Mem.DaysInForecastRange)

		c1 := setCondition(&hosts[i].Cpu)
		c2 := setCondition(&hosts[i].Mem)
		hosts[i].ConditionNum = math.Max(c1, c2)
		hosts[i].Condition = showCondition(hosts[i].ConditionNum)
        	fmt.Println(hosts[i].Condition)
        	fmt.Println(hosts[i].ConditionNum)
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

var newDashboardTmpl = template.Must(template.New("dashboard-new").ParseFiles(
	"templates/default-layout.tmpl",
	"templates/dashboard-new.tmpl"))

func layout(w http.ResponseWriter, t *template.Template, data interface{}) {
	err := t.ExecuteTemplate(w, "default-layout", data)
	if err != nil {
		log.Fatal(err)
	}
}

/* }}} */

/* {{{ interfacing habbix --------------------------------------------------- */

// tells habbix to re-sync database after parameter update
// always returns true since my local habbix always fails...
func updateFuture(id int) bool {
	fmt.Printf("\nStarting sync...")
	out, err := exec.Command("habbix", "sync", "-i", strconv.Itoa(id),habbixCfg).CombinedOutput()
	if err != nil {
		fmt.Printf("DB sync error: %s\n",err.Error())
		return true
	}
	fmt.Printf("%s", out)
	fmt.Printf("DB Synced.\n")
	return true
}

// gets future data from habbix without changing stored parameters
func getFutureNoUpdate(params string, id int) string {
	fmt.Printf("params=%s,id=%d\n", params, id)
	fmt.Printf(fmt.Sprintf("'%s'\n", params))
	cmd := exec.Command("habbix", "execute", "--outcombine", "-p", params, strconv.Itoa(id),habbixCfg)
	fmt.Println(cmd.Args)
	out, err := cmd.Output()
	var newJSON = string(out)
	fmt.Printf("%s", newJSON)
	if err != nil {
		fmt.Printf("habbix execute error: %s\nJSON:%s\n",err.Error(),newJSON)
		
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
	var scale int		//scale of forecast metric
	
	db.QueryRow(`SELECT item_future.modelid, items.value_type, item_future.itemid, items.name, host, params, details
	FROM item_future
	LEFT JOIN items on items.itemid = item_future.itemid
	LEFT JOIN hosts on hosts.hostid = items.hostid
	WHERE item_future.id = $1`,
		ifId).Scan(&modelId, &vtype, &itemId, &metric, &host, &params, &details)

	tr := queryThreshold(ifId)

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
		params = noUpdateParams
	} else {
		rows, _ = db.Query(`SELECT clock, value FROM future WHERE itemid = $1 ORDER BY clock`, ifId)
		future = parseValueJSON(rows)
	}
	
	row := db.QueryRow(`
		SELECT m.scale
		FROM item_future if
		INNER JOIN items i on i.itemid = if.itemid
		INNER JOIN metric m on m.key_ = i.key_
		WHERE if.id = $1`, ifId)
	
	if err := row.Scan(&scale); err != nil {
		fmt.Printf("fetching scale failed: %s", ifId, err)
		scale = 1
	}
	
	threshold := fmt.Sprintf(`{ "lower":%s, "high":%f, "warning":%f, "critical":%f }`,
		boolToJson(tr.Lower), tr.High, tr.Warning, tr.Critical)
	fmt.Printf("scale: %d\n",scale)
	output := fmt.Sprintf(
		`{ "host":"%s", "params":%s, "metric":"%s", "details":%s,
		"threshold":%s, "history":%s, "future":%s, "model":%d, "scale":%d }`,
		host, params, metric, details, threshold, history, future, modelId, scale)
	
	w.Write([]byte(output))
}

func queryThreshold(ifId int) Threshold {
	var tr Threshold
	db.QueryRow(`SELECT lower, high, warning, critical FROM threshold WHERE
	itemid = $1`, ifId).Scan(&tr.Lower, &tr.High, &tr.Warning, &tr.Critical)
	return tr
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
	Threshold      Threshold
	Max_past_7d    float32
	Max_next_24h   float32
	Max_next_7d    float32
	Color_past_7d  string
	Color_next_24h string
	Color_next_7d  string
	Condition      string
	Scale          int
	DaysInForecastRange float64
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

/* Item by item_future.id */
func getItem(ifid int) (error, Item) {

   if ifid < 0 {
      return fmt.Errorf("Item does not exist"), Item{}
   }

	var res Item
	var vtype int
        var dataRange []byte
        var resRange float64
//        var stop_lower string

	row := db.QueryRow(`
		SELECT i.value_type, if.id, i.name, i.itemid, m.scale, if.params
		FROM item_future if
		INNER JOIN items i on i.itemid = if.itemid
		INNER JOIN metric m on m.key_ = i.key_
		WHERE if.id = $1`, ifid)


	if err := row.Scan(&vtype, &res.Id, &res.Name, &res.ItemId, &res.Scale, &dataRange); err != nil {
		return fmt.Errorf("fetching info failed (%g): %s", ifid, err), Item{}
	}

	res.Threshold = queryThreshold(ifid)
	res.Threshold.CriticalScaled = res.Threshold.Critical / float32(res.Scale)

	//kaivetaan item_futuren params -jsonista rangen alku ja loppu
	var dat map[string]interface{}
	if err := json.Unmarshal(dataRange, &dat); err != nil {
		panic(err)
	}

	//muodostetaan alun, lopun ja tämän hetken perusteella ennusteaikavälin pituus sekunneissa
	var stop_upper float64
	var stop_lower float64

	if dat["stop_upper"] == nil {
		stop_upper = float64(time.Now().Unix())
	} else {
		stop_upper = dat["stop_upper"].(float64)
	}

	if dat["stop_lower"] == nil {
		stop_lower = float64(time.Now().Unix())
	} else {
		stop_lower = dat["stop_lower"].(float64)
	}

	resRange = stop_upper - stop_lower

	
	//muutos päiviksi ja tallennus Itemiin
	res.DaysInForecastRange = resRange/(3600*24)

	row = db.QueryRow(`
	SELECT max(h.value_max) as max_past_7d, max(f1.value) as max_next_24h,
			max(f2.value) as max_next_7d
	FROM trend h, future f1, future f2
	WHERE h.itemid  = $1
	AND   f1.itemid = $2
	AND   f2.itemid = f1.itemid
	AND h.clock  > EXTRACT(EPOCH FROM current_timestamp) - 7*86400
	AND f1.clock > EXTRACT(EPOCH FROM current_timestamp)
	AND f1.clock < EXTRACT(EPOCH FROM current_timestamp) + 86400
	AND f2.clock > EXTRACT(EPOCH FROM current_timestamp)
	AND f2.clock < EXTRACT(EPOCH FROM current_timestamp) + 7*86400`, res.ItemId, ifid)

	if err := row.Scan(&res.Max_past_7d, &res.Max_next_24h, &res.Max_next_7d); err != nil {
		log.Print("no history/future for item_future.id = ", ifid, ": ", err)
		res.Max_next_7d = -1;
		res.Max_next_24h = -1;
		res.Max_next_7d = -1;
	}

	res.Max_past_7d = res.Max_past_7d / float32(res.Scale)
	res.Max_next_24h = res.Max_next_24h / float32(res.Scale)
	res.Max_next_7d = res.Max_next_7d / float32(res.Scale)

	return nil, res
}

/* ALL item_future.id's */
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

	var database = flag.String("s", "multi-axis", "Postgres database")
	var user     = flag.String("u", "postgres", "Postgres user")
	var config   = flag.String("c", "config.yaml", "Config file for habbix")
	var port     = flag.String("p", "8080", "Port to bind to")
	var host     = flag.String("h", "localhost", "Host to listen on")
	flag.Parse()

	habbixCfg = fmt.Sprintf("--config=%s", *config)

	fmt.Printf("connecting to: %s,\nhabbix cfg: %s\n",*database,habbixCfg)

	// establish db connection
	db, err = sql.Open("postgres", fmt.Sprintf("user=%s dbname=%s sslmode=disable", *user, *database))
	if err != nil {
		log.Fatal(err)
	}
	err = db.Ping()
	if err != nil {
		log.Fatal(err)
	}

	// check that habbix is present
	var hab []byte
	hab, err = exec.Command("habbix", "--version").CombinedOutput()
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("%s ...is present\n", string(hab))

	http.HandleFunc("/static/", staticHandler)
	http.HandleFunc("/old-dashboard", dashboardHandler)
	http.HandleFunc("/dashboard", newDashboardHandler)
	http.HandleFunc("/item/", itemHandler)
	http.HandleFunc("/habbix/", apiDirectHabbix)
	http.HandleFunc("/api/", apiHandler)

	addr := *host + ":" + *port

	log.Printf("Listening at http://%s\n", addr)
	http.ListenAndServe(addr, nil)
}

/* NOTE: this is possibly unsafe. running habbix with random arguments should is
 * rather safe, but one may cause problems by reconfiguring something in the db. */
func apiDirectHabbix(w http.ResponseWriter, r *http.Request) {
	args := strings.Split(strings.TrimPrefix(r.URL.Path, "/habbix/"), " ")
	fmt.Println(args)
	cmd := exec.Command("habbix", args...)
	out, err := cmd.CombinedOutput()
	if err != nil {
		fmt.Printf("habbix errored: %s", err)
		http.Error(w, err.Error() + "\n" + string(out), 500)
	} else {
		w.Write(out)
	}
}

func newDashboardHandler(w http.ResponseWriter, r *http.Request) {
	layout(w, newDashboardTmpl, nil)
}

/* }}} */
