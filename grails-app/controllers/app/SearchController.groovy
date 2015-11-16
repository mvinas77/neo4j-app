package app

class SearchController {

    static responseFormats = ["json"]

    static allowedMethods = [save: "POST", update: "PUT", patch: "PATCH", delete: "DELETE", search: 'GET']

    def searchService

    def index() {

    }


    def search(String q) {

        respond searchService.search(q)
    }


}
