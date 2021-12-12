/**
 @author Gonçalo Henriques nº93205
 */

class CyclicBarrier {

    private int numberWaiters;
    private int currentWaiters = 0;
    private int passedWaiters = 0;
    private Runnable runnable;

    public CyclicBarrier(int numberWaiters, Runnable runnable){
        this.numberWaiters=numberWaiters;
        this.runnable=runnable;
    }

    public synchronized void await() throws InterruptedException {
        currentWaiters++;
        while (currentWaiters < numberWaiters)
            wait();
        if (passedWaiters == 0) {
            runnable.run();
            notifyAll();
        }

        passedWaiters++;
        if (passedWaiters == numberWaiters) {
            passedWaiters = 0;
            currentWaiters = 0;
        }
    }
}