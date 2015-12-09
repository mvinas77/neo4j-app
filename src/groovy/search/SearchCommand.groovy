package search

import grails.validation.Validateable

import sun.misc.Sort

@Validateable
class SearchCommand {

    String q
    Paging paging
    Sort sort
    String filters

    public SearchCommand() {

    }

    public Collection<Filter> getFilters() {

        Collection<Filter> filters = []

        return filters
    }

}
