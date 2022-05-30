package eu.byncing.sokky.result;

import java.util.Collection;

public interface ISokkyResult<T> {

    default ISokkyResult<T> addListener(Listener<T> listener) {
        getListeners().add(listener);
        return this;
    }

    default void complete(T type) {
        for (Listener<T> listener : getListeners()) {
            listener.complete(this, type);
            getListeners().remove(listener);
        }
    }

    default void failure(Throwable throwable) {
        for (Listener<T> listener : getListeners()) {
            listener.failure(this, throwable);
            getListeners().remove(listener);
        }
    }

    void sync();

    void async();

    Collection<Listener<T>> getListeners();

    interface Listener<T> {

        void complete(ISokkyResult<T> result, T type);

        default void failure(ISokkyResult<T> result, Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}