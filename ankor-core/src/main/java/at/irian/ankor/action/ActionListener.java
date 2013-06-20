package at.irian.ankor.action;

import at.irian.ankor.ref.Ref;

/**
 * @author MGeiler (Manfred Geiler)
 */
public interface ActionListener {
    void processAction(Ref actionContextRef, Action action);
}
