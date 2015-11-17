package app.search

import grails.converters.JSON
import grails.transaction.Transactional

import search.Paging

@Transactional
class SearchController {

    def searchService

    def index(String q, String type, Paging paging) {
        def result = searchService.search(q, type, paging)

        render result as JSON
    }

}
