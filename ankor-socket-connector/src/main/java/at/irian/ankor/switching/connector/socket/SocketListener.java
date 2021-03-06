package at.irian.ankor.switching.connector.socket;

import at.irian.ankor.action.Action;
import at.irian.ankor.change.Change;
import at.irian.ankor.serialization.MessageDeserializer;
import at.irian.ankor.path.PathSyntax;
import at.irian.ankor.switching.Switchboard;
import at.irian.ankor.switching.msg.ActionEventMessage;
import at.irian.ankor.switching.msg.ChangeEventMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;

/**
* @author Manfred Geiler
*/
public class SocketListener {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SocketListener.class);

    private final String systemName;
    private final int listenPort;
    private final MessageDeserializer<String> messageDeserializer;
    private final Switchboard switchboard;
    private final PathSyntax pathSyntax;
    private volatile boolean running;
    private ServerSocket serverSocket;
    private Thread receiveLoopThread;

    public SocketListener(String systemName,
                          URI localAddress,
                          MessageDeserializer<String> messageDeserializer,
                          Switchboard switchboard,
                          PathSyntax pathSyntax) {
        this.systemName = systemName;
        this.listenPort = localAddress.getPort();
        this.messageDeserializer = messageDeserializer;
        this.switchboard = switchboard;
        this.pathSyntax = pathSyntax;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void start() {
        LOG.info("Starting {} ...", this);

        try {
            serverSocket = new ServerSocket(listenPort);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open server socket on port " + listenPort);
        }

        this.receiveLoopThread = new Thread(new Runnable() {
            @Override
            public void run() {
                loop();
            }
        }, "Ankor '" + systemName + "'");
        this.receiveLoopThread.setDaemon(true);

        this.running = true;
        this.receiveLoopThread.start();
        LOG.info("{} successfully started", this);
    }

    public void stop() {
        this.running = false;
        try {
            this.serverSocket.close();
        } catch (IOException ignore) {}
        this.receiveLoopThread.interrupt();
    }

    private void loop() {
        LOG.debug("{} is now listening...", SocketListener.this);
        while (running) {

            String serializedMsg;
            try {
                serializedMsg = receive();
            } catch (IOException e) {
                if (e instanceof SocketException && "Socket closed".equals(e.getMessage())) {
                    LOG.info("Socket was closed");
                    return;
                }
                throw new RuntimeException("Error reading message from " + serverSocket, e);
            }

            LOG.debug("{} received: {}", SocketListener.this, serializedMsg);
            try {
                SocketMessage socketMessage = messageDeserializer.deserialize(serializedMsg, SocketMessage.class);
                if (socketMessage.getAction() != null) {
                    handleIncomingActionEventMessage(socketMessage);
                } else if (socketMessage.getChange() != null) {
                    handleIncomingChangeEventMessage(socketMessage);
                } else if (socketMessage.isClose()) {
                    handleIncomingCloseMessage(socketMessage);
                } else {
                    handleIncomingConnectMessage(socketMessage);
                }
            } catch (Exception e) {
                LOG.error("Exception while handling socket message " + serializedMsg, e);
            }
        }
    }

    private String receive() throws IOException {
        Socket accept = null;
        try {
            accept = serverSocket.accept();
            InputStreamReader inReader = new InputStreamReader(accept.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(inReader);
            return reader.readLine();
        } finally {
            if (accept != null) {
                try {
                    accept.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void handleIncomingConnectMessage(SocketMessage socketMessage) {
        SocketModelAddress sender = getSenderFrom(socketMessage);
        switchboard.openConnection(sender, socketMessage.getConnectParams());
    }

    private void handleIncomingActionEventMessage(SocketMessage socketMessage) {
        Action action = socketMessage.getAction();
        SocketModelAddress sender = getSenderFrom(socketMessage);
        switchboard.send(sender, new ActionEventMessage(socketMessage.getProperty(),
                                                        action,
                                                        socketMessage.getStateValues(),
                                                        socketMessage.getStateProps()));
    }

    private void handleIncomingChangeEventMessage(SocketMessage socketMessage) {
        Change change = socketMessage.getChange();
        SocketModelAddress sender = getSenderFrom(socketMessage);
        switchboard.send(sender, new ChangeEventMessage(socketMessage.getProperty(),
                                                        change,
                                                        socketMessage.getStateValues(),
                                                        socketMessage.getStateProps()));
    }

    private void handleIncomingCloseMessage(SocketMessage socketMessage) {
        SocketModelAddress sender = getSenderFrom(socketMessage);
        switchboard.closeAllConnections(sender);
    }

    private SocketModelAddress getSenderFrom(SocketMessage socketMessage) {
        String modelName = pathSyntax.rootOf(socketMessage.getProperty());
        URI senderAddress = URI.create(socketMessage.getSenderId());
        return new SocketModelAddress(senderAddress, modelName);
    }

    @Override
    public String toString() {
        return "SocketListener{" +
               "systemName='" + systemName + '\'' +
               ", listenPort=" + listenPort +
               '}';
    }
}
