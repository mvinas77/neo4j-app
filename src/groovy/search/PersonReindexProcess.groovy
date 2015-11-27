package search
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import app.search.SearchService

class PersonReindexProcess implements Runnable {

    private static final Log log = LogFactory.getLog(PersonReindexProcess)

    SearchService searchService
    long batchId
    List idsToIndex
    public PersonReindexProcess(SearchService searchService, int batchID, List ids) {
        this.idsToIndex = ids
        this.batchId = batchID
        this.searchService = searchService
    }

    @Override
    void run() {

        long startTime = System.currentTimeMillis()
        searchService.index(idsToIndex)
        println("Total time to index batch ${batchId} was ${System.currentTimeMillis()-startTime}ms.")
        log.debug("Total time to index batch ${batchId} was ${System.currentTimeMillis()-startTime}ms.")

    }
}
