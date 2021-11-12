# hazelcast-atom

A Clojure library implementing distributed atoms on top of hazelcast.

## Usage

```clojure
(ns hazelcast-atom.example
  (:require [hazelcast-atom.core :as hza]))

(def hazelcast-instance
  (com.hazelcast.core.Hazelcast/newHazelcastInstance hazelcast-config))

(def initial-state {:foo 42})

(def shared-atom
  (hza/hz-atom hazelcast-instance :my-shared-atom initial-state))

;; on node1:
(swap! shared-atom update :foo inc)

;; on node2:
(swap! shared-atom assoc :bar 42)

;; on either node
@shared-atom ;; => {:foo 43, :bar 42}
```

## License

Copyright © 2021 Alesya Huzik

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
