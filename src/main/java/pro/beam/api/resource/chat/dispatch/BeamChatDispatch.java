package pro.beam.api.resource.chat.dispatch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import pro.beam.api.resource.chat.AbstractChatEvent;
import pro.beam.api.resource.chat.AbstractChatReply;

import java.util.Collection;

public class BeamChatDispatch {
    protected final Multimap<HandleableKey, ChatHandler> handlers;

    public BeamChatDispatch() {
        this.handlers = ArrayListMultimap.create();
    }

    public <T extends AbstractChatEvent> void on(Class<T> t, EventHandler handler) {
        this.handlers.put(new EventKey(t), handler);
    }

    public void replySubscribe(int id, ReplyHandler handler) {
        this.handlers.put(new ReplyKey(id), handler);
    }

    private <T extends AbstractChatEvent> void dispatchEvent(T event) {
        Collection<ChatHandler> handlers = this.handlersFor(new EventKey(event.getClass()));
        for (ChatHandler handler : handlers) {
            handler.handle(event);
        }
    }

    private <T extends AbstractChatReply> void dispatchReply(T reply) {
        HandleableKey key = new ReplyKey(reply.id);
        Collection<ChatHandler> handlers = this.handlersFor(key);

        for (ChatHandler handler : handlers) {
            handler.handle(reply);
            this.handlers.remove(key, handler);
        }
    }

    private Collection<ChatHandler> handlersFor(HandleableKey key) {
        return this.handlers.get(key);
    }
}
