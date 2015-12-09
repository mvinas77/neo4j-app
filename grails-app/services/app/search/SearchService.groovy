package app.search

import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.response.RangeFacet
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.util.DateUtil

import grails.transaction.Transactional
import grails.validation.ValidationException

import app.AbstractGraphDomain
import app.Club
import app.League
import app.Person
import app.Player
import search.DateRangeUnit
import search.Facet
import search.Paging
import search.SearchResponse
import search.SolrQueryBuilder

@Transactional
class SearchService {

    private static final facets = [
        [name: 'LastName', solrField: 'lastName_t', facetField: 'lastName', type: Person.name],
        [name: 'FirstName', solrField: 'firstName_t', facetField: 'firstName', type: Person.name],
        [name: 'Blood Type', solrField: 'facet_bloodType_s', facetField: 'bloodType', facetProperty: 'simpleName', type: Person.name],
        [name: 'City', solrField: 'facet_city_s', facetField: 'address', facetProperty: 'city', type: Person.name],
        [name: 'Nationality', solrField: 'facet_nationality_s', facetField: 'nationality', type: Person.name],
        [name: 'Occupation', solrField: 'facet_occupation_s', facetField: 'occupation', type: Person.name],
        [name: 'Status', solrField: 'facet_status_s', facetField: 'status', type: Person.name],
        [name: 'Zip', solrField: 'street_zip_i', facetField: 'zip', type: Person.name],
        [name: 'BirthDate', solrField: 'facet_birthDate_tdt', facetField: 'birthDate', type: Person.name,
                config: [
                    type: DateRangeFacetConfig,
                    rangeStart: new Date(40, 0, 1, 0, 0, 0),
                    rangeEnd: new Date(100, 0, 1, 0, 0, 0),
                    increment: 10,
                    unit: DateRangeUnit.YEAR
                ]
        ],
    ]

    private List<FacetDefinition> facetCache = []

    def grailsApplication
    SolrClient server

    public void initialize() {

        String solrUrl = grailsApplication.config.app.search.solr.serverUrl

        if (!solrUrl) {
            log.fatal('Impossible initialize solr: property app.search.solr.serverUrl is required')
            throw new IllegalArgumentException('Impossible initialize solr: property app.search.solr.serverUrl is required')
        }
        HttpSolrClient server = new HttpSolrClient(solrUrl);
        this.server = server

        FacetDefinition.list().each { FacetDefinition facetDef ->
            facetDef.delete()
        }

        if (FacetDefinition.count < facets.size()) {

            facets.each { Map facetDef ->

                FacetDefinition facet = FacetDefinition.findByName(facetDef.name)

                if (!facet) {

                    Map configMap = facetDef.remove('config')
                    facet = new FacetDefinition(facetDef)

                    if (configMap) {
                        Class configClass = configMap.remove('type')
                        FacetConfig config = configClass.newInstance()
                        config.properties = configMap
                        facet.config = config
                    }

                    if (facet.validate()) {
                        log.debug("Facet ${facetDef.name} added")
                        facet.save(validate: false)
                    }
                    else {

                        throw new ValidationException('facet validation', facet.errors)
                    }
                }

                this.facetCache << facet
            }
        }
        else {
            this.facetCache.addAll(FacetDefinition.list())
        }
    }

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

    public def searchSolr(String textToSearch, String type = null, Paging paging = null) {

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

        searchResponse.facets = createFacetOptions(response)

        return searchResponse
    }

    private List<Facet> createFacetOptions(QueryResponse response) {
        List<Facet> facets = []

        response.facetFields?.each { FacetField facetField ->

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

        response.facetRanges?.each { RangeFacet rangeFacet ->

           // FacetDefinition definition = FacetDefinition.findBySolrField(rangeFacet.name)
            FacetDefinition definition = this.facetCache.find { it.solrField == rangeFacet.name }

            Facet facet = new Facet()
            facet.name = rangeFacet ? definition.name : rangeFacet.name
            facet.options = []

            long count = 0

            rangeFacet.counts?.each {

                Facet option = new Facet()
                option.name = definition?.config ? formatValue(definition.config, it.value) : it.value
                option.count = it.count
                count += it.count
                facet.options << option
            }

            facet.count = count
            facets << facet
        }

        return facets
    }

    private Object formatValue(DateRangeFacetConfig config, String value) {

        Date date = DateUtil.parseDate(value)
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)

        def start = calendar.get(config.unit.field)
        calendar.add(config.unit.field, config.increment)
        def end = calendar.get(config.unit.field)

        return "${start}-${end}"
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
