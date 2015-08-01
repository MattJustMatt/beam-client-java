package pro.beam.api.resource.chat;

import com.google.common.util.concurrent.ListenableFuture;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.chat.events.EventHandler;
import pro.beam.api.resource.chat.events.data.IncomingMessageData;
import pro.beam.api.resource.chat.methods.AuthenticateMessage;
import pro.beam.api.resource.chat.replies.ReplyHandler;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

public class BeamChatBridge {
    private static final String CLOSING_STRING = "BEAM_JAVA_DISCONNECT";
    private static final SSLSocketFactory SSL_SOCKET_FACTORY = (SSLSocketFactory) SSLSocketFactory.getDefault();

    protected final BeamAPI beam;
    protected final BeamChat chat;

    public BeamChatConnectable connectable;

    // Cached
    protected AuthenticateMessage auth;

    public BeamChatBridge(BeamAPI beam, BeamChat c) {
        this.beam = beam;
        this.chat = c;
    }

    public ListenableFuture<BeamChatBridge> connect() {
        BeamChatConnectable newConn = new BeamChatConnectable(this, this.chat.selectEndpoint());

        // Copy over the old event handlers
        if (this.connectable != null) {
            for (Map.Entry<Class<? extends AbstractChatEvent>, EventHandler> entry : this.connectable.eventHandlers.entries()) {
                newConn.on(entry.getKey(), entry.getValue());
            }
        }

        try {
            newConn.setSocket(SSL_SOCKET_FACTORY.createSocket());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.connectable = newConn;
        return this.beam.executor.submit(new Callable<BeamChatBridge>() {
            @Override public BeamChatBridge call() throws Exception {
                BeamChatBridge.this.connectable.connectBlocking();
                return BeamChatBridge.this;
            }
        });
    }

    public void disconnect() {
        this.connectable.closeConnection(1000, CLOSING_STRING);
    }

    protected void notifyClose(int i, String s, boolean b) {
        if (CLOSING_STRING.equals(s)) {
            return;
        }

        try {
            // HACK: Wait 500msec because the websocket client currently used doesn't report its state until after a wait
            // has occurred..
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        this.connect();

        if (this.auth != null) {
            this.send(this.auth);
        }
    }

    // Delegates are below:
    public <T extends AbstractChatEvent> boolean on(Class<T> eventType, EventHandler<T> handler) {
        return connectable.on(eventType, handler);
    }

    public void send(AbstractChatMethod method) {
        this.send(method, null);
    }

    public <T extends AbstractChatReply> void send(AbstractChatMethod method, ReplyHandler<T> handler) {
        if (method.getClass() == AuthenticateMessage.class) {
            this.auth = (AuthenticateMessage) method;
        }

        this.connectable.send(method, handler);
    }

    public void delete(IncomingMessageData message) {
        this.connectable.delete(message);
    }
}
