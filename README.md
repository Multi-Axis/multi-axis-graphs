multi-axis-graphs
=================

Server metrics on multi-axis graphs.

<http://multi-axis.github.io/>

new-dashboard
-------------

The new dashboard javascript is checked out in git, `static/new-dashboard.js`,
so it works out of the box with just the webserver set up.

To develop it:

```sh
cabal install purescript-0.6.8 # or later
pacman -S npm
npm install pulp
cd new-dashboard
pulp dep install

# try
pulp build
pulp run

# deploy
pulp browserify --to ../static/new-dashboard.js
```
