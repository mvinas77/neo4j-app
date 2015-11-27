package app.search

import javax.xml.bind.ValidationException

import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument

import grails.transaction.Transactional

import app.AbstractGraphDomain
import app.Club
import app.League
import app.Person
import app.Player
import search.Facet
import search.Paging
import search.SearchResponse
import search.SolrQueryBuilder

@Transactional
class SearchService {

    private static final facets = [
        [name: 'League', solrField: 'facet_league_s', facetField: 'league', facetProperty: 'name', type: Club.name],

        [name: 'Nationality', solrField: 'facet_nationality_s', facetField: 'nationality', facetProperty: 'name', type: Player.name],

        [name: 'Tags', solrField: 'facet_tags_st_list', facetField: 'tags', facetProperty: 'name', type: League.name],
    ]

    private List<FacetDefinition> facetCache = []

    def grailsApplication
    SolrClient server

    public void initialize() {

        String mode = grailsApplication.config.app.search.solr.mode
        if (!mode) {
            log.debug('Using default solr mode: Embedded')
            mode = 'embedded'
        }

        mode = mode.toLowerCase()

        if (mode == 'embedded') {
            /*
               String solrHome = grailsApplication.config.app.search.solr.home

               if (!solrHome) {
                   log.fatal('Impossible initialize solr: property app.search.solr.home is required')
                   throw new IllegalArgumentException('Impossible initialize solr: property app.search.solr.home is required')
               }

               System.setProperty("solr.solr.home", solrHome)
               CoreContainer.Initializer initializer = new CoreContainer.Initializer()
               CoreContainer coreContainer = initializer.initialize()
               EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "")
               this.server = server    */
        }
        else if (mode == 'server') {

            String solrUrl = grailsApplication.config.app.search.solr.serverUrl

            if (!solrUrl) {
                log.fatal('Impossible initialize solr: property app.search.solr.serverUrl is required')
                throw new IllegalArgumentException('Impossible initialize solr: property app.search.solr.serverUrl is required')
            }
            HttpSolrClient server = new HttpSolrClient(solrUrl);
            this.server = server

        }
        else {

            log.fatal("Impossible initialize solr mode unknown: ${mode}")
            throw new IllegalArgumentException("Impossible initialize solr mode unknown: ${mode}")

        }

        if (FacetDefinition.count == 0) {

            facets.each { Map facetDef ->

                FacetDefinition facet = new FacetDefinition(facetDef)

                if (facet.validate()) {
                    facet.save(validate: false)

                }
                else {

                    throw new ValidationException('facet validation', facet.errors)
                }

                this.facetCache << facet
            }
        }
        else {
            this.facetCache.addAll(FacetDefinition.list())
        }
    }

    /*
    public void initialize() {

        String mode = grailsApplication.config.app.search.solr.mode
                if (!mode) {
            log.debug('Using default solr mode: Embedded')
            mode = 'embedded'
        }

        mode = mode.toLowerCase()

        if (mode == 'embedded') {

            String solrHome = grailsApplication.config.app.search.solr.home

            if (!solrHome) {
                log.fatal('Impossible initialize solr: property app.search.solr.home is required')
                throw new IllegalArgumentException('Impossible initialize solr: property app.search.solr.home is required')
            }

            System.setProperty("solr.solr.home", solrHome)
            CoreContainer.Initializer initializer = new CoreContainer.Initializer()
            CoreContainer coreContainer = initializer.initialize()
            EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "")
            this.server = server
        }
        else if (mode == 'server') {

            String solrUrl = grailsApplication.config.app.search.solr.serverUrl

            if (!solrUrl) {
                log.fatal('Impossible initialize solr: property app.search.solr.serverUrl is required')
                throw new IllegalArgumentException('Impossible initialize solr: property app.search.solr.serverUrl is required')
            }
            HttpSolrServer server = new HttpSolrServer(solrUrl);
            this.server = server

        }
        else {

            log.fatal("Impossible initialize solr mode unknown: ${mode}")
            throw new IllegalArgumentException("Impossible initialize solr mode unknown: ${mode}")

        }

        if (FacetDefinition.count == 0) {

            facets.each { Map facetDef ->

                FacetDefinition facet = new FacetDefinition(facetDef)

                if (facet.validate()) {
                    facet.save(validate: false)

                }
                else {

                    throw new ValidationException('facet validation', facet.errors)
                }

                this.facetCache << facet
            }
        }
        else {
            this.facetCache.addAll(FacetDefinition.list())
        }
    }
    */

    public void index(AbstractGraphDomain graphDomain) {

        SolrInputDocument document = new SolrInputDocument();
        document.addField('id', graphDomain.id);
        document.addField('__type__', graphDomain.class.name);
        addPropertiesToIndex(document, graphDomain)
        indexFacets(document, graphDomain)

        UpdateResponse response = server.add(document);
        server.commit();
    }

    public void index(List<Long> batch) {

        List<SolrInputDocument> docs = []

        long start = System.currentTimeMillis()
        batch.each { Long id ->

            Person person = Person.get(id)

            SolrInputDocument document = new SolrInputDocument();
            document.addField('id', person.id);
            document.addField('__type__', person.class.name);
            addPropertiesToIndex(document, person)
            indexFacets(document, person)

            docs << document
        }

        long end = System.currentTimeMillis()

        log.debug("Time to build batch was ${end - start}ms.")

        log.debug("adding docs to server....")
        UpdateResponse response = server.add(docs, -1);
        log.debug("Time to Push batch was ${System.currentTimeMillis() - end}ms.")
    }

    public void commit() {

        server.commit()
    }


    public def search(String textToSearch, String type = null, Paging paging = null) {

        long starTime = System.currentTimeMillis()
        SolrQuery query = new SolrQueryBuilder()
            .setTextToSearch(textToSearch)
            .setType(type)
            .setPaging(paging)
            .build()

        QueryResponse response = server.query(query)
        //return response

        long searchTime = System.currentTimeMillis()

        SearchResponse searchResponse = createSearchResponse(response)

        long buildTime = System.currentTimeMillis()

        log.debug("=========================================================================")
        log.debug("Total records found ${searchResponse.numFound} returned ${searchResponse.end - searchResponse.start}")
        log.debug("Solr search done in ${searchTime - starTime} ms.")
        log.debug("Solr reponse Object build in ${buildTime - searchTime} ms.")
        log.debug("Total time was ${buildTime - starTime} ms.")
        log.debug("=========================================================================")

        return searchResponse
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

        club.players?.each { Player player ->
            document.addField('player_list', player?.id)
        }
    }

    private void addPropertiesToIndex(SolrInputDocument document, Player player) {

        document.addField('name_t', player.name)
    }

    private void addPropertiesToIndex(SolrInputDocument document, League league) {

        document.setField('name_t', league.name)
        document.setField('dateCreated', league.dateCreated)
        document.setField('lastUpdated', league.lastUpdated)

        league.clubs?.each { Club club ->
            document.addField('club_list', club?.id)
        }

    }

    private void addPropertiesToIndex(SolrInputDocument document, Person person) {

        document.addField('street_number_i', person.address.number)
        document.addField('street_street_s', person.address.street)
        document.addField('street_zip_i', person.address.zip)
        document.addField('firstName_t', person.firstName)
        document.addField('lastName_t', person.lastName)
        document.addField('fullName_t', person.fullName)
        document.addField('email_s', person.email)
        document.addField('phone_s', person.phone)
        document.addField('facet_birthDate_tdt', person.birthDate)
        document.addField('facet_city_s', person.address.city)
        document.addField('facet_bloodType_s', person.bloodType.simpleName)
        document.addField('facet_nationality_s', person.nationality)
        document.addField('facet_occupation_s', person.occupation)
        document.addField('facet_status_s', person.status)
    }

    private SearchResponse createSearchResponse(QueryResponse response) {
        SearchResponse searchResponse = new SearchResponse()

        SolrDocumentList documentList = response.results

        searchResponse.start = documentList.start
        searchResponse.end = documentList.start + documentList.size()
        searchResponse.numFound = documentList.numFound

        documentList.each { SolrDocument document ->

            Object item = getNeo4jObject(document)

            if (item) {
                searchResponse.items << item
            }
        }

        searchResponse.facets = createFacetOptions(response.facetFields)

        return searchResponse
    }

    private List<Facet> createFacetOptions(List<FacetField> facetFields) {
        List<Facet> facets = []

        facetFields?.each { FacetField facetField ->

            FacetDefinition definition = FacetDefinition.findBySolrField(facetField.name)

            Facet facet = new Facet()
            facet.name = definition ? definition.name : facetField.name
            long count = 0
            facet.options = []

            facetField.values?.each {

                Facet option = new Facet()
                option.name = it.name
                option.count = it.count
                count += it.count
                facet.options << option
            }

            facet.count = count

            facets << facet
        }

        return facets
    }

    private Object getNeo4jObject(SolrDocument document) {

        Long id = Long.valueOf((String) document.getFieldValue('id'))
        String type = document.getFirstValue('__type__')
        Class klass = getClass().getClassLoader().loadClass(type)

        if (klass) {
            return klass.get(id)
        }

        return null
    }

    public Collection<FacetDefinition> getFacets(Class klass) {

        if (klass) {
            return this.facetCache.findAll { it.type == klass.name }
        }
        else {
            return this.facetCache
        }
    }

    private void indexFacets(SolrInputDocument document, Object object) {
        getFacets(object.class).each { FacetDefinition facet ->

            def value = object."${facet.facetField}"

            if (value instanceof Collection) {

                value.each { it ->

                    Object facetValue = it

                    if (facet.facetProperty) {
                        facetValue = value."${facet.facetProperty}"
                    }
                    document.addField(facet.solrField, facetValue)
                }
            }
            else {
                if (facet.facetProperty) {
                    value = value."${facet.facetProperty}"
                }

                document.addField(facet.solrField, value)
            }
        }
    }

}
