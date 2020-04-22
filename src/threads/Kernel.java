package threads;

import threads.templates.CommandLine;
import threads.templates.Output;
import threads.templates.Process;
import threads.templates.ReadyQueueComparator;
import views.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Kernel {
    static LinkedList<Process> processes = new LinkedList<>();
    public static ProcessCreation processCreation;
    static Dispatcher dispatcher;
    static CountDownLatch processCreationLatch;
    ReadyQueueComparator.queueType queueType;

    public Kernel(ReadyQueueComparator.queueType queueType){
        if(processCreation == null && dispatcher == null){
            processCreation = new ProcessCreation();
            processCreation.start();

            dispatcher = new Dispatcher(queueType);
            dispatcher.start();
            this.queueType = queueType;
        }
    }


    public static void main(String[] args) {

        //reads processes always on
        processCreation = new ProcessCreation();
        processCreation.start();

        dispatcher = new Dispatcher(ReadyQueueComparator.queueType.FCFS_process);
        dispatcher.start();

        LinkedList<Process> processes = new LinkedList<>();


        Process process = new Process(1, 3, Process.Type.fileCompiling, new File("Program1.txt"));
        Process process2 = new Process(2, 2, Process.Type.fileCompiling, new File("Program2.txt"));
        Process process3 = new Process(3, Process.Type.commandLine, new CommandLine("dir"));
        process3.setHandledByIO(false);


        processes.add(process);
        processes.add(process2);

        compileCodeFileProcess(processes);



//        runTerminalCode(process3);


    }

    public static void runTerminalCode(Process process){
        addProcess(process);
        processCreation.interrupt();
    }

    public static void compileCodeFileProcess(LinkedList<Process> processes) {
        processCreationLatch = new CountDownLatch(processes.size());
        while(!processes.isEmpty()){
            addProcess(processes.poll());
        }
        processCreation.interrupt();



        try{
            processCreationLatch.await();
        }catch (InterruptedException e){
            System.out.println(e);
        }


//        try {
//            Thread.sleep(4000);
//        }catch (InterruptedException e){
//            System.out.println( e);
//        }

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
}
