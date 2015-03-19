module Main where

import Network.Ajax
import Control.Monad.Trans
import Control.Monad.Cont.Trans
import Control.Monad.Eff
import Data.Either
import Data.Maybe
import Data.Foldable
import Data.List (List())
import Data.Array
import DOM

import Text.Smolder.HTML (h1)
import Text.Smolder.Markup
import Text.Smolder.Renderer.String (render)

import Debug.Trace

main = do
   runContT getCont print

getCont = do
  res <- ajaxCont "habbix/dashboard" $ HttpRequest {accepts: Json, contentType: TextContent, method: GET, contents: Nothing}
  handleContent res
  where
    handleContent (Left err)  = return "request error"
    handleContent (Right res) = do
        lift $ asDashboard $ render $ dashboardView res
        return ""

type Host = { hostname :: String, hostid :: Number, items :: [Item] }

-- instance hostIsForeign :: IsForeign Host where
--     read value = do
--         h   <- readProp "hostname" value
--         hid <- readProp "hostid" value
--         return $ Host { hostname : h, hostid : hid, items : [] }
-- 
type Item = 
          { metric_name :: String
          , current_value :: Number
          , current_time :: Number
          , next24h :: Number
          , next6d :: Number
          , past7d :: Number
          }

dashboardView :: [Host] -> Markup
dashboardView hosts = do
    for_ hosts $ \host -> do
        h1 $ text (host.hostname)

foreign import asDashboard
    "function asDashboard(html) {\
    \   document.getElementById(\"dashboard\").innerHTML = html;\
    \}" :: forall eff. String -> Eff (dom :: DOM | eff) Unit
