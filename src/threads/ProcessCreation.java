package threads;

import threads.templates.Process;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
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
            System.out.println("pc ran ...");
            try {
                wait();
            } catch (InterruptedException e) {
                Process process = OSKernel.getProcess();
                readyQueue.add(process);

                //if Process came from IOQUEUE THEN TRIGGER DISPATCHER INTERRUPT
                //as process is part of another process and does not need to wait for user input
                if(process.getIOOutput() != null){
                    OSKernel.dispatcher.interrupt();
                }
                System.out.println(readyQueue.size());
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
