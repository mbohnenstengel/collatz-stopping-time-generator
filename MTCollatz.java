import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Duration;
import java.time.Instant;


public class MTCollatz {
    public static void main(String[] args) {
        boolean useLock = false;
        
        if(args.length > 3 ){
            System.err.println("Please provide input values for stoping number and number of threads");
            System.exit(0); 
            }

        int numberOfStoppingTimes = Integer.parseInt(args[0]);
        int threadNumber = Integer.parseInt(args[1]);

        if(args.length == 3 && args[2].equals("[-nolock]"))
        {
            useLock = false;
        }else{
            useLock = true;
        }

        if(numberOfStoppingTimes <= 0 || threadNumber <= 0 || threadNumber > 6){
            System.err.println("Stoping number and number of threads must be greater than 0 ");
            System.exit(1); 
        }

        SharedHistogram histogram = new SharedHistogram();
        StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate = new StoppingTimeNumberToCalculate();
        Thread[] threads = new Thread[threadNumber];

        Task task = new Task(histogram, stoppingTimeNumberToCalculate, numberOfStoppingTimes, useLock);
        Instant startTime = Instant.now();

        for (int i = 0; i < threadNumber; i++) {
            System.out.printf("Creating thread %d \n", i);
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
        System.out.println("Results");
        histogram.print();
        System.out.println("Total execution time: " + duration.toMillis() + " milliseconds");
        
    }
}
class SharedHistogram {
    private HashMap<Integer, Integer> histogram = new HashMap<>();

    int count;
    public synchronized void setDataPoint(int stoppingTime) {
        System.out.println(histogram.get(stoppingTime));
        if(histogram.get(stoppingTime) == null){
            histogram.put(stoppingTime, 1);
        }else {
            count = histogram.get(stoppingTime);
            histogram.put(stoppingTime, count+1);
        }
    }

    public HashMap<Integer, Integer> getHashMap() {
        return histogram;
    }

    public void print() {
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}

class StoppingTimeNumberToCalculate {
    private int currentStoppingTimeNumber;


    public StoppingTimeNumberToCalculate() {
        currentStoppingTimeNumber =  1;
    }

    public int getN() {
        return currentStoppingTimeNumber;
    }


    public void incrementN() {
        currentStoppingTimeNumber++;
    }
}

class Task implements Runnable {
    private SharedHistogram histogram;
    private StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate;
    private boolean useLock;
    private ReentrantLock lock = new ReentrantLock();
    private int numberOfStoppingTimes;

    public Task(SharedHistogram histogram, StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate, int numberOfStoppingTimes, boolean useLock) {
        this.histogram = histogram;
        this.stoppingTimeNumberToCalculate = stoppingTimeNumberToCalculate;
        this.useLock = useLock;
        this.numberOfStoppingTimes = numberOfStoppingTimes;
    }
// TODO How is histogram set up??
    @Override
    public void run() {
        int numberToCalc;
        int stoppingTimeOfN;

        while(true){
            if(useLock){
                System.out.println("Lock enabled for printing stoping time calculations");
                try{
                        lock.lock();
                        numberToCalc = stoppingTimeNumberToCalculate.getN();
                        stoppingTimeNumberToCalculate.incrementN();
                }finally{
                    lock.unlock();
                }
            }else{
                numberToCalc = stoppingTimeNumberToCalculate.getN();
                stoppingTimeNumberToCalculate.incrementN();
            }

            System.out.printf("number to calc: %d \n", numberToCalc );
            System.out.printf("should break?: %s \n", numberOfStoppingTimes < numberToCalc );
            if(numberOfStoppingTimes < numberToCalc) break;
            stoppingTimeOfN = computeStoppingTime(numberToCalc);

            if(useLock){
                try{
                    lock.lock();
                    histogram.setDataPoint(stoppingTimeOfN);
                
                }finally{
                    lock.unlock();
                    
                }
            }else{
                histogram.setDataPoint(stoppingTimeOfN);
            }

        }
    
    }

    static int computeStoppingTime(int initVal) {

        System.out.printf("computing for stopping time for: %d \n", initVal);
        int n = initVal;
        int counter = 0;

        while (n != 1) {
            if (n % 2 == 0) {
                n = n / 2;
            } else {
                n = (3 * n) + 1;
            }
            counter++;
        }
        System.out.printf("Stopping time for the number %d, is: %d \n", initVal, counter);
        return counter;
    }
}

