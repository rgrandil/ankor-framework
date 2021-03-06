package at.irian.ankor.fx.binding.value;

import at.irian.ankor.fx.binding.cache.FxCacheSupport;
import at.irian.ankor.ref.Ref;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.util.Callback;

/**
 * A JavaFX observable ObservableList backed by a Ankor Ref that references a List.
 * The list instance is directly retrieved from the underlying Ref.
 * The value of this observable value is an ObservableList that is backed by the Ref as well.
 * Listeners of this observable get notified whenever the underlying Ref changes - i.e. when the list instance is replaced.
 *
 * @author Manfred Geiler
 */
public class ObservableValueListRef<T> extends ObservableRef<ObservableList<T>> implements ObservableValue<ObservableList<T>> {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ObservableValueListRef.class);

    private final ObservableList<T> observableList;

    protected ObservableValueListRef(Ref ref) {
        super(ref, null);
        this.observableList = ObservableListRef.createObservableList(ref);
    }


    public static <T> ObservableValue<ObservableList<T>> createObservableValueList(Ref ref) {
        return FxCacheSupport.getBindingCache(ref)
                             .getObservableValueList(ref, null, new Callback<Ref, ObservableValue<ObservableList<T>>>() {
                                 @Override
                                 public ObservableValue<ObservableList<T>> call(Ref ref) {
                                     LOG.debug("Creating ObservableValueListRef for {}", ref);
                                     return new ObservableValueListRef<>(ref);
                                 }
                             });
    }


    @Override
    public ObservableList<T> getValue() {
        return observableList;
    }

}
