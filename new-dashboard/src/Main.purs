module Main where

import Network.Ajax
import Control.Monad.Trans
import Control.Monad.Cont.Trans
import Control.Monad.Eff
import Data.Either
import Data.Maybe
import Data.Foldable
import Data.List (List())
import DOM
import Data.Foreign.Undefined

import Text.Smolder.HTML (table, tr, th, td, a, h2, div)
import Text.Smolder.HTML.Attributes (href, className, title)
import Text.Smolder.Markup
import Text.Smolder.Renderer.String (render)

import Debug.Trace

main = do
   runContT getCont trace

getCont = do
  res <- ajaxCont "habbix/dashboard" $ HttpRequest {accepts: Json, contentType: TextContent, method: GET, contents: Nothing}
  handleContent res
  where
    handleContent (Left err)  = return "request error"
    handleContent (Right res) = do
        lift $ asDashboard $ render $ dashboardView res
        return ""

type Host = { hostname :: String, hostid :: Number, items :: [Item] }
type Item = { metric_name :: String, metric_scale :: Number
            , current_value :: Number, current_time :: Number
            , next24h :: Number, next6d :: Number, past7d :: Number
            , threshold_lower :: Boolean
            , threshold_high :: Number, threshold_warning :: Number, threshold_critical :: Number }

dashboardView :: [Host] -> Markup
dashboardView hosts =
    div (for_ hosts $ \h -> div (hostView h) ! className "host-block") ! className "new-dashboard"
  where
    hostView host = do
        h2 $ text (host.hostname)
        table $ do
            -- tr $ do
            --     th (text "")
            --     th (text "past 7d")
            --     th (text "now")
            --     th (text "next 24h")
            --     th (text "next 6d")
            for_ (host.items) (tr <<< itemView host.hostname)

itemView :: String -> Item -> Markup
itemView hn i
    | i.current_time < 0 = do
        td $ text i.metric_name
        td $ text "(n/a)"
    | otherwise          = do

        th $ a (text i.metric_name)
            ! href ("/item/" <> hn <> "/" <> i.metric_name)
            ! title ("threshold (" <> (if i.threshold_lower then "_): " else "^): ")
                    <> metricPP i i.threshold_critical <> "/" <> metricPP i i.threshold_warning <> "/" <> metricPP i i.threshold_high
                    <> ", scale " <> show i.metric_scale)

        let pp v = td (text (metricPP i v)) ! className (thresholdClassName i v)
        pp i.past7d
        pp i.current_value
        pp i.next24h
        pp i.next6d

metricPP :: Item -> Number -> String
metricPP i v = show $ sigFigs (v / i.metric_scale) 2

thresholdClassName :: Item -> Number -> String
thresholdClassName i n = go
    where cmp v t                         = if i.threshold_lower then v <= t else v >= t
          go | cmp n i.threshold_critical = "critical"
             | cmp n i.threshold_high     = "high"
             | cmp n i.threshold_warning  = "warning"
             | otherwise                  = "normal"


foreign import asDashboard
    "function asDashboard(html) {\
    \   return function(){\
    \      document.getElementById(\"dashboard\").innerHTML = html;\
    \   }\
    \}" :: forall eff. String -> Eff (dom :: DOM | eff) Unit

foreign import sigFigs
    "function sigFigs(n) {\
    \   return function(sig) {\
    \     if (n == 0) return 0;\
    \     var mult = Math.pow(10, sig - Math.floor(Math.log(Math.abs(n)) / Math.LN10) - 1);\
    \     console.log(n, sig, mult);\
    \     return Math.round(n * mult) / mult;\
    \   }\
    \}" :: Number -> Number -> Number
