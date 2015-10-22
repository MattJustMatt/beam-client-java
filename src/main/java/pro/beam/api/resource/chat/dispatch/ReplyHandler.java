package pro.beam.api.resource.chat.dispatch;

import pro.beam.api.resource.chat.AbstractChatReply;

public abstract class ReplyHandler extends ChatHandler<AbstractChatReply> {
    protected final int id;

    public ReplyHandler(final int id) {
        super(FinalizedState.REMOVABLE);

        this.id = id;
    }

    @Override public boolean equals(ChatHandler other) {
        if (!(other instanceof ReplyHandler)) {
            return false;
        }

        return this.id == ((ReplyHandler) other).id;
    }
}
