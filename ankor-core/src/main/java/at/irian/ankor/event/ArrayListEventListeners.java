package at.irian.ankor.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author MGeiler (Manfred Geiler)
 */
public class ArrayListEventListeners implements EventListeners {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnsynchronizedListenersHolder.class);

    private final EventListeners parentListeners;
    private List<ModelEventListener> listeners;

    public ArrayListEventListeners() {
        this(null);
    }

    public ArrayListEventListeners(EventListeners parentListeners) {
        this.parentListeners = parentListeners;
        this.listeners = Collections.emptyList();
    }

    @Override
    public void add(ModelEventListener listener) {
        ArrayList<ModelEventListener> newListeners = new ArrayList<ModelEventListener>(listeners);
        newListeners.add(listener);
        listeners = newListeners;
    }

    @Override
    public void remove(ModelEventListener listener) {
        ArrayList<ModelEventListener> newListeners = new ArrayList<ModelEventListener>(listeners);
        newListeners.remove(listener);
        listeners = newListeners;
    }

    @Override
    public Iterator<ModelEventListener> iterator() {
        if (parentListeners != null) {
            final Iterator<ModelEventListener> parentIterator = parentListeners.iterator();
            final Iterator<ModelEventListener> thisIterator = listeners.iterator();
            return new Iterator<ModelEventListener>() {

                private int internalState = 0;

                @Override
                public boolean hasNext() {
                    return parentIterator.hasNext() || thisIterator.hasNext();
                }

                @Override
                public ModelEventListener next() {
                    if (parentIterator.hasNext()) {
                        internalState = 1;
                        return parentIterator.next();
                    } else {
                        internalState = 2;
                        return thisIterator.next();
                    }
                }

                @Override
                public void remove() {
                    if (internalState == 1) {
                        internalState = 0;
                        parentIterator.remove();
                    } else if (internalState == 2) {
                        internalState = 0;
                        thisIterator.remove();
                    } else {
                        throw new IllegalStateException();
                    }
                }
            };
        } else {
            return listeners.iterator();
        }
    }

    @Override
    public void cleanup() {
        Iterator<ModelEventListener> it = this.iterator();
        while (it.hasNext()) {
            ModelEventListener listener = it.next();
            if (listener.isDiscardable()) {
                it.remove();
            }
        }
    }

}
