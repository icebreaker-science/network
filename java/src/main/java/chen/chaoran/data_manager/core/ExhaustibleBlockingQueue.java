package chen.chaoran.data_manager.core;

import java.util.concurrent.BlockingQueue;

public interface ExhaustibleBlockingQueue<E> extends BlockingQueue<E> {
    boolean isExhausted();
    void setExhausted(boolean exhausted);
}
