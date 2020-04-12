package threads;

import threads.templates.Output;
import threads.templates.Process;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Kernel {
    static ConcurrentLinkedQueue<Process> processes = new ConcurrentLinkedQueue<>();
    static ProcessCreation processCreation;
    static Dispatcher dispatcher;

    public static void main(String[] args) {

        //reads processes always on
        processCreation = new ProcessCreation();
        processCreation.start();

        dispatcher = new Dispatcher();
        dispatcher.start();


        Process process = new Process(1, 3, Process.Type.fileHandling, new File("file.txt"));
//        addProcess(process);
//        processCreation.interrupt();
//        dispatcher.start();


        compileCodeFileProcess(process, processCreation, dispatcher);


        //run processes button in priority, fcfs

    }

    public static void compileCodeFileProcess(Process process, ProcessCreation processCreation, Dispatcher dispatcher){
        addProcess(process);
        scheduleSortProcesses(processCreation);

        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){
            System.out.println( e);
        }

        executeProcesses(dispatcher);

    }

    public static void runTerminalCommand(Process process, ProcessCreation processCreation, Dispatcher dispatcher){
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
        return  processes.poll();
    }

}
