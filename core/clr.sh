#!/usr/bin/env bash

echo "Preparing lib directory"

M2="$HOME/.m2/repository"
LIB="target/clr/lib"

mkdir -p "$LIB"

unzip -q -o "$M2/org/clojure/clr/data.json/2.4.0/data.json-2.4.0.jar" -d "$LIB"
unzip -q -o "$M2/by/borge/clj-json-pointer/1.0.0/clj-json-pointer-1.0.0.jar" -d "$LIB"

echo "Done!"

CLOJURE_LOAD_PATH="src/main/cljr:src/main/cljc:$LIB" \
  Clojure.Main -m jarl.core-clr
