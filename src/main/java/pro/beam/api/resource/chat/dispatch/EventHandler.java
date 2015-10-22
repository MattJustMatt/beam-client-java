package pro.beam.api.resource.chat.dispatch;

import pro.beam.api.resource.chat.AbstractChatEvent;

public abstract class EventHandler extends ChatHandler<AbstractChatEvent> {
    protected final Class<? extends AbstractChatEvent> predicateClass;

    public EventHandler(Class<? extends AbstractChatEvent> predicateClass) {
        super(FinalizedState.NONREMOVABLE);

        this.predicateClass = predicateClass;
    }

    @Override public boolean equals(ChatHandler other) {
        if (!(other instanceof EventHandler)) {
            return false;
        }

        return this.predicateClass == ((EventHandler) other).predicateClass;
    }
}
