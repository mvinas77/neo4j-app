package app.search

import grails.transaction.Transactional

import app.Person

class CreatePersonsController {

    SearchService searchService
    PersonsService personsService

    def createPersons() {

        personsService.createPersons(1000)

        render 'ok'
    }

    @Transactional
    def count() {

        render Person.count
    }


    def reindex() {
        long startTime = System.currentTimeMillis()
        personsService.reindex()
        String message = "Reindex done in ${System.currentTimeMillis() - startTime}ms."
        log.debug(message)
        render message
    }

    def commit() {
        long startTime = System.currentTimeMillis()
        searchService.commit()
        String message = "Commit time was ${System.currentTimeMillis() - startTime}ms."
        log.debug(message)
        render message
    }
}
