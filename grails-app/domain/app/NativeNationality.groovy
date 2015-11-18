package app

class NativeNationality extends Nationality {

    static mapWith = "neo4j"

    static constraints = {
    }

    public String toString() {

        return 'id' + id + 'NativeNationality: ' + name
    }
}
