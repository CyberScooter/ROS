package main.java;

import main.java.mmu.MMU;
import main.java.mmu.PageReplacementAlgorithm.ReplacementAlgorithm;
import main.java.process_scheduler.threads.Dispatcher;
import main.java.process_scheduler.threads.ProcessCreation;
import main.java.process_scheduler.threads.templates.PCB;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Kernel {
    private static ConcurrentLinkedQueue<PCB> processes = new ConcurrentLinkedQueue<>();
    public static ProcessCreation processCreation;
    public static Dispatcher dispatcher;
    public static CountDownLatch processCreationLatch;
    private static CountDownLatch addingProcessesLatch;
    ReadyQueueComparator.queueType queueType;
    private MMU mmu;

    public Kernel(ReadyQueueComparator.queueType queueType){
        if(processCreation == null && dispatcher == null){
            processCreation = new ProcessCreation();
            processCreation.start();

            dispatcher = new Dispatcher(queueType);
            dispatcher.start();
            this.queueType = queueType;
        }
    }

    public Kernel(ReplacementAlgorithm algorithm){
        mmu = new MMU(algorithm);
    }


    public static void runCodeFileProcesses(LinkedList<PCB> processesToAdd) {
        processCreationLatch = new CountDownLatch(processesToAdd.size());
        while (!processesToAdd.isEmpty()) {
            processes.add(processesToAdd.poll());
        }


        try {
            processCreationLatch.await();
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        executeProcesses(dispatcher);
    }

    public static void runTerminalProcess(PCB commandLine){
        processCreationLatch = new CountDownLatch(1);
        addProcess(commandLine);


    }

    public static void executeProcesses(Dispatcher dispatcher){
        dispatcher.interrupt();
    }

    public static void addProcess(PCB PCB){
        processes.add(PCB);

    }

    //removes process and returns it
    public static PCB getProcess(){
        return processes.poll();
    }

    public ReadyQueueComparator.queueType getQueueType() {
        return queueType;
    }

    public MMU getMmu() {
        return mmu;
    }

    public static ProcessCreation getProcessCreation(){
        return processCreation;
    }

    public void setQueueType(ReadyQueueComparator.queueType queueType) {
        this.queueType = queueType;
    }

    public static ConcurrentLinkedQueue<PCB> getProcesses() {
        return processes;
    }
}
