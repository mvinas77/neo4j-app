package app

import grails.persistence.Entity

@Entity
class Club extends AbstractGraphDomain {

    //static mapWith = "neo4j"

    String name
    Date dateCreated
    Date lastUpdated
    String big

    static belongsTo = [league: League]

    static hasMany = [players: Player]

    static constraints = {
        name blank: false, unique: true
    }

    static transients = ['big']

    public transient String getBig() {
        return 'bigtext' + name
    }

    public transient void setBig(String big) {
        this.big = big
    }

}