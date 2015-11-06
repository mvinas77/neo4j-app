package app

import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@Transactional
class LeagueController {

    def uefaService

    static responseFormats = ["json"]

    static allowedMethods = [save: "POST", update: "PUT", patch: "PATCH", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 50, 100)
        respond League.list(params), model: [count: League.count()]
    }

    def show(League league) {
        respond league
    }

    def update(League league) {
        if (league == null) {
            render status: NOT_FOUND
            return
        }

        league.clubs.first().name = "River"

        league.validate()
        if (league.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

        league.save flush: true, validate: false
        respond league, [status: CREATED]
    }

//    @Transactional
//    def update() {
//        League instance = League.get(params.id)
//        if (instance == null) {
//            render status: NOT_FOUND
//            return
//        }
//
//        instance.properties = request
//        instance.save flush: true, validate: false
//
//        respond instance, [status: OK]
//    }

    def addItalianLeague() {
        new League(name: "Italian Serie A").addToClubs(new Club(name: "AC Milan")).save()

        uefaService.dummy()

        League italian = new League(name: "Italian Serie B").addToClubs(new Club(name: "Livorno")).save()

        respond italian, [status: OK]
    }

    def deleteLeagues() {
        League.where {}.deleteAll()

        render status: OK
    }

    def players() {
        List<League> leagues = League.list()
        Map map = leagues.clubs.first().players.collectEntries { [(it.name): "${it.nationality}"] }

        respond map
    }
}