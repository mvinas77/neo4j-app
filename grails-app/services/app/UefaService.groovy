package app

public class UefaService {

    static transactional = 'neo4j'

    def tags = ["UEFA"]

    def uefa = ["English Premier League": ["Chelsea FC", "Arsenal FC", "Manchester City FC", "Manchester United FC", "Tottenham Hotspur FC", "Liverpool FC", "Newcastle United FC", "Everton FC", "Stoke City FC", "Swansea City AFC", "Birmingham City FC", "Fulham FC", "Wigan Athletic FC", "Southampton FC", "Hull City AFC", "West Ham United FC"],
                "Spanish Liga"          : ["Real Madrid CF", "FC Barcelona", "Club Atlético de Madrid", "Valencia CF", "Sevilla FC", "Athletic Club", "Málaga CF", "Villarreal CF", "Levante UD", "Real Betis Balompié", "Real Sociedad de Fútbol"],
                "French Ligue 1"        : ["Paris Saint-Germain", "Olympique Lyonnais", "Olympique de Marseille", "AS Monaco FC", "LOSC Lille", "FC Girondins de Bordeaux", "AS Saint-Étienne", "EA Guingamp", "Montpellier Hérault SC", "Stade Rennais FC", "OGC Nice", "FC Sochaux-Montbéliard"],
                "German Bundesliga"     : ["FC Bayern München", "Borussia Dortmund", "FC Schalke 04", "Bayer 04 Leverkusen", "Hannover 96", "VfL Wolfsburg", "VfL Borussia Mönchengladbach", "Eintracht Frankfurt", "VfB Stuttgart", "SC Freiburg", "FC Augsburg", "1. FSV Mainz 05"]]

    public initialize() {
        log.debug "initializing UEFA..."


        initUEFA()
        int totalClubs = uefa.collect { it.value.size() }.sum()
        log.debug("UEFA initialized with ${League.count()} leagues and ${Club.count()} clubs")
        assert Club.count() == totalClubs

    }

    public dummy() {
        log.debug "Dummy service..."
    }


    private void initUEFA() {
        if (Tag.count() == 0) {
            tags.each { String tag ->
                new Tag(name: tag).save()
            }
        }

        if (League.count() == 0) {
            uefa.each() { entry ->
                League league = new League(name: entry.key).addToTags(Tag.findByName("UEFA"))
                List<String> clubs = entry.value
                clubs?.each() { String name ->
                    Player player1 = new Player(name: "Messi", nationality: new NativeNationality(name: 'ARG'))
                    Player player2 = new Player(name: "Ronaldo", nationality: new NativeNationality(name: 'POR'))
                    Club club = new Club(name: name, players: [player1, player2], big: 'yeah')
                    league.addToClubs(club)
                }
                league.save()
            }
        }
    }
}