package threads;

import threads.templates.Process;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

//keep thread running and will read processes from OSKernal class
public class ProcessCreation extends Thread{
    static ConcurrentLinkedQueue<Process> readyQueue;
    private final Semaphore semaphore = new Semaphore(1);

    public ProcessCreation() {
        if(readyQueue == null){
            readyQueue = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public synchronized void run() {
        while(true) {
            try {
                wait();
            } catch (InterruptedException e) {
                Process process = Kernel.getProcess();
                readyQueue.add(process);

                //if Process came from IOQUEUE THEN TRIGGER DISPATCHER INTERRUPT
                //as process is part of another process and does not need to wait
                if(process.getIOOutput() != null){
                    Kernel.dispatcher.interrupt();
                }

            }
        }
    }

    public static ConcurrentLinkedQueue<Process> getReadyQueue() {
        return readyQueue;
    }

    public static void main(String[] args) {
        new ProcessCreation().start();
    }


}
