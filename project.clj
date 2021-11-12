(defproject hazelcast-atom "0.1.0-SNAPSHOT"
  :description "Distributed atom implementation on top of hazelcast IMap"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.hazelcast/hazelcast "4.0.2"]]
  :repl-options {:init-ns hazelcast-atom.core})
