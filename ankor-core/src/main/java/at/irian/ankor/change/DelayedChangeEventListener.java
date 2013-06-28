package at.irian.ankor.change;

import at.irian.ankor.event.DelayedModelEventListener;
import at.irian.ankor.event.EventDelay;
import at.irian.ankor.event.EventDelaySupport;
import at.irian.ankor.ref.Ref;
import at.irian.ankor.ref.impl.RefContextImplementor;

/**
 * @author Manfred Geiler
 */
public abstract class DelayedChangeEventListener extends ChangeEventListener
        implements DelayedModelEventListener<ChangeEvent> {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DelayedChangeEventListener.class);

    private final EventDelay delay;

    public DelayedChangeEventListener(Ref watchedProperty, long delayMilliseconds) {
        super(watchedProperty);
        EventDelaySupport eventDelaySupport = ((RefContextImplementor) watchedProperty.context()).eventDelaySupport();
        this.delay = eventDelaySupport.createEventDelayFor(this, delayMilliseconds);
    }

    @Override
    public final void process(ChangeEvent event) {
        delay.processDelayed(event);
    }

}