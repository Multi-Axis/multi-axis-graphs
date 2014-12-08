package main
 
import	(
	"net/http"
	"net/url"
//	"fmt"
	"log"
	"testing"
	"os/exec"
	"time"
	"io/ioutil"
	"bytes"
	"strings"
	"fmt"
	"io"
//	"os"
//	"net/url"
)
 
var server *exec.Cmd
var client *http.Client 
 
func TestDashBoard(t *testing.T) {
	r, err := http.Get("http://localhost:8080/dashboard")
	if err != nil {
		t.Errorf(err.Error())
	}
	if (r.StatusCode != 200) {
		t.Errorf("%s",r.StatusCode)
	}
	body, _ := ioutil.ReadAll(r.Body)
	if (!strings.Contains(string(body),"ohtu1")) {
		t.Errorf("%s","yarr no ohtu1")
	}
}

var graphtests = []struct {
	item string
}{
	{"cpu"},
	{"mem"},
	{"swap"},
}

func TestItems(t *testing.T) {
	for _,item := range graphtests {
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/item/ohtu1/%s",item.item))
		if err != nil {
			t.Errorf(err.Error())
		}
		if (r.StatusCode != 200) {
			t.Errorf("could not find item: %s, statuscode:%d",item.item,r.StatusCode)
		}
	}
}

var updatetests = []struct {
	id	string
	oldthreshold string
	oldlower string
	newthreshold string
	newlower string
	params string
}{
	{"1","10.000000","false", "5.000000","true",
	"params\":{\"stop_lower\":1414380539,\"pre_filter\":\"DailyMax"},
	{"6","85.000000","true","95.000000","false",
	"params\":{\"pre_filter\":\"DailyMax\",\"stop_lower\":1411951812,\"stop_upper\":1414023544"},
	{"8","900000000.000000","true","500000000.000000","false",
	"params\":{\"stop_lower\":1413898327,\"stop_upper\":1416125574"},
}

func TestParams(t *testing.T) {
	for _,apiId := range updatetests {
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/api/%s",apiId.id))
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		body, err := ioutil.ReadAll(r.Body)
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		if (!strings.Contains(string(body), apiId.params )) {
			t.Errorf("wrong params, should be: %s",apiId.params)
		}
	}
}

func TestThresholds(t *testing.T) {
	for _,apiId := range updatetests {
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/api/%s",apiId.id))
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		body, err := ioutil.ReadAll(r.Body)
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		if (!strings.Contains(string(body), fmt.Sprintf("threshold\":{ \"value\":%s",apiId.oldthreshold))) {
			t.Errorf("%s","wrong threshold")
		}
		if (!strings.Contains(string(body), fmt.Sprintf("lower\":%s }",apiId.oldlower))) {
			t.Errorf("%s","wrong  lower")
		}
	}
}



func TestUpdates(t *testing.T) {
	for _,apiId := range updatetests {
	//	req, err := http.NewRequest("POST", "http://localhost:8080/api/1", nil)
	//	if err != nil {
	//		t.Errorf("%s", err.Error)
	//	}
	//	req.ParseForm()
	//	req.PostForm.Add("params", "{\"test\":1234}")
	//	req.PostForm.Add("threshold", "5")
	//	req.PostForm.Add("threshold_type", "f")
	//	resp, err := client.Do(req)
		form := url.Values{}	
		form.Set("params", fmt.Sprintf("{\"test\":%s}",apiId.id))
		form.Set("threshold", apiId.newthreshold)
		form.Set("threshold_type", apiId.newlower)
		//_, err := 
		http.PostForm(fmt.Sprintf("http://localhost:8080/api/%s", apiId.id), form)	
	//	defer resp.Body.Close()
		// if (err != nil) {
			// if err != io.EOF {
                // t.Errorf(err.Error())
				// return
            // }           
		// }
		r, err := http.Get(fmt.Sprintf("http://localhost:8080/api/%s",apiId.id))
		if err != nil {
			t.Errorf(err.Error())
			return
		}
		body, err := ioutil.ReadAll(r.Body)
		if err != nil && err != io.EOF {
			t.Errorf(err.Error())
		}
		if (!strings.Contains(string(body), fmt.Sprintf("threshold\":{ \"value\":%s",apiId.newthreshold))) {
			t.Errorf("%s","wrong threshold")
		}
		if (!strings.Contains(string(body), fmt.Sprintf("lower\":%s }",apiId.newlower))) {
			t.Errorf("%s","wrong  lower")
		}
		if (!strings.Contains(string(body), fmt.Sprintf("params\":%s",fmt.Sprintf("{\"test\":%s}",apiId.id)))) {
			t.Errorf("%s","wrong params")
		}
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


	if (!bytes.Equal(body, body2)) {
		t.Errorf("zoink!")
	}
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

func TestWrong(t *testing.T) {
	for _,wrong := range wrongurl {
		r, err := http.Get(wrong.url)
		if err != nil {
			t.Errorf(err.Error())
		}
		if (r.StatusCode != 404) {
			t.Errorf("found url:%s with statuscode:%s",wrong.url,r.StatusCode)
		}
		err = server.Process.Kill()
		if err != nil {
			t.Errorf(err.Error())
		}
	}
}

func init() {
	dbinit := exec.Command("ssh", "ohtu@83.150.98.77",
	 "psql multi-axis-test -c 'drop schema public cascade;create schema public;';psql multi-axis-test < test.dump.sql")
	err := dbinit.Run()	
	if err != nil {
		log.Fatal(err)
	}
	server = exec.Command("go","run","zab2.go","-s","multi-axis-test","-h","--config=config_test.yaml")
	err = server.Start()
	time.Sleep(5 * time.Second)
	if err != nil {
		log.Fatal(err)
	}
}

func checkRun() {
	if (server.ProcessState.Exited()) {
		server.Start()
		time.Sleep(1 * time.Second)
	}
}
