package threads;

import threads.templates.Output;
import threads.templates.Process;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Kernel {
    static ConcurrentLinkedQueue<Process> processes = new ConcurrentLinkedQueue<>();
    static ProcessCreation processCreation;
    static Dispatcher dispatcher;

    public static void main(String[] args) {

        processCreation = new ProcessCreation();
        dispatcher = new Dispatcher();


        processCreation.start();

        Process process = new Process(1, Process.Type.fileHandling, "file.txt");
        addProcess(process);
        processCreation.interrupt();
        dispatcher.start();

        try {
            Thread.sleep(2000);
        }catch (InterruptedException e){
            System.out.println( e);
        }

        dispatcher.interrupt();

        try {
            Thread.sleep(2000);
        }catch (InterruptedException e){
            System.out.println( e);
        }




    }

    public static void addProcess(Process process){
        processes.add(process);

    }

    //removes process and returns it
    public static Process getProcess(){
        return  processes.poll();
    }

}
