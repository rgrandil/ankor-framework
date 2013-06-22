package at.irian.ankor.service;

import at.irian.ankor.messaging.MessageBus;
import at.irian.ankor.messaging.MessageFactory;
import at.irian.ankor.rmi.ELRemoteMethodActionListener;
import at.irian.ankor.rmi.RemoteMethodActionListener;
import at.irian.ankor.application.ELApplication;

/**
 * @author Manfred Geiler
 */
public abstract class ELAnkorServer extends AnkorServerBase {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ELAnkorServer.class);

    public ELAnkorServer(ELApplication application, MessageFactory messageFactory, MessageBus messageBus) {
        super(application, messageFactory, messageBus);
    }

    @Override
    public void start() {
        super.start();
        registerDefaultListeners();
    }

    protected void registerDefaultListeners() {
        ELApplication application = (ELApplication)getApplication();
        RemoteMethodActionListener remoteMethodActionListener
                = new ELRemoteMethodActionListener(application.getExpressionFactory(),
                                                 application.getRefFactory());
        getApplication().getListenerRegistry().registerRemoteActionListener(null, remoteMethodActionListener);
    }
}