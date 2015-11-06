package app

import grails.transaction.Transactional 

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional
class ClubController {
	
	static responseFormats = ["json"]

    static allowedMethods = [save: "POST", update: "PUT", patch: "PATCH", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 50, 100)
        respond Club.list(params), model: [count: Club.count()]
    }

    def show(Club club) {
        respond club
    }

    def update(Club club) {
    	if (club == null) {
            render status: NOT_FOUND
            return
        }

        club.validate()
        if (club.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

        club.save flush: true, validate: false
        respond club, [status: CREATED]
    }

}
