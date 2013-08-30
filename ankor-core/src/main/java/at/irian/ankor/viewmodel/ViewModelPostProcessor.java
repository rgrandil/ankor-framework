package at.irian.ankor.viewmodel;

import at.irian.ankor.ref.Ref;

/**
 * @author Manfred Geiler
 */
public interface ViewModelPostProcessor {
    void postProcess(Object viewModelObject, Ref viewModelRef);
}
