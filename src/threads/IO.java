package threads;

import threads.templates.IOOutput;
import threads.templates.Process;
import threads.templates.RegexExpressions;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class IO extends Thread {
    Semaphore semaphore = new Semaphore(1);
    static ConcurrentLinkedQueue<IOOutput> ioQueue;
//    static Vector<Result> ioQueue;
    String result;
    String io;
    int processID;
    int lineNumber;
    Process process;
    Process.Type processType;

    public IO(int processID, Process process, String io, int lineNumber, Process.Type processType) {
        this.io = io;
        this.processID = processID;
        this.lineNumber= lineNumber;
        this.processType = processType;
        this.process = process;
        if(ioQueue == null){
//            ioQueue = new Vector<>();
            ioQueue = new ConcurrentLinkedQueue<IOOutput>();
        }
    }

    @Override
    public void run() {

        try {

            semaphore.acquire();

            System.out.println("IM IN LMAO");

            ioQueue.add(new IOOutput(processID, io, lineNumber));


            //FCFS - Scheduling algorithm
            for(IOOutput result : ioQueue){
                if(result.getProcessID() == this.processID && result.getLineNumber() == this.lineNumber && result.getOutput().equals(this.io)){
                    //for print variables/strings in code file
                    if(Pattern.matches(RegexExpressions.PRINT_REGEX, result.getOutput()) && processType == Process.Type.fileHandling){
                        System.out.println("regex matches");
                        handlePrintIOCodeFile(result);
                    }
                    //for print data in command line
                    if(processType == Process.Type.commandLine){

                    }

                }
            }

//            while(!ioQueue.isEmpty()){
//
//                if(ioQu)
//
//
//                E element = ioQueue.firstElement();
//
//                //process element
//
//                //return String result to CPU thread
//            }




        }catch (InterruptedException e){
            System.out.println(e);
        } finally{
            semaphore.release();
        }

    }

    public void handlePrintIOCodeFile(IOOutput result){
        int itemToOutputLength = result.getOutput().substring(6).trim().length();
        if(itemToOutputLength > 1){
            //will get added to ready queue
            System.out.println("hey");
            OSKernel.addProcess(new Process(processID, Process.Type.fileHandling, new IOOutput(result.getOutput(), false, result.getLineNumber(), false, process)));
        }else if(itemToOutputLength == 1){
            OSKernel.addProcess(new Process(processID, Process.Type.fileHandling, new IOOutput(result.getOutput(), true, result.getLineNumber(), false, process)));
        }else if(itemToOutputLength == 0){
            OSKernel.addProcess(new Process(processID, Process.Type.fileHandling, new IOOutput(result.getOutput(), true, result.getLineNumber(), true, process)));
        }
        OSKernel.processCreation.interrupt();
    }


    public String getResult() {
        return result;
    }
}
