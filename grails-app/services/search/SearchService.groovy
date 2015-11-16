package search

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.core.CoreContainer

import grails.transaction.Transactional

import app.AbstractGraphDomain
import app.Player

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

        getPropertiesToIndex(graphDomain).each { String property ->

            document.addField(property, graphDomain."$property")
        }

        getFacetsToIndex(graphDomain).each { String facet ->

            document.addField(facet, graphDomain."$facet")
        }

        UpdateResponse response = server.add(document);

        // Remember to commit your changes!

        server.commit();
    }

    public void remove(AbstractGraphDomain graphDomain) {


        server.deleteById(graphDomain.id?.toString())
        server.commit()
    }

    private Set<String> getPropertiesToIndex(Player player) {
            return [ 'name']
        }

    private Set<String> getFacetsToIndex(Player player) {
        return [ 'nationality']
    }
}
