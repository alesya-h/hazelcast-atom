(ns hazelcast-atom.core
  (:import [clojure.lang IRef IAtom]
           [com.hazelcast.core IMap EntryAdapter]
           [com.hazelcast.map.listener MapListener]))

(defn swap-hz-atom [hz-atom f]
  (let [old-val @hz-atom
        new-val (f old-val)]
    (if (.compareAndSet hz-atom old-val new-val)
      new-val
      (recur hz-atom f))))

(defn validate [vf val]
  (try
    (if (and (not (nil? vf))
             (not (vf val)))
      (throw (IllegalStateException. "Invalid reference state")))
    (catch RuntimeException re
      (throw re))
    (catch Exception e
      (throw (IllegalStateException. "Invalid reference state" e)))))

(def hz-map-atom-key "atom")

(defrecord HazelcastAtom [^IMap hz-map
                          watches-atom
                          validator-atom]
  IRef
  (deref [this] (.get hz-map hz-map-atom-key))
  (getValidator [this] @validator-atom)
  (setValidator [this new-validator] (reset! validator-atom new-validator))
  (getWatches [this] @watches-atom)
  (addWatch [this key callback] (swap! watches-atom assoc key callback) this)
  (removeWatch [this key] (swap! watches-atom dissoc key) this)

  IAtom
  (swap [this f]          (swap-hz-atom this #(f %)))
  (swap [this f x]        (swap-hz-atom this #(f % x)))
  (swap [this f x y]      (swap-hz-atom this #(f % x y)))
  (swap [this f x y args] (swap-hz-atom this #(apply f % x y args)))
  (compareAndSet [this old-val new-val]
    (validate @validator-atom new-val)
    (let [ret (.replace hz-map hz-map-atom-key old-val new-val)]
      ret))
  (reset [this new-val]
    (let [old-val @this]
      (validate @validator-atom val)
      (.set hz-map hz-map-atom-key new-val)
      new-val)))

(defmethod print-method HazelcastAtom [v ^java.io.Writer w]
  (.write w (format "#hz-atom[%s 0x%x]" @v (.hashCode @v))))

(defn make-map-listener [the-hz-atom]
  (proxy [EntryAdapter] []
    (onEntryEvent [evt]
      (doseq [[k watcher-fn] (.getWatches the-hz-atom)]
        (watcher-fn k the-hz-atom
                    (.getOldValue evt) (.getValue evt))))))

(defn hz-atom [hz atom-name initial-value]
  (let [hz-map (.getMap hz (name atom-name))
        the-hz-atom (HazelcastAtom. hz-map (atom {}) (atom nil))]
    (.addEntryListener hz-map
                       (make-map-listener the-hz-atom)
                       hz-map-atom-key true)
    (.putIfAbsent hz-map hz-map-atom-key initial-value)
    the-hz-atom))

