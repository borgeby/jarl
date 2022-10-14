package agg

p[x] {
	z = [y | q[y]]
	max(z, x)
}

q[x] {
	x := data.a[_]
}

r {
	x = data.a[3]
	not max([1, 2, 3, 4, 5], x)
}
