import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Instant;
import java.time.Duration;

public class MTCollatz {
    // Global variables for range and thread count
    private static int N; // Range of numbers for Collatz stopping times
    private static int T; // Number of worker threads

    // Shared resources
    private static int[] histogram; // Histogram for Collatz stopping times
    private static int COUNTER; // Shared counter for threads to fetch the next number

    // Lock for synchronization
    private static Lock lock;
    private static boolean useLock;

    public static void main(String[] args) {
        // Parse command-line arguments
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: java MTCollatz <N> <T> [-nolock]");
            System.exit(1);
        }

        N = Integer.parseInt(args[0]);
        T = Integer.parseInt(args[1]);
        useLock = args.length == 3 && args[2].equals("-nolock") ? false : true;

        if (N <= 0 || T <= 0) {
            System.err.println("N and T must be positive integers.");
            System.exit(1);
        }

        // Initialize shared resources
        histogram = new int[N + 1]; // Histogram for numbers 1 to N
        COUNTER = 1; // Start COUNTER at 1

        // Initialize lock for thread synchronization
        if (useLock) {
            lock = new ReentrantLock();
        }

        // Create worker threads
        Thread[] threads = new Thread[T];
        for (int i = 0; i < T; i++) {
            threads[i] = new Thread(new WorkerThread());
        }

        // Record start time just before starting the threads
        Instant start = Instant.now();

        // Start all worker threads
        for (int i = 0; i < T; i++) {
            threads[i].start();
        }

        // Wait for all threads to complete
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Record end time immediately after threads finish
        Instant end = Instant.now();
        Duration elapsedTime = Duration.between(start, end);

        // Print histogram results
        for (int k = 1; k <= N; k++) {
            System.out.println(k + ", " + histogram[k]);
        }

        // Print elapsed time to stderr
        System.err.println(N + "," + T + "," + elapsedTime.toMillis() / 1000.0);
    }

    static class WorkerThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                int number;

                if (useLock) {
                    // Synchronize access to COUNTER with lock
                    lock.lock();
                    try {
                        if (COUNTER > N) {
                            return; // Exit thread if COUNTER exceeds N
                        }
                        number = COUNTER++;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    // Without lock, just fetch and increment the COUNTER
                    number = fetchAndIncrementCounterWithoutLock();
                }

                if (number > N) {
                    return; // Exit thread if number exceeds N
                }

                // Compute Collatz stopping time for 'number'
                int stoppingTime = computeCollatzStoppingTime(number);

                if (useLock) {
                    // Update histogram with synchronization
                    lock.lock();
                    try {
                        histogram[stoppingTime]++;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    // Update histogram without synchronization
                    updateHistogramWithoutLock(stoppingTime);
                }
            }
        }

        private int computeCollatzStoppingTime(int n) {
            int steps = 0;
            long current = n;
            while (current != 1) {
                if (current % 2 == 0) {
                    current = current / 2;
                } else {
                    current = 3 * current + 1;
                }
                steps++;
            }
            return steps + 1; // +1 for the final step reaching 1
        }

        private int fetchAndIncrementCounterWithoutLock() {
            // Simple increment without synchronization, risky but for -nolock
            return COUNTER++;
        }

        private void updateHistogramWithoutLock(int stoppingTime) {
            // Update histogram without synchronization, may cause race conditions
            histogram[stoppingTime]++;
        }
    }
}
