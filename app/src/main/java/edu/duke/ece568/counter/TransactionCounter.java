package edu.duke.ece568.counter;

public class TransactionCounter {
    private static TransactionCounter counter_obj = null;
    private static int next_counter;
    private static int current_id;

    /**
     * Private Constructor
     */
    private TransactionCounter(){
        next_counter = 1;
    }
    
    public static TransactionCounter getInstance(){
        if (counter_obj == null){
            synchronized(TransactionCounter.class){
                if (counter_obj == null){
                    counter_obj = new TransactionCounter();
                }
            }
        }
        current_id = next_counter;
        next_counter++;
        return counter_obj;
    }

    public int getCurrent_id() {
        return current_id;
    }

}
