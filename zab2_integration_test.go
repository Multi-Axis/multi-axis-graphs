package main

import (
	"net/http"
	"net/url"
	//	"fmt"
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"os/exec"
	"strings"
	"testing"
	"time"
	//	"os"
	//	"net/url"
)

var server *exec.Cmd
var client *http.Client

var dashboardtests = []struct {
	old string
	new string
}{
	{"ohtu1", "ohtu1"},
	{"kivimylly.relex.fi", "kivimylly.relex.fi"},
	{"<span class=high>0.813</span>", "<span class=normal>0.813</span>"},
}

var graphtests = []struct {
	item string
}{
	{"cpu"},
	{"mem"},
	{"swap"},
}

var basicupdatetests = []struct {
	id          string
	oldlower    string
	newlower    string
	oldhigh     string
	newhigh     string
	oldwarning  string
	newwarning  string
	oldcritical string
	newcritical string
	oldparams   string
	newparams   string
}{
	{"9",
		"false", "true",
		"1.000000", "5.000000",
		"0.800000", "15.000000",
		"0.900000", "25.000000",
		"params\":{\"stop_lower\":-518400,\"stop_upper\":null}",
		"{\"stop_lower\":1414380539,\"pre_filter\":\"DailyMax\"}",
	},
	{"6",
		"true", "true",
		"60.000000", "95.000000",
		"0.000000", "75.000000",
		"0.000000", "35.000000",
		"params\":{\"test\":6}",
		"{\"stop_lower\":1414380539,\"pre_filter\":\"DailyMax\"}",
	},
	{"8",
		"false", "true",
		"826781184.000000", "500000000.000000",
		"885836992.000000", "500000000.000000",
		"1181116032.000000", "500000000.000000",
		"params\":{\"stop_lower\":1415488908,\"stop_upper\":1416395691}",
		"{\"stop_lower\":1414380539,\"pre_filter\":\"DailyMax\"}",
	},
}

var forecasttests = []struct {
	id  string
	old string
	new string
}{
	{"6",
		"future\":[{\"time\":1418372406,\"val\":99.6706},{\"time\":1418458806,\"val\":99.7462},{\"time\":1418545206,\"val\":99.8219},{\"time\":1418631606,\"val\":99.8975},{\"time\":1418718006,\"val\":99.9731},{\"time\":1418804406,\"val\":100.0487},{\"time\":1418890806,\"val\":100.1243},{\"time\":1418977206,\"val\":100.1999}]",
		"future\":[{\"time\":1418372406,\"val\":99.6706},{\"time\":1418458806,\"val\":99.7462},{\"time\":1418545206,\"val\":99.8219},{\"time\":1418631606,\"val\":99.8975},{\"time\":1418718006,\"val\":99.9731},{\"time\":1418804406,\"val\":100.0487},{\"time\":1418890806,\"val\":100.1243},{\"time\":1418977206,\"val\":100.1999}]",
	},
	{"9",
		"future\":[{\"time\":1418372405,\"val\":0},{\"time\":1418458805,\"val\":-0.0002},{\"time\":1418545205,\"val\":-0.0004},{\"time\":1418631605,\"val\":-0.0005},{\"time\":1418718005,\"val\":-0.0007},{\"time\":1418804405,\"val\":-0.0009},{\"time\":1418890805,\"val\":-0.0011},{\"time\":1418977205,\"val\":-0.0012}]",
		"future", // habbix fail so doesn't work anyway
	},
	{"8",
		"future\":[{\"time\":1418372405,\"val\":8.6340384e+08},{\"time\":1418458805,\"val\":8.6209216e+08},{\"time\":1418545205,\"val\":8.607805e+08},{\"time\":1418631605,\"val\":8.594688e+08},{\"time\":1418718005,\"val\":8.581571e+08},{\"time\":1418804405,\"val\":8.5684544e+08},{\"time\":1418890805,\"val\":8.5553376e+08},{\"time\":1418977205,\"val\":8.542221e+08}]",
		"future",
	},
}

var wrongurl = []struct {
	url string
}{
	//	{"http://localhost:8080/api/100"},
	{"http://localhost:8080/item/ohtufail/cpu"},
	{"http://localhost:8080/item/ohtu1/carrot"},
	//	{"http://localhost:8080/static/zoink.css"},
	{"http://localhost:8080/dashbored"},
	{"http://localhost:8080/"},
}

func TestWrongUrl(t *testing.T) {
	for _, wrong := range wrongurl {
		r, err := http.Get(wrong.url)
		if err != nil {
			t.Errorf(err.Error())
		}
		if r.StatusCode != 404 {
			t.Errorf("found false url:%s with statuscode:%s", wrong.url, r.StatusCode)
		}
	}
}

func TestItemsFound(t *testing.T) {
	for _, item := range graphtests {
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/item/ohtu1/%s", item.item))
		if err != nil {
			t.Errorf(err.Error())
		}
		if r.StatusCode != 200 {
			t.Errorf("could not find item %s, received statuscode: %d", item.item, r.StatusCode)
		}
	}
}

func TestDashBoard(t *testing.T)  { dashboarding(t, false) }
func TestParams(t *testing.T)     { parametring(t, false) }
func TestThresholds(t *testing.T) { tresholding(t, false) }
func TestForecast(t *testing.T)   { forecasting(t, false) }

func TestUpdates(t *testing.T) {
	for _, apiId := range basicupdatetests {
		form := url.Values{}
		form.Set("params", apiId.newparams)
		form.Set("tr_high", apiId.newhigh)
		form.Set("tr_critical", apiId.newcritical)
		form.Set("tr_warning", apiId.newwarning)
		form.Set("tr_lower", apiId.newlower)
		form.Set("model", "1")
		_, err := http.PostForm(fmt.Sprintf("http://localhost:8080/api/%s", apiId.id), form)
		if err != nil {
			t.Errorf("error sending POST to server: %s", err.Error())
		}
	}
	time.Sleep(5 * time.Second)
}

func TestPostUpdateDashBoard(t *testing.T)  { dashboarding(t, true) }
func TestPostUpdateParams(t *testing.T)     { parametring(t, true) }
func TestPostUpdateForecast(t *testing.T)   { forecasting(t, true) }
func TestPostUpdateThresholds(t *testing.T) { tresholding(t, true) }

func dashboarding(t *testing.T, post bool) {
	r, err := http.Get("http://localhost:8080/dashboard")
	if err != nil {
		t.Errorf(err.Error())
	}
	if r.StatusCode != 200 {
		t.Errorf("%s", r.StatusCode)
	}
	body, _ := ioutil.ReadAll(r.Body)
	if err != nil && err != io.EOF {
		t.Errorf(err.Error())
	}
	var item string
	for _, ts := range dashboardtests {
		if post {
			item = ts.new
		} else {
			item = ts.old
		}
		if !strings.Contains(string(body), fmt.Sprintf(item)) {
			t.Errorf("could not find %s from dashboard", item)
		}
	}
}

func forecasting(t *testing.T, post bool) {
	var data string
	for _, apiId := range forecasttests {
		if post {
			data = apiId.new
		} else {
			data = apiId.old
		}
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/api/%s", apiId.id))
		if err != nil {
			t.Errorf(err.Error())
			return
		}
		body, err := ioutil.ReadAll(r.Body)
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		if !strings.Contains(string(body), fmt.Sprintf(data)) {
			t.Errorf("wrong forecast for item %s", apiId.id)
		}
	}
}

func parametring(t *testing.T, post bool) {
	var params string
	for _, apiId := range basicupdatetests {
		if post {
			params = apiId.newparams
		} else {
			params = apiId.oldparams
		}
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/api/%s", apiId.id))
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		body, err := ioutil.ReadAll(r.Body)
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		if !strings.Contains(string(body), params) {
			t.Errorf("wrong params for item %s", apiId.id)
		}
	}
}

func tresholding(t *testing.T, post bool) {
	var high, critical, warning, lower string
	for _, apiId := range basicupdatetests {
		if post {
			high = apiId.newhigh
			critical = apiId.newcritical
			warning = apiId.newwarning
			lower = apiId.newlower
		} else {
			high = apiId.oldhigh
			critical = apiId.oldcritical
			warning = apiId.oldwarning
			lower = apiId.oldlower
		}
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/api/%s", apiId.id))
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		body, err := ioutil.ReadAll(r.Body)
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		tresbolding(t, body, "lower", lower, apiId.id)
		tresbolding(t, body, "high", high, apiId.id)
		tresbolding(t, body, "warning", warning, apiId.id)
		tresbolding(t, body, "critical", critical, apiId.id)

	}
}

func tresbolding(t *testing.T, body []byte, thing string, value string, id string) {
	if !strings.Contains(string(body), fmt.Sprintf("%s\":%s", thing, value)) {
		t.Errorf("wrong %s for item %s, should be %s", thing, id, value)
	}
}

func TestApi(t *testing.T) {
	client = &http.Client{
		CheckRedirect: nil,
	}
	req, err := http.NewRequest("Get", "http://localhost:8080/api/1", nil)
	if err != nil {
		t.Errorf(err.Error())
	}
	req.ParseForm()
	//	req.PostForm.Add("params", "{}")

	resp, err := client.Do(req)
	defer resp.Body.Close()
	body, _ := ioutil.ReadAll(resp.Body)

	req2, err := http.NewRequest("Get", "http://localhost:8080/api/1", nil)
	if err != nil {
		t.Errorf(err.Error())
	}
	req2.ParseForm()
	//	req2.PostForm.Add("params", "{}")

	resp2, err := client.Do(req2)
	defer resp2.Body.Close()
	body2, _ := ioutil.ReadAll(resp2.Body)

	if !bytes.Equal(body, body2) {
		t.Errorf("zoink!")
	}
}

func TestEnds(t *testing.T) {
	err := server.Process.Kill()
	if err != nil {
		t.Errorf(err.Error())
	}
}

func init() {
	//	uncomment the following to enable automatic reset of test database before testing
	//	this *will* cause problems with travis, so remember to undo/recomment!
	/*	dbinit := exec.Command("ssh", "ohtu@83.150.98.77",
		 "psql multi-axis-test -c 'drop schema public cascade;create schema public;';psql multi-axis-test < test.dump.sql")
		err := dbinit.Run()
		if err != nil {
			log.Fatal(err)
		} */
	exec.Command("go", "build", "-o", "test_bin", "zab2.go").Run()
	server = exec.Command("./test_bin", "-s", "multi-axis-test", "-h", "--config=config_test.yaml")
	err := server.Start()
	time.Sleep(5 * time.Second)
	if err != nil {
		log.Fatal(err)
	}
}

func checkRun() {
	if server.ProcessState.Exited() {
		server.Start()
		time.Sleep(1 * time.Second)
	}
}
