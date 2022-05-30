package eu.byncing.sokky.result;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SokkyResult<T> implements ISokkyResult<T> {

    private final Collection<Listener<T>> listeners = new ConcurrentLinkedQueue<>();

    private Runnable runnable;

    public void invoke(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void sync() {
        if (runnable != null) runnable.run();
    }

    @Override
    public void async() {
        if (runnable != null) new Thread(runnable).start();
    }

    @Override
    public Collection<Listener<T>> getListeners() {
        return listeners;
    }
}