package app.search

class FacetDefinition {

    static mapWith = "neo4j"

    String name
    String solrField
    String facetField
    String facetProperty
    String type
    FacetConfig config

    static constraints = {
        facetField(nullable: false)
        facetProperty(nullable: true)
        name(nullable: false)
        solrField(nullable: false)
        type(nullable: false)
        config(nullable: true)
    }
}
