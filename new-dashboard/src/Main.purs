module Main where

import Network.Ajax
import Control.Monad.Trans
import Control.Monad.Cont.Trans
import Control.Monad.Eff
import Data.Either
import Data.Maybe
import Data.Foldable
import Data.Monoid
import Data.List (List())
import DOM
import Data.Foreign.Undefined

import Text.Smolder.HTML (small, table, tr, th, td, a, h2, div, span)
import Text.Smolder.HTML.Attributes (href, className, title, colspan)
import Text.Smolder.Markup
import Text.Smolder.Renderer.String (render)

import Debug.Trace
import Data.Array (sortBy, map, length, null)
import Math (round, pow, log, abs, ln10, floor)

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

type Dashboard = { timestamp :: String, hosts :: [Host] }
type Host = { hostname :: String, hostid :: Number, items :: [Item] }
type Item = { metric_name :: String, metric_scale :: Number
            , current_value :: Number, current_time :: Number
            , next24h :: Number, next6d :: Number, past7d :: Number
            , threshold_lower :: Boolean
            , threshold_high :: Number, threshold_warning :: Number, threshold_critical :: Number }

type ScoredHost = { score :: Number, host :: Host }

dashboardView :: Dashboard -> Markup
dashboardView x = do
    div (legend x.timestamp) ! className "legend"
    div (for_ (orderHosts x.hosts) hostView) ! className "new-dashboard"

hostView :: ScoredHost -> Markup
hostView x
    | null x.host.items = mempty
    | otherwise = div theView ! className "host-block"
  where theView = do
            h2 hostNameView ! className (scoreClassName x.score)
            table $ do
                tr $ do
                    th (text "days")    ! className "tiny"
                    td (text "last 7 days") ! className "tiny"
                    td (text "now")     ! className "tiny"
                    td (text "tomorrow")  ! className "tiny"
                    td (text "next week")  ! className "tiny"
                for_ (x.host.items) (itemView x.host.hostname)

        hostNameView = do
            a (text $ x.host.hostname) ! href ("https://monitoring.relex.fi/host_screen.php?screenid=26&hostid=" <> show x.host.hostid)
            span (text $ show x.score) ! className ("right " <> scoreClassName x.score)

orderHosts :: [Host] -> [ScoredHost]
orderHosts xs = sortBy cmp $ map withCritScore xs
  where cmp a b = compare b.score a.score

withCritScore :: Host -> ScoredHost
withCritScore h = { score : if null h.items then 0 else round (sum (map itemScore h.items) / length h.items)
                  , host : h }
  where itemScore i = sum $ map (thresholdScore i) [i.current_value, i.next24h, i.next6d]

-- | Dashboard legend
legend :: String -> Markup
legend timestamp = table $ tr $ do
    th (text "item link")
    td (text "past 7d")
    td (text "now")
    td (text "next 24h")
    td (text "next 6d")
    td (text timestamp)

-- Item row (tr).
itemView :: String -> Item -> Markup
itemView hn i
    | i.current_time < 0 = tr $ td (text "metric n/a") ! colspan "5"
    | otherwise          = tr $ do
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
metricPP i v = show (sigFigs (v / i.metric_scale) 2) ++ scalePP i.metric_scale

scalePP 1073741824 = "G"
scalePP _ = ""

thresholdClassName :: Item -> Number -> String
thresholdClassName i n = scoreClassName (thresholdScore i n)

scoreClassName :: Number -> String
scoreClassName n
    | n == 0    = "normal"
    | n == 1    = "high"
    | n <= 3    = "warn"
    | n >= 4    = "critical"
    | otherwise = "unknown"

-- | item -> number to compare -> score
thresholdScore :: Item -> Number -> Number
thresholdScore i n = go
    where cmp v t                         = if i.threshold_lower then v <= t else v >= t
          go | cmp n i.threshold_critical = 4
             | cmp n i.threshold_warning  = 2
             | cmp n i.threshold_high     = 1
             | otherwise                  = 0

sigFigs :: Number -> Number -> Number
sigFigs n sig | n == 0 = 0
              | otherwise = let mult = pow 10 (sig - floor (log (abs n) / ln10) - 1)
                                in round (n * mult) / mult

-- * foreign stuff

foreign import asDashboard
    "function asDashboard(html) {\
    \   return function(){\
    \      document.getElementById(\"dashboard\").innerHTML = html;\
    \   }\
    \}" :: forall eff. String -> Eff (dom :: DOM | eff) Unit

