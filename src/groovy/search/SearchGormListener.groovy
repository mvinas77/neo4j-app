package search

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.context.ApplicationEvent

import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType

import app.AbstractGraphDomain
import app.search.SearchService

class SearchGormListener extends AbstractPersistenceEventListener {

    private Log log = LogFactory.getLog(SearchGormListener)
    private SearchService searchService

    public SearchGormListener(final Datastore datastore, final SearchService searchService) {
        super(datastore)
        this.searchService = searchService
    }

    @Override
    protected void onPersistenceEvent(final AbstractPersistenceEvent event) {
        switch (event.eventType) {
            case EventType.PostInsert:
                def object = event.entityObject
                if (object instanceof AbstractGraphDomain) {
                    searchService.index(object)
                }

                break

            case EventType.PostUpdate:
                def object = event.entityObject
                if (object instanceof AbstractGraphDomain) {
                    searchService.index(object)
                }
                break

            case EventType.PostDelete:
                def object = event.entityObject
                if (object instanceof AbstractGraphDomain) {
                    searchService.remove(object)
                }
                break
        }
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return true
    }
}
