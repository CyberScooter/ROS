package main.java.process_scheduler.threads;

import main.java.Kernel;
import main.java.process_scheduler.threads.templates.PCB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

//keep thread running and will read processes from Kernel class
public class ProcessCreation extends Thread{
    static ConcurrentLinkedQueue<PCB> readyQueue;

    public ProcessCreation() {
        if(readyQueue == null){
            readyQueue = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public synchronized void run() {
        //keeps thread running
        while(true) {
            //this uses FCFS to manage for io processes in ready queue to CPU that came from the
            //io queue in the io thread

            PCB processesToAdd = Kernel.getProcess();

            if(processesToAdd != null) {
                processesToAdd.setState(PCB.State.ready);
                readyQueue.add(processesToAdd);
                Kernel.processCreationLatch.countDown();

                if (processesToAdd.getIOOutput() != null || processesToAdd.getType() == PCB.Type.commandLine) {
                    Kernel.dispatcher.interrupt();
                    //two stop two interrupts being called simultaeneously
                    try{
                        Thread.sleep(200);
                    }catch (InterruptedException e){

                    }
                }

            }

        }

    }
    public static ConcurrentLinkedQueue<PCB> getReadyQueue() {
        return readyQueue;
    }


}
