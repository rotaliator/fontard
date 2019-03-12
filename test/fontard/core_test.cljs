(ns fontard.core-test
  (:require
   [cljs.test :refer-macros [deftest is testing]]
   [fontard.core :refer [get-matrix]]))

(deftest get-matrix-test
  (is (= (get-matrix "0x20, 0x12, 0x0A, 0x06, 0x1E,")
         ["00000100" "01001000" "01010000" "01100000" "01111000"])))
