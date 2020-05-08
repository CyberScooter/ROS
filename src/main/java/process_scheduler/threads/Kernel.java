package main.java.process_scheduler.threads;

import main.java.mmu.MMU;
import main.java.mmu.PageReplacementAlgorithm.ReplacementAlgorithm;
import main.java.process_scheduler.threads.templates.Process;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class Kernel {
    static LinkedList<Process> processes = new LinkedList<>();
    public static ProcessCreation processCreation;
    static Dispatcher dispatcher;
    static CountDownLatch processCreationLatch;
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


    public static void main(String[] args) {

        //used for testing purposes
        //reads processes always on
        processCreation = new ProcessCreation();
        processCreation.start();

        dispatcher = new Dispatcher(ReadyQueueComparator.queueType.FCFS_process);
        dispatcher.start();

        LinkedList<Process> processes = new LinkedList<>();

        Process process1 = new Process(1, 3, Process.Type.fileCompiling, new File("Program1.txt"));
        Process process2 = new Process(2, 2, Process.Type.fileCompiling, new File("Program2.txt"));
        Process process3 = new Process(1, 3, Process.Type.fileCompiling, new File("Program1.txt"));
        Process process4 = new Process(2, 2, Process.Type.fileCompiling, new File("Program2.txt"));

        processes.add(process3);
        processes.add(process4);

        runCodeFileProcesses(processes);


    }

    public static void runCodeFileProcesses(LinkedList<Process> processes) {
        processCreationLatch = new CountDownLatch(processes.size());
        while (!processes.isEmpty()) {
            addProcess(processes.poll());
        }
        processCreation.interrupt();

        try {
            processCreationLatch.await();
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        executeProcesses(dispatcher);
    }

    public static void runTerminalProcess(Process commandLine){
        processCreationLatch = new CountDownLatch(1);
        addProcess(commandLine);

        processCreation.interrupt();

    }

    public static void executeProcesses(Dispatcher dispatcher){
        dispatcher.interrupt();
    }

    public static void addProcess(Process process){
        processes.add(process);

    }

    //removes process and returns it
    public static Process getProcess(){
        return processes.poll();
    }

    public ReadyQueueComparator.queueType getQueueType() {
        return queueType;
    }

    public MMU getMmu() {
        return mmu;
    }
}
