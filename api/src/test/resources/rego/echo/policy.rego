package test

# METADATA
# entrypoint: true
echo := out {
	out := {
		"input": input,
		"data.attributes": data.attributes,
	}
} else := out {
	out := {"data.attributes": data.attributes}
} else := out {
	out := {"input": input}
} else := {}
