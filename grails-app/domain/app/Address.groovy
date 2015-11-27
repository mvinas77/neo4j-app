package app

class Address {

    static mapWith = "neo4j"

    String street
    Integer number
    String city
    String zip

    static constraints = {

        city(nullable: false)
        number(nullable: false)
        street(nullable: false)
        zip(nullable: false)
    }
}
