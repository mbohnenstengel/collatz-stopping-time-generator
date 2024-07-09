import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Duration;
import java.time.Instant;

/**
 * Main class for the multi-threaded Collatz sequence computation.
 */
public class MTCollatz {
    
    /**
     * Main method to start the program.
     * 
     * @param args Command line arguments: stopping number, number of threads, and optional '-nolock'.
     */
    public static void main(String[] args) {
        boolean useLock = true;
        
        if(args.length > 3 ){
            System.err.println("Usage: java MTCollatz <N> <T> [-nolock]");
            System.exit(0); 
        }

        int numberOfStoppingTimes = Integer.parseInt(args[0]);
        int threadNumber = Integer.parseInt(args[1]);

        if(args.length == 3 && args[2].equals("-nolock"))
        {
            useLock = false;
        }

        if(numberOfStoppingTimes <= 0 || threadNumber <= 0 || threadNumber > 8){
            System.err.println("Stopping number and number of threads must be greater than 0, threads must be 8 or less");
            System.exit(1); 
        }

        SharedHistogram histogram = new SharedHistogram();
        StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate = new StoppingTimeNumberToCalculate();
        Thread[] threads = new Thread[threadNumber];

        Instant startTime = Instant.now();

        for (int i = 0; i < threadNumber; i++) {
            Task task = new Task(histogram, stoppingTimeNumberToCalculate, numberOfStoppingTimes, useLock, i);
            threads[i] = new Thread(task);
            threads[i].start();
        }

        for (int i = 0; i < threadNumber; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);
        System.out.println("All threads have finished execution.");
        histogram.print();
        System.out.println("Total execution time: " + duration.toMillis() + " milliseconds");
    }
}

/**
 * Class to maintain the histogram of stopping times.
 */
class SharedHistogram {
    private HashMap<Integer, Integer> histogram = new HashMap<>();

    /**
     * Sets a data point in the histogram.
     * 
     * @param stoppingTime The stopping time to record.
     */
    public synchronized void setDataPoint(int stoppingTime) {
        if(histogram.get(stoppingTime) == null){
            histogram.put(stoppingTime, 1);
        } else {
            int count = histogram.get(stoppingTime);
            histogram.put(stoppingTime, count + 1);
        }
    }

    /**
     * Gets the current histogram.
     * 
     * @return The histogram as a HashMap.
     */
    public HashMap<Integer, Integer> getHashMap() {
        return histogram;
    }

    /**
     * Prints the histogram.
     */
    public void print() {
        System.out.println("Stopping time, Frequency");
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            System.out.println(entry.getKey() + "," + entry.getValue());
        }
    }
}

/**
 * Class to manage the current stopping time number.
 */
class StoppingTimeNumberToCalculate {
    private int currentStoppingTimeNumber;

    /**
     * Constructor to initialize the stopping time number.
     */
    public StoppingTimeNumberToCalculate() {
        currentStoppingTimeNumber =  1;
    }

    /**
     * Gets the current stopping time number.
     * 
     * @return The current stopping time number.
     */
    public int getN() {
        return currentStoppingTimeNumber;
    }

    /**
     * Increments the current stopping time number.
     */
    public void incrementN() {
        currentStoppingTimeNumber++;
    }
}

/**
 * Runnable task class to compute stopping times.
 */
class Task implements Runnable {
    private SharedHistogram histogram;
    private StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate;
    private int numberOfStoppingTimes;
    private boolean useLock;
    private int threadNum;
    private ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor to initialize the task.
     * 
     * @param histogram Shared histogram for storing results.
     * @param stoppingTimeNumberToCalculate Object to manage stopping time numbers.
     * @param numberOfStoppingTimes Total number of stopping times to compute.
     * @param useLock Whether to use a lock for synchronization.
     * @param threadNum The thread number.
     */
    public Task(SharedHistogram histogram, StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate, int numberOfStoppingTimes, boolean useLock, int threadNum) {
        this.histogram = histogram;
        this.stoppingTimeNumberToCalculate = stoppingTimeNumberToCalculate;
        this.numberOfStoppingTimes = numberOfStoppingTimes;
        this.useLock = useLock;
        this.threadNum = threadNum;
    }

    /**
     * The run method for the thread.
     */
    @Override
    public void run() {
        int numberToCalc = 1;
        int stoppingTimeOfN = 1;

        while(true) {
            if(useLock) {
                lock.lock();
                try {
                    numberToCalc = stoppingTimeNumberToCalculate.getN();
                    stoppingTimeNumberToCalculate.incrementN();
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } else {
                numberToCalc = stoppingTimeNumberToCalculate.getN();
                stoppingTimeNumberToCalculate.incrementN();
            }

            if(numberOfStoppingTimes < numberToCalc) break;

            stoppingTimeOfN = computeStoppingTime(numberToCalc);

            if(useLock) {
                lock.lock();
                try {
                    histogram.setDataPoint(stoppingTimeOfN);
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } else {
                histogram.setDataPoint(stoppingTimeOfN);
            }
        }
    }

    /**
     * Computes the stopping time for a given number.
     * 
     * @param initVal The initial value to compute the stopping time for.
     * @return The stopping time.
     */
    static int computeStoppingTime(int initVal) {
        Long n = Long.valueOf(initVal);
        int counter = 0;

        while (n != 1) {
            if (n % 2 == 0) {
                n = n / 2;
            } else {
                n = (3 * n) + 1;
            }
            counter++;
        } 
        return counter;
    }
}
