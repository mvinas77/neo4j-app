package app

abstract class Nationality extends AbstractGraphDomain {

    static mapWith = "none"

    String name

    static constraints = {
    }

    public String toString() {

        return 'id' + id + 'Nationality: ' + name
    }
}
