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
	{"kurpitsa", "ohtu1"},
	{"ohtu1", "ohtu1"},
}

var graphtests = []struct {
	item string
}{
	{"cpu"},
	{"mem"},
	{"swap"},
}

var basicupdatetests = []struct {
	id           string
	oldthreshold string
	newthreshold string
	oldcritical  string
	newcritical  string
	oldwarning   string
	newwarning   string
	oldlower     string
	newlower     string
	oldparams    string
	newparams    string
}{
	{"1",
		"10.000000", "5.000000",
		"10.000000", "5.000000",
		"10.000000", "5.000000",
		"false", "true",
		"params\":{\"stop_lower\":1414380539,\"pre_filter\":\"DailyMax",
		"params\":{\"stop_lower\":1414380539,\"pre_filter\":\"DailyMax",
	},
	{"6",
		"85.000000", "95.000000",
		"85.000000", "95.000000",
		"85.000000", "95.000000",
		"true", "false",
		"params\":{\"pre_filter\":\"DailyMax\",\"stop_lower\":1411951812,\"stop_upper\":1414023544",
		"params\":{\"pre_filter\":\"DailyMax\",\"stop_lower\":1411951812,\"stop_upper\":1414023544",
	},
	{"8",
		"900000000.000000", "500000000.000000",
		"900000000.000000", "500000000.000000",
		"900000000.000000", "500000000.000000",
		"true", "false",
		"params\":{\"stop_lower\":1413898327,\"stop_upper\":1416125574",
		"params\":{\"stop_lower\":1413898327,\"stop_upper\":1416125574",
	},
}

var forecasttests = []struct {
	id  string
	old string
	new string
}{
	{"1", "ohtu1", "ohtu1"},
	{"6", "kurpitsa", "ohtu1"},
	{"8", "ohtu1", "koink1"},
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
	err := server.Process.Kill()
	if err != nil {
		t.Errorf(err.Error())
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
		form.Set("threshold", apiId.newthreshold)
		form.Set("critical", apiId.newcritical)
		form.Set("warning", apiId.newwarning)
		form.Set("threshold_type", apiId.newlower)
		form.Set("model", "1")
		_, err := http.PostForm(fmt.Sprintf("http://localhost:8080/api/%s", apiId.id), form)
		if err != nil {
			t.Errorf("error sending POST to server: %s", err.Error())
		}
	}
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
	var threshold, critical, warning, lower string
	for _, apiId := range basicupdatetests {
		if post {
			threshold = apiId.newthreshold
			critical = apiId.newcritical
			warning = apiId.newwarning
			lower = apiId.newlower
		} else {
			threshold = apiId.oldthreshold
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
		tresbolding(t, body, "threshold", threshold, apiId.id)
		tresbolding(t, body, "critical", critical, apiId.id)
		tresbolding(t, body, "warning", warning, apiId.id)
		tresbolding(t, body, "lower", lower, apiId.id)
	}
}

func tresbolding(t *testing.T, body []byte, thing string, value string, id string) {
	if !strings.Contains(string(body), fmt.Sprintf("%s\":{ \"value\":%s", thing, value)) {
		t.Errorf("wrong %s for item %s", thing, id)
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

func init() {
	dbinit := exec.Command("ssh", "ohtu@83.150.98.77",
		"psql multi-axis-test -c 'drop schema public cascade;create schema public;';psql multi-axis-test < test.dump.sql")
	err := dbinit.Run()
	if err != nil {
		log.Fatal(err)
	}
	exec.Command("go", "build", "-o", "test_bin", "zab2.go").Run()
	server = exec.Command("test_bin", "-s", "multi-axis-test", "-h", "--config=config_test.yaml")
	err = server.Start()
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
