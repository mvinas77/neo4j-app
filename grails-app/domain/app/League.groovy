package app

class League {
	
	static mapWith = "neo4j"

	String name 
	Date dateCreated
    Date lastUpdated

	List<Club> clubs = []

	static hasMany = [clubs: Club, tags: Tag]

	static constraints = {
		name blank: false, unique: true
	}

	//static mapping = {
	//	tags fetch: "eager"
	//}
}