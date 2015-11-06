class UrlMappings {

	static mappings = {
		"/clubs"(resources: 'club')
		"/leagues"(resources: 'league')
		"/leagues/addItalianLeague"(controller: "league", action: "addItalianLeague")
		"/leagues/deleteLeagues"(controller: "league", action: "deleteLeagues")
		"/leagues/players"(controller: "league", action: "players")

        "500"(view:'/error')
	}
}
