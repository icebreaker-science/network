package chen.chaoran.data_manager.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class JobManager {
    private final int NUM_WORKERS;
    private final int PACKET_SIZE;
    private final int QUEUE_POLL_TIMEOUT_SECONDS = 5;

    public JobManager() {
        this.NUM_WORKERS = 4;
        this.PACKET_SIZE = 200;
    }

    public <T extends Job> void executeJobs(BlockingQueue<T> jobs, Master<T> master) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS);

        // Create workers
        List<Worker<T>> workers = new ArrayList<>();
        for (int i = 0; i < NUM_WORKERS; i++) {
            workers.add(master.createWorker());
        }

        System.out.println("Number of workers: " + workers.size());

        AtomicInteger numberFinishedJobs = new AtomicInteger();

        // Run workers
        for (Worker<T> worker : workers) {
            executor.submit(() -> {
                try {
                    String threadName = Thread.currentThread().getName();
                    System.out.println(threadName + " has started");

                    while (true) {
                        List<T> jobsForWorker = new ArrayList<>();
                        for (int i = 0; i < PACKET_SIZE; i++) {
                            try {
                                T job = null;
                                if (jobs instanceof ExhaustibleBlockingQueue) {
                                    while (!((ExhaustibleBlockingQueue<Job>) jobs).isExhausted() || !jobs.isEmpty()) {
                                        job = jobs.poll(QUEUE_POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                                        if (job != null) {
                                            break;
                                        }
                                    }
                                }
                                else {
                                    job = jobs.poll(QUEUE_POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                                }
                                if (job == null) {
                                    break;
                                }
                                jobsForWorker.add(job);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (jobsForWorker.isEmpty()) {
                            break;
                        }
                        LocalDateTime now = LocalDateTime.now();
                        System.out.println("[" + threadName + "] [" + now + "] received " + jobsForWorker.size() + " new jobs.");
                        try {
                            worker.start(jobsForWorker);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        int n = numberFinishedJobs.addAndGet(jobsForWorker.size());
                        now = LocalDateTime.now();
                        System.out.println("[" + threadName + "] [" + now + "] Total finished jobs: " + n);
                    }


                    if (worker instanceof AutoCloseable) {
                        try {
                            ((AutoCloseable) worker).close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Unexpected exception");
                    ex.printStackTrace();
                    throw ex;
                } catch (Error error) {
                    System.err.println("Errrrroooooooooorrrrr!");
                    error.printStackTrace();
                    throw error;
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
