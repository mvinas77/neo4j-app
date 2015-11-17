package search

import org.apache.solr.client.solrj.SolrQuery

import grails.util.Holders

import app.search.SearchService

class SolrQueryBuilder {

    private String textToSearch
    private String type
    private Paging paging

    public SolrQueryBuilder setTextToSearch(String text) {

        this.textToSearch = text

        return this
    }

    public SolrQueryBuilder setType(String type) {

        this.type = type

        return this
    }

    public SolrQueryBuilder setPaging(Paging paging) {

        this.paging = paging

        return this
    }

    public SolrQuery build() {
        Class typeClass = null
        SolrQuery query = new SolrQuery()
        query.setQuery(textToSearch ?: '*')

        if (type) {
            typeClass = getClass().getClassLoader().loadClass(type)
            query.setFilterQueries("__type__:${type}") // Filter by type
        }

        SearchService searchService = Holders.applicationContext.getBean('searchService')

        searchService.getFacets(typeClass).each {
            query.addFacetField(it.solrField)
        }

        if (paging?.limit) {
            query.setRows(paging.limit)
        }

        if (paging?.skip) {
            query.setStart(paging.skip)
        }

        return query
    }
}
