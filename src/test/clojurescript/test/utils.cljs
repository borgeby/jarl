(ns test.utils
  (:require [cljs.test]
            [jarl.exceptions]
            [jarl.builtins.registry])
  (:require-macros [test.utils]))

; Only here to suppress IntelliJ "can't resolve" warning, which is otherwise present in tests
(declare testing-builtin)
