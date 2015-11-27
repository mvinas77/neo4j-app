class UrlMappings {

	static mappings = {
		"/clubs"(resources: 'club')
		"/leagues"(resources: 'league')
		"/leagues/addItalianLeague"(controller: "league", action: "addItalianLeague")
		"/leagues/deleteLeagues"(controller: "league", action: "deleteLeagues")
		"/leagues/players"(controller: "league", action: "players")
		"/search"(resources: 'search')
		"/create/persons"(controller: "createPersons", action: "createPersons")
		"/persons/count"(controller: "createPersons", action: "count")
		"/persons/reindex"(controller: "createPersons", action: "reindex")
		"/persons/commit"(controller: "createPersons", action: "commit")
        "500"(view:'/error')
	}
}
