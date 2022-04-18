package object_comprehension

x := ["a", "b", "c"]

p = {i: v | v := x[i]}
