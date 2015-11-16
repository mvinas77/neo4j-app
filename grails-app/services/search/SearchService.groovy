package search
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.core.CoreContainer

import grails.transaction.Transactional

import app.AbstractGraphDomain
import app.Club
import app.League
import app.Player
import app.Tag

@Transactional
class SearchService {

    SolrServer server

    public void initialize() {

        System.setProperty("solr.solr.home", "/home/mvinas/Work/Brinqa/workspace/brinqa/neo4j-app/solr")
        CoreContainer.Initializer initializer = new CoreContainer.Initializer()
        CoreContainer coreContainer = initializer.initialize()
        EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "")
        this.server = server
    }

    public void index(AbstractGraphDomain graphDomain) {

        SolrInputDocument document = new SolrInputDocument();
        document.addField('id', graphDomain.id);
        document.addField('__type__', graphDomain.class.name);

        addPropertiesToIndex(document, graphDomain)

        UpdateResponse response = server.add(document);

        // Remember to commit your changes!

        server.commit();
    }

    public SolrDocumentList search(String textToSearch) {

        SolrQuery query = new SolrQuery()
        query.setQuery(textToSearch)

        QueryResponse response = server.query(query)
    }

    public void remove(AbstractGraphDomain graphDomain) {

        server.deleteById(graphDomain.id?.toString())
        server.commit()
    }

    private void addPropertiesToIndex(SolrInputDocument document, Object object) {

        //Empty no facets to add
    }

    private void addPropertiesToIndex(SolrInputDocument document, Club club) {

        document.setField('name_t', club.name)
        document.setField('dateCreated', club.dateCreated)
        document.setField('lastUpdated', club.lastUpdated)
        document.setField('league_tl', club.league?.id)

        club.players?.each { Player player ->
            document.addField('player_list', player?.id)
        }
    }

    private void addPropertiesToIndex(SolrInputDocument document, Player player) {

        document.addField('name_t', player.name)
        document.setField('nationality_s', player?.strNationality)
    }

    private void addPropertiesToIndex(SolrInputDocument document, League league) {

        document.setField('name_t', league.name)
        document.setField('dateCreated', league.dateCreated)
        document.setField('lastUpdated', league.lastUpdated)

        league.clubs?.each { Club club ->
            document.addField('club_list', club?.id)
        }

        league.tags?.each { Tag tag ->
            document.addField('tags_st_list', tag.name)
        }

    }
}
