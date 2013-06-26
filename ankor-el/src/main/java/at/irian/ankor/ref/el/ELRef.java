package at.irian.ankor.ref.el;

import at.irian.ankor.action.Action;
import at.irian.ankor.action.ActionEvent;
import at.irian.ankor.action.ActionEventListener;
import at.irian.ankor.change.ChangeEvent;
import at.irian.ankor.change.ChangeEventListener;
import at.irian.ankor.el.ELUtils;
import at.irian.ankor.event.ModelEventListener;
import at.irian.ankor.path.PathSyntax;
import at.irian.ankor.ref.*;
import at.irian.ankor.util.ObjectUtils;

import javax.el.ValueExpression;
import javax.el.ValueReference;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author MGeiler (Manfred Geiler)
 */
class ELRef implements Ref {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ELRef.class);

    private final ValueExpression ve;
    private final ELRefContext refContext;

    ELRef(ValueExpression ve, ELRefContext refContext) {
        this.ve = ve;
        this.refContext = refContext;
    }

    @Override
    public void setValue(Object newValue) {

        ChangeEvent changeEvent = new ChangeEvent(this);

        // remember old watched values
        IdentityHashMap<ModelEventListener, Object> oldWatchedValues = new IdentityHashMap<ModelEventListener, Object>();
        for (ModelEventListener listener : refContext.getListenersHolder().getListeners()) {
            if (changeEvent.isAppropriateListener(listener)) {
                Ref watchedProperty = ((ChangeEventListener) listener).getWatchedProperty();
                if (watchedProperty != null) {
                    Object oldWatchedValue = watchedProperty.getValue();
                    oldWatchedValues.put(listener, oldWatchedValue);
                } else {
                    oldWatchedValues.put(listener, null);
                }
            }
        }

        // set the new value
        ve.setValue(refContext.getElContext(), newValue);

        // invoke all listeners where watched value has changed
        for (Map.Entry<ModelEventListener, Object> entry : oldWatchedValues.entrySet()) {
            ModelEventListener listener = entry.getKey();
            Object oldWatchedValue = entry.getValue();

            boolean process;
            Ref watchedProperty = ((ChangeEventListener) listener).getWatchedProperty();
            if (watchedProperty != null) {
                Object newWatchedValue = watchedProperty.getValue();
                process = !ObjectUtils.nullSafeEquals(oldWatchedValue, newWatchedValue);
            } else {
                process = true;
            }

            if (process) {
                try {
                    changeEvent.processBy(listener);
                } catch (Exception e) {
                    LOG.error("Listener " + listener + " threw exception while processing " + changeEvent, e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue() {
        try {
            return (T)ve.getValue(refContext.getElContext());
        } catch (Exception e) {
            LOG.warn("unable to get value of " + this, e);
            return null;
        }
    }

    @Override
    public boolean isValid() {
        if (isRoot()) {
            Object modelRoot = ve.getValue(refContext.getElContext());
            return modelRoot != null;
        } else {
            ValueReference valueReference = ve.getValueReference(refContext.getElContext());
            return valueReference != null && valueReference.getBase() != null;
        }
    }

    @Override
    public Ref root() {
        return refFactory().rootRef();
    }

    @Override
    public Ref parent() {
        if (isRoot()) {
            throw new UnsupportedOperationException("root ref has no parent");
        } else {
            return refFactory().ref(pathSyntax().parentOf(path()));
        }
    }

    private PathSyntax pathSyntax() {return refContext.getPathSyntax();}

    private RefFactory refFactory() {return refContext.getRefFactory();}

    @Override
    public Ref append(String propertyOrSubPath) {
        return refFactory().ref(pathSyntax().concat(path(), propertyOrSubPath));
    }

    @Override
    public Ref appendIdx(int index) {
        return refFactory().ref(pathSyntax().addArrayIdx(path(), index));
    }

    @Override
    public Ref appendLiteralKey(String literalKey) {
        return refFactory().ref(pathSyntax().addLiteralMapKey(path(), literalKey));
    }

    @Override
    public Ref appendPathKey(String pathKey) {
        return refFactory().ref(pathSyntax().addMapKey(path(), pathKey));
    }

    @Override
    public void fireAction(Action action) {
        ActionEvent actionEvent = new ActionEvent(this, action);
        for (ModelEventListener listener : refContext.getListenersHolder().getListeners()) {
            if (actionEvent.isAppropriateListener(listener)) {
                try {
                    actionEvent.processBy(listener);
                } catch (Exception e) {
                    LOG.error("Listener " + listener + " threw exception while processing " + actionEvent, e);
                }
            }
        }
    }

    @Override
    public String path() {
        return ELUtils.exprToPath(expression());
    }

    @Override
    public boolean isDescendantOf(Ref ref) {
        if (isRoot()) {
            return false;
        }
        Ref parentRef = parent();
        return parentRef != null && (parentRef.equals(ref) || parentRef.isDescendantOf(ref));
    }

    @Override
    public boolean isAncestorOf(Ref ref) {
        return ref.isDescendantOf(this);
    }

    @Override
    public boolean isRoot() {
        return path().equals(refContext.getModelRootVarName());
    }

    @Override
    public RefContext getRefContext() {
        return refContext;
    }

    @Override
    public Ref withRefContext(RefContext newRefContext) {
        return new ELRef(ve, (ELRefContext)newRefContext);
    }

    @Override
    public void addChangeListener(final ChangeListener listener) {
        ChangeEventListener eventListener = new ChangeEventListener(this) {
            @Override
            public void processChange(Ref changedProperty) {
                listener.processChange(changedProperty, getWatchedProperty());
            }
        };
        refContext.getListenersHolder().addListener(eventListener);
    }

    @Override
    public void addActionListener(final ActionListener listener) {
        ActionEventListener eventListener = new ActionEventListener(this) {
            @Override
            public void processAction(Ref actionProperty, Action action) {
                listener.processAction(getWatchedProperty(), action);
            }
        };
        refContext.getListenersHolder().addListener(eventListener);
    }

    @Override
    public boolean equals(Object that) {
        return this == that
               || !(that == null || getClass() != that.getClass()) && expression().equals(((ELRef) that).expression());
    }

    private String expression() {
        return ve.getExpressionString();
    }

    @Override
    public int hashCode() {
        return expression().hashCode();
    }

    @Override
    public String toString() {
        return "Ref{" + path() + "}";
    }

}
