package threads;

import threads.templates.Process;
import threads.templates.ReadyQueueComparator;

import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

//keep thread running and will read processes from OSKernal class
public class ProcessCreation extends Thread{
    static ConcurrentLinkedQueue<Process> readyQueue;
    private final Semaphore semaphore = new Semaphore(1);
    private static Process processToAdd;

    public ProcessCreation() {
        if(readyQueue == null){
            readyQueue = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public synchronized void run() {
        while(true) {
            //this uses FCFS to manage for io processes in ready queue to CPU that came from the
            //io queue in the io thread

            //keep the thread running

            Process processToAdd = Kernel.getProcess();

            if(processToAdd != null) {
                readyQueue.add(processToAdd);
                Kernel.processCreationLatch.countDown();

                if (processToAdd.getIOOutput() != null || processToAdd.getType() == Process.Type.commandLine) {
                    Kernel.dispatcher.interrupt();
                    //two stop two interrupts being called simultaeneously
                    try{
                        Thread.sleep(200);
                    }catch (InterruptedException e){

                    }
                }

            }

            //refreshes every 200ms
            try{
                Thread.sleep(200);
            }catch (InterruptedException e){

            }

        }

    }
    public static ConcurrentLinkedQueue<Process> getReadyQueue() {
        return readyQueue;
    }

}
