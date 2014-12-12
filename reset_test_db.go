package main

import 	(
	"os/exec"
	"log"
)

func main() {
	dbinit := exec.Command("ssh", "ohtu@83.150.98.77",
	 "psql multi-axis-test -c 'drop schema public cascade;create schema public;';psql multi-axis-test < test.dump.sql")
	err := dbinit.Run()	
	if err != nil {
		log.Fatal(err)
	}
}
