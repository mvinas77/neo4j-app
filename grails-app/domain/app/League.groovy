package app

class League extends AbstractGraphDomain {

	String name 
	Date dateCreated
    Date lastUpdated

	List<Club> clubs = []

	static hasMany = [clubs: Club, tags: Tag]

	static constraints = {
		name blank: false, unique: true
	}

	public String toString() {

     return 'id: ' + id + ' League: ' + name
 }
}