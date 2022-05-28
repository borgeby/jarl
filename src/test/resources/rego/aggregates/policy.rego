package agg

p[x] {
	z = [y | q[y]]
	max(z, x)
}

q[x] {
	x := data.a[_]
}
