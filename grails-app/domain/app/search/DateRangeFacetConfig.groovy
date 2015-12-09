package app.search

import search.DateRangeUnit

class DateRangeFacetConfig extends FacetConfig {

    Date rangeStart
    Date rangeEnd
    Integer increment
    DateRangeUnit unit

    static constraints = {
        rangeStart(nullable: false)
        rangeEnd(nullable: false)
        increment(nullable: false)
        unit(nullable: false)
    }
}
