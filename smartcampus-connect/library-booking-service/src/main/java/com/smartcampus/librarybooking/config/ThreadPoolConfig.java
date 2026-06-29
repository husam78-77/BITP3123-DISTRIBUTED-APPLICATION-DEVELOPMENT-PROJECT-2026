// R5 - Multithreaded Server: Thread Pool Configuration
package com.smartcampus.librarybooking.config;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * â•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گ
 * R5 â€” THREAD POOL REQUIREMENT (Multithreaded Server)
 * â•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گ
 *
 * This configuration class satisfies the <b>Thread Pool</b> requirement of R5
 * by creating a managed {@link ExecutorService} bean backed by a fixed pool
 * of 10 worker threads.
 *
 * <h3>Why a Spring-managed Bean?</h3>
 * <ul>
 *   <li>The {@code ExecutorService} is created once and shared as a singleton
 *       across all consumers (LibraryService, load tests, etc.).</li>
 *   <li>Spring's IoC container owns the lifecycle, so the pool is created at
 *       application startup and <b>gracefully shut down</b> via
 *       {@link PreDestroy} when the context closes â€” preventing thread leaks.</li>
 * </ul>
 *
 * <h3>Why {@code newFixedThreadPool(10)}?</h3>
 * <ul>
 *   <li>At most 10 booking tasks execute concurrently; additional tasks queue
 *       in an unbounded {@code LinkedBlockingQueue} until a thread is free.</li>
 *   <li>This caps resource usage and prevents unbounded thread creation that
 *       could exhaust memory or CPU under heavy load.</li>
 * </ul>
 *
 * <h3>Lifecycle Management</h3>
 * <ol>
 *   <li>{@code shutdown()} â€” stops accepting new tasks and lets running tasks
 *       finish normally.</li>
 *   <li>{@code awaitTermination(30s)} â€” blocks up to 30 seconds for running
 *       tasks to complete.</li>
 *   <li>{@code shutdownNow()} â€” if tasks still haven't finished, forcibly
 *       interrupts them and drains the queue to prevent thread leaks.</li>
 * </ol>
 * â•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گ
 */
@Configuration
public class ThreadPoolConfig {

    /** The managed thread pool instance â€” stored for graceful shutdown. */
    private ExecutorService executorService;

    /**
     * Creates a fixed-size thread pool with 10 worker threads.
     *
     * <p>This bean is injected into {@code LibraryService} to process
     * concurrent room-booking requests, and into
     * {@code RoomBookingLoadTest} to simulate concurrent student traffic.</p>
     *
     * @return a singleton {@link ExecutorService} with 10 threads
     */
    @Bean
    public ExecutorService executorService() {
        this.executorService = Executors.newFixedThreadPool(10);
        return this.executorService;
    }

    /**
     * R5 â€” GRACEFUL SHUTDOWN (Thread-Leak Prevention).
     *
     * <p>Called automatically by Spring when the application context is
     * destroyed (e.g. server shutdown, redeploy).  Without this method the
     * 10 pool threads would keep running as daemon-less threads, preventing
     * the JVM from exiting cleanly â€” a <b>thread leak</b>.</p>
     *
     * <p>Shutdown sequence:</p>
     * <ol>
     *   <li>{@code shutdown()} â€” reject new tasks, let in-flight tasks finish.</li>
     *   <li>{@code awaitTermination(30, SECONDS)} â€” wait up to 30 s.</li>
     *   <li>{@code shutdownNow()} â€” if tasks are still running, interrupt them.</li>
     * </ol>
     */
    @PreDestroy
    public void shutdownExecutorService() {
        if (executorService != null) {
            // Step 1: Stop accepting new tasks
            executorService.shutdown();
            try {
                // Step 2: Wait for currently executing tasks to finish
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    // Step 3: Force shutdown if tasks did not finish in time
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                // If the waiting thread itself is interrupted, force shutdown
                executorService.shutdownNow();
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }
}
