# Backend server

This server (written in Go) handles passing data to and from html/javascript [front end](https://github.com/Multi-Axis/multi-axis-graphs/wiki/Server-view-front-end-notes) and dashboard to the [Habbix back end](https://github.com/Multi-Axis/multi-axis-graphs/blob/master/Doc/Habbix.md) and the [multi-axis database](https://github.com/Multi-Axis/multi-axis-graphs/blob/master/Doc/DatabaseAndData.md).

Server [source code.](https://github.com/Multi-Axis/multi-axis-graphs/blob/master/zab2.go)

Using `deploy.sh` to deploy the server is recommended. Compiling  requires [this postgres driver](https://github.com/lib/pq). 

* URL:s ending with `[ip]:8080/dashboard` will lead to dashboard. 
* `[ip]:8080/static/` is used to deliver javascript and css files.
* `[ip]:8080/item/:server/:item` (for example, item/ohtu1/cpu) leads to specific graphs for items, and is also currently (somewhat stupidly) used to deliver JSON files which has some "amusing" side effects with client-side web caching. 
* Incorrect URL:s will result in 404 errors.

The server is *probably* protected against typical sql injection attacks, but will happily send all kinds of wacky stuff from front end's "Model details" field to the forecast models, so be warned.

Currently most errors are handled elegantly by `log.Fatal(err)`, e.g. server dies on purpose if something goes wrong.
