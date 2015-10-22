package pro.beam.api.resource.chat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import pro.beam.api.BeamAPI;
import pro.beam.api.http.ws.CookieDraft_17;

import java.net.URI;

import pro.beam.api.resource.chat.dispatch.BeamChatDispatch;
import pro.beam.api.resource.chat.events.IncomingWidgetEvent;
import pro.beam.api.resource.chat.events.data.IncomingMessageData;
import pro.beam.api.resource.chat.replies.ReplyHandler;

import java.util.concurrent.Callable;

@SuppressWarnings("unchecked")
public class BeamChatConnectable extends WebSocketClient {
    protected final BeamChatBridge bridge;

    public BeamChatConnectable(BeamChatBridge bridge, URI endpoint) {
        super(endpoint, new CookieDraft_17(bridge.beam.http));

        this.bridge = bridge;
    }

    public void send(AbstractChatMethod method) {
        this.send(method, null);
    }

    public <T extends AbstractChatReply> void send(final AbstractChatMethod method, ReplyHandler<T> handler) {
        if (handler != null) {
            this.replyHandlers.put(method.id, ReplyPair.from(handler));
        }

        this.bridge.beam.executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                byte[] data = BeamChatConnectable.this.bridge.beam.gson.toJson(method).getBytes();
                BeamChatConnectable.this.send(data);

                return null;
            }
        });
    }

    public ListenableFuture<BeamChatConnectable> connectFuture() {
        return this.bridge.beam.executor.submit(new Callable<BeamChatConnectable>() {
            @Override public BeamChatConnectable call() throws Exception {
                BeamChatConnectable self = BeamChatConnectable.this;

                self.connectBlocking();
                return self;
            }
        });
    }

    public void delete(IncomingMessageData message) {
        String path = BeamAPI.BASE_PATH.resolve("chats/" + message.channel + "/message/" + message.id).toString();
        this.bridge.beam.http.delete(path, null);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
    }

    @Override
    public void onMessage(String s) {
        JsonObject e;
        try {
            e = this.bridge.beam.gson.fromJson(s, JsonObject.class);
        } catch (JsonSyntaxException ignored) {
            // The API sent us bad data, and we can't do anything with it.
            return;
        }

        if (e.has("id")) {
            AbstractChatReply reply = this.bridge.beam.gson.fromJson(s, AbstractChatReply.class);
            this.dispatcher.dispatchReply(reply);
        } else if (e.has("event")) {
            Class<? extends AbstractChatEvent> type;

            JsonElement userId = e.getAsJsonObject("data").get("user_id");
            if (userId != null && userId.getAsInt() == -1) {
                type = IncomingWidgetEvent.class;
            } else {
                String name = e.get("event").getAsString();
                type = AbstractChatEvent.EventType.fromSerializedName(name).getCorrespondingClass();
            }

            AbstractChatEvent event = this.bridge.beam.gson.fromJson(e, type);
            this.dispatcher.dispatchEvent(event);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        this.bridge.notifyClose(i, s, b);
    }

    @Override
    public void onError(Exception e) {
    }
}
