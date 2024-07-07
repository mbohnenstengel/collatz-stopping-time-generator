import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Duration;
import java.time.Instant;


public class MTCollatz {
    public static void main(String[] args) {
        boolean useLock = true;
        
        if(args.length > 3 ){
            System.err.println("Please provide input values for stoping number and number of threads");
            System.exit(0); 
            }

        int numberOfStoppingTimes = Integer.parseInt(args[0]);
        int threadNumber = Integer.parseInt(args[1]);

        if(args.length == 3 && args[2].equals("-nolock"))
        {
            useLock = false;
        }

        if(numberOfStoppingTimes <= 0 || threadNumber <= 0 || threadNumber > 20){ // TODO: verify thread number (example shows 8)
            System.err.println("Stoping number and number of threads must be greater than 0 ");
            System.exit(1); 
        }

        SharedHistogram histogram = new SharedHistogram();
        StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate = new StoppingTimeNumberToCalculate();
        Thread[] threads = new Thread[threadNumber];

        Instant startTime = Instant.now();

        for (int i = 0; i < threadNumber; i++) {
            Task task = new Task(histogram, stoppingTimeNumberToCalculate, numberOfStoppingTimes, useLock, i);
            // System.out.printf("Creating thread %d \n", i);
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
        // System.out.println("Results");
        // histogram.print();
        System.out.println("Total execution time: " + duration.toMillis() + " milliseconds");
        
    }
}
class SharedHistogram {
    private HashMap<Integer, Integer> histogram = new HashMap<>();

    int count;
    public synchronized void setDataPoint(int stoppingTime) {
        // System.out.println(histogram.get(stoppingTime));
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
        currentStoppingTimeNumber =  113380;
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
    private int numberOfStoppingTimes;
    private boolean useLock;
    private int treadNum;

    private ReentrantLock lock = new ReentrantLock();
    
    public Task(SharedHistogram histogram, StoppingTimeNumberToCalculate stoppingTimeNumberToCalculate, int numberOfStoppingTimes, boolean useLock, int treadNum) {
        this.histogram = histogram;
        this.stoppingTimeNumberToCalculate = stoppingTimeNumberToCalculate;
        this.numberOfStoppingTimes = numberOfStoppingTimes;
        this.useLock = useLock;
        this.treadNum = treadNum;
    }

    @Override
    public void run() {
        int numberToCalc = 1;
        int stoppingTimeOfN = 1;

        while(true){
            if(useLock){ // should I have a try catch here? or somewhere else
                // System.out.println("Lock enabled for printing stoping time calculations");
                lock.lock();
                try{
                        numberToCalc = stoppingTimeNumberToCalculate.getN();
                        stoppingTimeNumberToCalculate.incrementN();
                }catch(Exception e){
                    e.printStackTrace();
                    
                }finally{
                    lock.unlock();
                }
            }else{
                numberToCalc = stoppingTimeNumberToCalculate.getN();
                stoppingTimeNumberToCalculate.incrementN();
            }

            // System.out.printf("should break?: %s \n", numberOfStoppingTimes < numberToCalc );
            if(numberOfStoppingTimes < numberToCalc) break;
            // System.out.printf("number to calc: %d, using tread: %d\n", numberToCalc, treadNum);
            stoppingTimeOfN = computeStoppingTime(numberToCalc);

            if(useLock){
                lock.lock();
                try{
                    
                    histogram.setDataPoint(stoppingTimeOfN);
                
                }catch(Exception e){
                    e.printStackTrace();
                    
                }finally{
                    lock.unlock();
                    
                }
            }else{
                histogram.setDataPoint(stoppingTimeOfN);
            }

        }
    
    }

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

