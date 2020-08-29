package chen.chaoran.data_manager.core;


public interface Master<T extends Job> extends Startable {

    Worker<T> createWorker();

}
