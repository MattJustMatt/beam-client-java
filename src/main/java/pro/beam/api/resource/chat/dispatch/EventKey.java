package pro.beam.api.resource.chat.dispatch;

import pro.beam.api.resource.chat.AbstractChatEvent;

public class EventKey extends HandleableKey<AbstractChatEvent> {
    protected Class<? extends AbstractChatEvent> type;

    public EventKey(Class<? extends AbstractChatEvent> type) {
        this.type = type;
    }

    @Override public boolean matches(AbstractChatEvent event) {
        return event.getClass().equals(this.type);
    }
}
