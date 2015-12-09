package app.search

import grails.transaction.Transactional

import search.Paging
import search.SearchCommand

@Transactional
class SearchController {

    def searchService

    def index(String q, String type, Paging paging) {
        def result = searchService.search(q, type, paging)

        render(view: 'index', model: [result: result])
    }


    def searchSolr(SearchCommand command) {
        def result = searchService.search(q, type, paging)

        render(view: 'index', model: [result: result])
    }
}
