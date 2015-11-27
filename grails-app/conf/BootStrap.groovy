import app.Club
import search.SearchGormListener

class BootStrap {

    def grailsApplication
    def searchService
    def uefaService

    def init = { servletContext ->
        searchService.initialize()


        def applicationContext = grailsApplication.mainContext
        def neo4jDataStore = applicationContext.getBean('neo4jDatastore')
        def searchService = applicationContext.getBean('searchService')
        def grailsApplication = applicationContext.getBean('grailsApplication')
        def config = grailsApplication.config
        applicationContext.addApplicationListener(new SearchGormListener(neo4jDataStore, searchService, config))

        uefaService.initialize()
    }

    def destroy = {
        destroyUEFA()
    }


    private void destroyUEFA() {
        Club.where {}.deleteAll()
    }

}