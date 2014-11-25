package main
 
import	(
	"net/http"
//	"fmt"
	"log"
	"testing"
	"os/exec"
	"time"
	"io/ioutil"
	"bytes"
//	"os"
//	"net/url"
)
 
var cmd *exec.Cmd
 
func TestDash(t *testing.T) {
	r, err := http.Get("http://localhost:8080/dashboard")
	if err != nil {
		t.Errorf("%s", err.Error)
	}
	if (r.StatusCode != 200) {
		t.Errorf("%s",r.StatusCode)
	}
}

func TestCpu(t *testing.T) {
	r, err := http.Get("http://localhost:8080/item/ohtu1/cpu")
	if err != nil {
		t.Errorf("%s", err.Error)
	}
	if (r.StatusCode != 200) {
		t.Errorf("%s",r.StatusCode)
	}
}

func TestApi(t *testing.T) {
	client := &http.Client{
		CheckRedirect: nil,
	}
	req, err := http.NewRequest("Get", "http://localhost:8080/api/1", nil)
	if err != nil {
		t.Errorf("%s", err.Error)
	}
	req.ParseForm()
	req.PostForm.Add("params", "{}")

	resp, err := client.Do(req)
	defer resp.Body.Close()
	body, _ := ioutil.ReadAll(resp.Body)



	req2, err := http.NewRequest("Get", "http://localhost:8080/api/1", nil)
	if err != nil {
		t.Errorf("%s", err.Error)
	}
	req2.ParseForm()
	req2.PostForm.Add("params", "{}")

	resp2, err := client.Do(req2)
	defer resp2.Body.Close()
	body2, _ := ioutil.ReadAll(resp2.Body)


	if (!bytes.Equal(body, body2)) {
		t.Errorf("zoink!")
	}
}

func TestWrong(t *testing.T) {
//	checkRun()
	r, err := http.Get("http://localhost:8080/")
	if err != nil {
		t.Errorf("%s", err.Error)
	}
	if (r.StatusCode != 404) {
		t.Errorf("%s",r.StatusCode)
	}
	cmd.Process.Kill()
}

func init() {
	cmd = exec.Command("zab2")
	err := cmd.Start()
	time.Sleep(1 * time.Second)
	if err != nil {
		log.Fatal(err)
	}
}

func checkRun() {
	if (cmd.ProcessState.Exited()) {
		cmd.Start()
		time.Sleep(1 * time.Second)
	}
}
