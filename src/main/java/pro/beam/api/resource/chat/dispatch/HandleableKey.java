package pro.beam.api.resource.chat.dispatch;

public abstract class HandleableKey<T> {
    public abstract boolean matches(T t);
}
