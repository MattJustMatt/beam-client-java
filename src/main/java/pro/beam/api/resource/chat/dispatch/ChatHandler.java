package pro.beam.api.resource.chat.dispatch;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import pro.beam.api.resource.chat.AbstractChatDatagram;

import java.util.concurrent.Callable;

public abstract class ChatHandler<T extends AbstractChatDatagram> {
    protected final FinalizedState finalizedState;

    public ChatHandler(FinalizedState fs) {
        this.finalizedState = fs;
    }

    public abstract void handle(T t);

    public abstract boolean equals(ChatHandler<T> other);

    private ListenableFuture<FinalizedState> invoke(T t, ListeningExecutorService e) {
        return e.submit(this.asCallable(t));
    }

    private Callable<FinalizedState> asCallable(final T t) {
        return new Callable<FinalizedState>() {
            @Override public FinalizedState call() throws Exception {
                ChatHandler<T> instance = ChatHandler.this;

                instance.handle(t);
                return instance.finalizedState;
            }
        };
    }

    protected enum FinalizedState {
        REMOVABLE, NONREMOVABLE,
    }
}
