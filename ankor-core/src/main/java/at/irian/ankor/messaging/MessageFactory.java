package at.irian.ankor.messaging;

import at.irian.ankor.action.Action;

/**
 * @author Manfred Geiler
 */
public class MessageFactory {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageFactory.class);

    private final MessageIdGenerator messageIdGenerator;

    public MessageFactory(MessageIdGenerator messageIdGenerator) {
        this.messageIdGenerator = messageIdGenerator;
    }

    public Message createActionMessage(String sessionId, String actionPropertyPath, Action action) {
        return new ActionMessage(sessionId, messageIdGenerator.create(), actionPropertyPath, action);
    }

    public Message createChangeMessage(String sessionId, String changedPropertyPath, Object newValue) {
        return new ChangeMessage(sessionId, messageIdGenerator.create(), changedPropertyPath, newValue);
    }

}
