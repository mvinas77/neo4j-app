package search

import grails.validation.Validateable

@Validateable
class Paging {

    Integer skip
    Integer limit
}
