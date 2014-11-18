# Getting Started

To get the database setup, simply configure `config.yaml` from habbix, possibly
create the database (`createdb -O multi-axis multiaxis`) and then run `habbix
migrate-db`.

Next you wil want to sync local "meta" (that is, all but history tables) tables
from zabbix: `habbix sync-db -s`.

The history tables are rather large (over 300k rows *with a single server*), so
it is not wise to sync all of them.

Now, the following may sound a bit confusing but please bear with me.  Because
**history is coupled with future**, you will need to define some means to
predict the future before syncing the history.  That is, some
("future-predicting!") executable(s) in `$PWD/forecast_models`. For now you may
want to copy or link the `linreg` binary from habbix:
        
     mkdir future_models
     cd future_models
     ln -s ~/.cabal/bin/linreg .

Then register it in the database: `habbix add-model linreg`.  Refer to section
"Predictions" for more info on models.

Now you can register some items in the database. To see what items have any
history, you can e.g. do something like this on the **remote zabbix db**:

    zabbix=> select distinct itemid from history;
    zabbix=> select distinct itemid from history_uint;

When you now the itemid you want to add (say, 20000), just say

    habbix new-future 20000 1

The 1 specifies the model to use (see `habbix ls-models`).

Now you can synchronize the history from the remote db and predict the future:

    habbix sync-db

(That syncs only histories and futures; if you have added new hosts, you will
need to run habbix `sync-db -s` first.)

## Optimizations

    create index history_idx on history (itemid, clock);
    create index trend_idx on trend (itemid, clock);
    alter table trend drop column id;
    alter table history drop column id;
