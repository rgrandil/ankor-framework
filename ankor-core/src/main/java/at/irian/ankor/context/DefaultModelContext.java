package at.irian.ankor.context;

import at.irian.ankor.event.ArrayListEventListeners;
import at.irian.ankor.event.EventListeners;
import at.irian.ankor.event.dispatch.EventDispatcher;
import at.irian.ankor.event.dispatch.EventDispatcherFactory;

/**
 * @author Manfred Geiler
 */
class DefaultModelContext implements ModelContext {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ModelContext.class);

    private final String id;
    private final EventListeners eventListeners;
    private EventDispatcher eventDispatcher;
    private Object modelRoot;

    DefaultModelContext(String id, EventListeners eventListeners, Object modelRoot) {
        this.id = id;
        this.eventListeners = eventListeners;
        this.modelRoot = modelRoot;
    }

    public static ModelContext create(EventDispatcherFactory eventDispatcherFactory, String id, Object initialModelRoot) {
        EventListeners eventListeners = new ArrayListEventListeners();
        DefaultModelContext modelContext = new DefaultModelContext(id, eventListeners, initialModelRoot);
        modelContext.setEventDispatcher(eventDispatcherFactory.createFor(modelContext));
        return modelContext;
    }

    public String getId() {
        return id;
    }

    @Override
    public EventListeners getEventListeners() {
        return eventListeners;
    }

    @Override
    public Object getModelRoot() {
        return modelRoot;
    }

    @Override
    public void setModelRoot(Object modelRoot) {
        this.modelRoot = modelRoot;
    }

    private void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    @Override
    public void close() {
        if (eventDispatcher != null) {
            eventDispatcher.close();
        }
    }

    @Override
    public String toString() {
        return "DefaultModelContext{" +
               "id='" + id + '\'' +
               '}';
    }
}
