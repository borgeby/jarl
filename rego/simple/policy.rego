package simple

x := 42

y := null

p {
	input.x = x
}

p {
	input.x = 1337
}

q = "foo" {
	not y == null
} else = "bar" {
	true
}
