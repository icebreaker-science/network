package chen.chaoran.data_manager.core;

import java.util.List;


public interface Worker<T extends Job> {
    void start(List<T> jobs);
}
