package search

enum DateRangeUnit {

    DAY(Calendar.DATE),
    WEEK(Calendar.WEEK_OF_YEAR),
    MONTH(Calendar.MONTH),
    QUARTER(Calendar.MONTH),
    SEMESTER(Calendar.MONTH),
    YEAR(Calendar.YEAR)

    public int field

    public DateRangeUnit(int field) {

        this.field = field
    }

}
