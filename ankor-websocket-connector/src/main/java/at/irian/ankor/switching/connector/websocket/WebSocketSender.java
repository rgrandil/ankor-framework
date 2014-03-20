package at.irian.ankor.switching.connector.websocket;

import at.irian.ankor.messaging.MessageMapperFactory;
import at.irian.ankor.messaging.MessageSerializer;
import at.irian.ankor.switching.Switchboard;
import at.irian.ankor.switching.connector.HandlerScopeContext;
import at.irian.ankor.switching.routing.ModelAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author Thomas Spiegl
 */
public class WebSocketSender {
    private static Logger LOG = LoggerFactory.getLogger(WebSocketSender.class);

    private static final String MESSAGE_SERIALIZER_CTXT_ATTR_KEY = WebSocketSender.class.getName() + ".MessageSerializer";

    private final WebSocketSessionRegistry sessionRegistry;
    private final Switchboard switchboard;
    private final MessageMapperFactory<String> messageMapperFactory;

    public WebSocketSender(WebSocketSessionRegistry sessionRegistry,
                           Switchboard switchboard,
                           MessageMapperFactory<String> messageMapperFactory) {
        this.sessionRegistry = sessionRegistry;
        this.switchboard = switchboard;
        this.messageMapperFactory = messageMapperFactory;
    }

    public void send(ModelAddress sender,
                     WebSocketModelAddress receiver,
                     WebSocketMessage message,
                     HandlerScopeContext context) {
        Session session = sessionRegistry.getSession(receiver.getClientId());
        if (session == null) {
            LOG.error("Error sending {} from {} to {} - automatically disconnecting {}, reason: NO WS SESSION FOUND ...",
                    message,
                    sender,
                    receiver,
                    receiver);
            switchboard.closeConnection(sender, receiver);
        } else {
            try {
                String serializedMsg = getMessageSerializer(context).serialize(message);
                LOG.debug("Sending serialized message to {}: {}", receiver, serializedMsg);
                session.getBasicRemote().sendText(serializedMsg);
            } catch (IOException e) {
                LOG.error("Error sending {} from {} to {} - automatically disconnecting {} ...",
                        message,
                        sender,
                        receiver,
                        receiver, e);
                switchboard.closeConnection(sender, receiver);
            }
        }
    }

    private MessageSerializer<String> getMessageSerializer(HandlerScopeContext context) {
        @SuppressWarnings("unchecked")
        MessageSerializer<String> messageSerializer
                = (MessageSerializer<String>) context.getAttributes().get(MESSAGE_SERIALIZER_CTXT_ATTR_KEY);
        if (messageSerializer == null) {
            messageSerializer = messageMapperFactory.createMessageMapper();
            context.getAttributes().put(MESSAGE_SERIALIZER_CTXT_ATTR_KEY, messageSerializer);
        }
        return messageSerializer;
    }


}
