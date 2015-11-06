package app

class Tag {
	
	static mapWith = "neo4j"

	String name

	static constraints = {
		name blank: false, unique: true
	}
}