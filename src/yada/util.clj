(ns yada.util
  (:require
   [manifold.deferred :as d]
   clojure.core.async.impl.protocols)
  (:import [clojure.core.async.impl.protocols ReadPort]))

(defmacro link [ctx body]
  `(fn [~ctx] (or ~body ~ctx)))

;; Old comment :-
;; If this is something we can take from, in the core.async
;; sense, then call body again. We need this clause here
;; because: (satisfies? d/Deferrable (a/chan)) => true, so
;; (deferrable?  (a/chan) is (consequently) true too.

(defn deferrable?
  "An alternative version of deferrable that discounts
  ReadPort. Otherwise, core.async channels are considered as streams
  rather than values, which isn't what we want."
  [o]
  (and o
       (not (instance? ReadPort o))
       (d/deferrable? o)))


;; ------------------------------------------------------------------------
;; XML Parsing Transducers

(def children (mapcat :content))

(defn tagp [pred]
  (comp children (filter (comp pred :tag))))

(defn tag= [tag]
  (tagp (partial = tag)))

(defn attr-accessor [a]
  (comp a :attrs))

(defn attrp [a pred]
  (filter (comp pred (attr-accessor a))))

(defn attr= [a v]
  (attrp a (partial = v)))

(def text (comp (mapcat :content) (filter string?)))


;; Parsing

(def http-token #"[!#$%&'*+-\.\^_`|~\p{Alnum}]+")


;; Coercions

(defprotocol SetCoercion
  (to-set [_] "Coerce to a set, useful for a shorthand when specifying
  representation entries, which must always be coerced to sets."))

(extend-protocol SetCoercion
  java.util.Set
  (to-set [s] s)
  clojure.lang.Sequential
  (to-set [s] (set s))
  Object
  (to-set [o] #{o})
  nil
  (to-set [_] nil))

(defprotocol ListCoercion
  (to-list [_] "Coerce to a list, useful for a shorthand when specifying
  representation entries where ordering is relevant (languages)"))

(extend-protocol ListCoercion
  clojure.lang.Sequential
  (to-list [s] s)
  Object
  (to-list [o] [o])
  nil
  (to-list [_] nil))
