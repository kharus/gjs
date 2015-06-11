(defproject gjs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.googlecode.windowlicker/windowlicker-swing "r268"]
                 [junit/junit "4.12"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.igniterealtime.smack/smack-tcp "4.1.1"]
                 [org.igniterealtime.smack/smack-java7 "4.1.1"]
                 [org.igniterealtime.smack/smack-sasl-provided "4.1.1"]
                 [org.igniterealtime.smack/smack-im "4.1.1"]]
  :main ^:skip-aot gjs.core
  :aot [gjs.AuctionSniperDriver]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
