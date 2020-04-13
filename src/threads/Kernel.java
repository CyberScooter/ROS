package threads;

import threads.templates.Output;
import threads.templates.Process;
import threads.templates.ReadyQueueComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Kernel {
    static LinkedList<Process> processes = new LinkedList<>();
    static ProcessCreation processCreation;
    static Dispatcher dispatcher;

    public static void main(String[] args) {

        //reads processes always on
        processCreation = new ProcessCreation();
        processCreation.start();

        dispatcher = new Dispatcher(ReadyQueueComparator.queueType.priority);
        dispatcher.start();


        Process process = new Process(1, 2, Process.Type.fileHandling, new File("file.txt"));
        Process process2 = new Process(2, 3, Process.Type.fileHandling, new File("test.txt"));
//        addProcess(process);
//        processCreation.interrupt();
//        dispatcher.start();

        compileCodeFileProcess(process, process2);

        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.println(e);
        }



        //run processes button in priority, fcfs

    }

    public static void compileCodeFileProcess(Process p1, Process p2) {
        addProcess(p1);
        addProcess(p2);


        try {
            Thread.sleep(4000);
        }catch (InterruptedException e){
            System.out.println( e);
        }

        executeProcesses(dispatcher);

    }

    public static void runTerminalCommand(Process process){
        addProcess(process);
    }

    public static void executeProcesses(Dispatcher dispatcher){
        dispatcher.interrupt();
    }

    public static void scheduleSortProcesses(ProcessCreation processCreation){
        processCreation.interrupt();
    }

    public static void addProcess(Process process){
        processes.add(process);

    }

    //removes process and returns it
    public static Process getProcess(){
        return processes.poll();
    }

}
