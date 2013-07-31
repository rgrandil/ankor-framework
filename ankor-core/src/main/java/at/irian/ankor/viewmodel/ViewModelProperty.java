package at.irian.ankor.viewmodel;

import at.irian.ankor.base.Wrapper;
import at.irian.ankor.ref.Ref;

/**
 * @author Manfred Geiler
 */
@SuppressWarnings("UnusedDeclaration")
public class ViewModelProperty<T> implements Wrapper<T> {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ViewModelProperty.class);

    private Ref ref;
    private T value;

    ViewModelProperty() {
        this(null, null);
    }

    public ViewModelProperty(Ref parentObjectRef, String propertyName) {
        this(parentObjectRef, propertyName, null);
    }

    public ViewModelProperty(Ref parentObjectRef, String propertyName, T initialValue) {
        this.ref = parentObjectRef != null ? parentObjectRef.append(propertyName) : null;
        this.value = initialValue;
    }

    public static <T> ViewModelProperty<T> createUnreferencedProperty() {
        return new ViewModelProperty<T>(null, null, null);
    }

    public static <T> ViewModelProperty<T> createUnreferencedProperty(T initialValue) {
        return new ViewModelProperty<T>(null, null, initialValue);
    }

    public static <T> ViewModelProperty<T> createReferencedProperty(Ref parentObjectRef, String propertyName) {
        return new ViewModelProperty<T>(parentObjectRef, propertyName, null);
    }

    public static <T> ViewModelProperty<T> createReferencedProperty(Ref parentObjectRef, String propertyName, T initialValue) {
        return new ViewModelProperty<T>(parentObjectRef, propertyName, initialValue);
    }


    public void set(T newValue) {
        if (this.ref == null) {
            //throw new IllegalStateException("no ref");
            LOG.warn("setting non-referencing object " + this, new IllegalStateException());
            putWrappedValue(newValue);
        } else if (this.ref.isValid()) {
            // set value by indirection over ankor ref system
            this.ref.setValue(newValue);
        } else {
            putWrappedValue(newValue);
        }
    }

    public void init(T newValue) {
        putWrappedValue(newValue);
    }

    public T get() {
        return getWrappedValue();
    }

    public Ref getRef() {
        return ref;
    }

    void setRef(Ref ref) {
        this.ref = ref;
    }

    @Override
    public final T getWrappedValue() {
        return this.value;
    }

    @Override
    public final void putWrappedValue(T val) {
        this.value = val;
    }
}