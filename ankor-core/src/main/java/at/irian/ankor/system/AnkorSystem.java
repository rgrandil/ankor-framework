package at.irian.ankor.system;

import at.irian.ankor.application.Application;
import at.irian.ankor.connector.ConnectorLoader;
import at.irian.ankor.messaging.modify.Modifier;
import at.irian.ankor.msg.MessageBus;
import at.irian.ankor.msg.MessageListener;
import at.irian.ankor.msg.RoutingTable;
import at.irian.ankor.ref.RefContextFactory;
import at.irian.ankor.session.ModelSessionManager;
import at.irian.ankor.viewmodel.metadata.BeanMetadataProvider;
import com.typesafe.config.Config;

import java.util.List;

/**
 * This is the main system object that sticks all the Ankor parts together.
 * Typically every node in an Ankor environment has exactly one AnkorSystem instance.
 * So, in a pure-Java client-server environment (e.g. JavaFX client and Java web server) there is
 * one AnkorSystem instance on the client side and one AnkorSystem instance on the server side.
 *
 * @author Manfred Geiler
 */
@SuppressWarnings("UnusedDeclaration")
public class AnkorSystem {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnkorSystem.class);

    private final Application application;
    private final Config config;
    private final MessageBus messageBus;
    private final RefContextFactory refContextFactory;
    private final ModelSessionManager modelSessionManager;
    private final RoutingTable routingTable;
    private final Modifier modifier;
    private final List<MessageListener> defaultMessageListeners;
    private final ConnectorLoader connectorLoader;
    private final BeanMetadataProvider beanMetadataProvider;

    protected AnkorSystem(Application application,
                          Config config,
                          MessageBus messageBus,
                          RefContextFactory refContextFactory,
                          ModelSessionManager modelSessionManager,
                          RoutingTable routingTable,
                          Modifier modifier,
                          List<MessageListener> defaultMessageListeners,
                          BeanMetadataProvider beanMetadataProvider) {
        this.application = application;
        this.config = config;
        this.messageBus = messageBus;
        this.refContextFactory = refContextFactory;
        this.modelSessionManager = modelSessionManager;
        this.routingTable = routingTable;
        this.modifier = modifier;
        this.defaultMessageListeners = defaultMessageListeners;
        this.beanMetadataProvider = beanMetadataProvider;
        this.connectorLoader = new ConnectorLoader();
    }

    public String getSystemName() {
        return application.getName();
    }

    public Config getConfig() {
        return config;
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public RefContextFactory getRefContextFactory() {
        return refContextFactory;
    }

    public ModelSessionManager getModelSessionManager() {
        return modelSessionManager;
    }

    public Application getApplication() {
        return application;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public BeanMetadataProvider getBeanMetadataProvider() {
        return beanMetadataProvider;
    }

    @Override
    public String toString() {
        return "AnkorSystem{'" + getSystemName() + "'}";
    }

    public AnkorSystem start() {
        LOG.info("Starting {}", this);
        for (MessageListener messageListener : defaultMessageListeners) {
            messageBus.registerMessageListener(messageListener);
        }
        connectorLoader.loadAndInitConnectors(this);
        connectorLoader.startAllConnectors();
        messageBus.start();
        return this;
    }


    @SuppressWarnings("UnusedDeclaration")
    public void stop() {
        LOG.info("Stopping {}", this);
        messageBus.stop();
        connectorLoader.stopAllConnectors();
        for (MessageListener messageListener : defaultMessageListeners) {
            messageBus.unregisterMessageListener(messageListener);
        }
    }

}
