package search

import org.apache.solr.client.solrj.SolrQuery

import grails.util.Holders

import app.search.DateRangeFacetConfig
import app.search.FacetDefinition
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

            if (it.config) {
                addFacet(query, it, it.config)
            }
            else {
                query.addFacetField(it.solrField)
            }
        }

        if (paging?.limit) {
            query.setRows(paging.limit)
        }

        if (paging?.skip) {
            query.setStart(paging.skip)
        }

        return query
    }

    private void addFacet(SolrQuery query, FacetDefinition facetDefinition, DateRangeFacetConfig config) {

        String gap
        String sign = config.increment > 0 ? '+' : ''

        switch (config.unit) {
            case DateRangeUnit.DAY:

                gap = Math.abs(config.increment) > 1 ? "${sign}${config.increment}DAYS" : "${sign}${config.increment}DAY"
                break
            case DateRangeUnit.WEEK:

                gap = Math.abs(config.increment) > 1 ? "${sign}${config.increment}WEEKS" : "${sign}${config.increment}WEEK"
                break
            case DateRangeUnit.MONTH:

                gap = Math.abs(config.increment) > 1 ? "${sign}${config.increment}MONTHS" : "${sign}${config.increment}MONTH"
                break
            case DateRangeUnit.QUARTER:
                int increment = config.increment * 4
                gap = Math.abs(increment) > 1 ? "${sign}${increment}MONTHS" : "${sign}${increment}MONTH"
                break
            case DateRangeUnit.SEMESTER:
                int increment = config.increment * 6
                gap = Math.abs(increment) > 1 ? "${sign}${increment}MONTHS" : "${sign}${increment}MONTH"
                break
            case DateRangeUnit.YEAR:

                gap = Math.abs(config.increment) > 1 ? "${sign}${config.increment}YEARS" : "${sign}${config.increment}YEAR"
                break
        }

        query.addDateRangeFacet(facetDefinition.solrField, config.rangeStart, config.rangeEnd, gap)
    }

}
