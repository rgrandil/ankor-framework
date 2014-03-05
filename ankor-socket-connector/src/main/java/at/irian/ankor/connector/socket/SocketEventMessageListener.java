package at.irian.ankor.connector.socket;

import at.irian.ankor.messaging.MessageSerializer;
import at.irian.ankor.msg.ActionEventMessage;
import at.irian.ankor.msg.ChangeEventMessage;
import at.irian.ankor.msg.EventMessage;
import at.irian.ankor.msg.RoutingTable;
import at.irian.ankor.msg.party.Party;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.util.Collection;

/**
* @author Manfred Geiler
*/
class SocketEventMessageListener implements EventMessage.Listener {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SocketEventMessageListener.class);

    private final RoutingTable routingTable;
    private final MessageSerializer<String> messageSerializer;
    private final URI localAddress;

    public SocketEventMessageListener(RoutingTable routingTable,
                                      MessageSerializer<String> messageSerializer,
                                      URI localAddress) {
        this.routingTable = routingTable;
        this.messageSerializer = messageSerializer;
        this.localAddress = localAddress;
    }

    @Override
    public void onEventMessage(EventMessage msg) {
        Party sender = msg.getSender();
        Collection<Party> receivers = routingTable.getConnectedParties(sender);
        for (Party receiver : receivers) {
            if (receiver instanceof SocketParty) {
                send((SocketParty) receiver, msg);
            }
        }
    }


    private void send(SocketParty receiver, EventMessage eventMessage) {
        Socket socket;
        try {
            socket = new Socket(receiver.getHost(), receiver.getPort());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot connect to " + receiver);
        }

        try {
            SocketMessage socketMessage;
            if (eventMessage instanceof ActionEventMessage) {
                socketMessage = SocketMessage.createActionMsg(localAddress.toString(),
                                                              ((ActionEventMessage) eventMessage).getProperty(),
                                                              ((ActionEventMessage) eventMessage).getAction());
            } else if (eventMessage instanceof ChangeEventMessage) {
                socketMessage = SocketMessage.createChangeMsg(localAddress.toString(),
                                                              ((ChangeEventMessage) eventMessage).getProperty(),
                                                              ((ChangeEventMessage) eventMessage).getChange());
            } else {
                throw new IllegalArgumentException("Unsupported event message type " + eventMessage.getClass().getName());
            }

            String serializedMsg = messageSerializer.serialize(socketMessage);
            LOG.debug("Sending serialized message to {}: {}", receiver, serializedMsg);

            OutputStreamWriter outWriter = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outWriter, true);
            writer.println(serializedMsg);
        } catch (IOException e) {
            throw new RuntimeException("Error sending message to " + socket);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

}
