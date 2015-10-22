package pro.beam.api.resource.chat.dispatch;

import pro.beam.api.resource.chat.AbstractChatReply;

public class ReplyKey extends HandleableKey<AbstractChatReply> {
    protected final int id;

    public ReplyKey(int id) {
        this.id = id;
    }

    @Override public boolean matches(AbstractChatReply reply) {
        return this.id == reply.id;
    }
}
