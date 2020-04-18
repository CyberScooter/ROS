package threads;

import threads.templates.*;
import threads.templates.Process;

import java.util.PriorityQueue;
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
    boolean available = true;
    CommandLine terminalCode;

    public IO(int processID, Process process, String io, int lineNumber, Process.Type processType) {
        this.io = io;
        this.processID = processID;
        this.lineNumber= lineNumber;
        this.processType = processType;
        this.process = process;
        if(ioQueue == null){
//            ioQueue = new Vector<>();
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    public IO(int processID, Process process, CommandLine terminalCode, Process.Type processType) {
        this.processID = processID;
        this.processType = processType;
        this.process = process;
        this.terminalCode = terminalCode;
        if(ioQueue == null){
//            ioQueue = new Vector<>();
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public void run() {

        try {

            semaphore.acquire();

            if(processType == Process.Type.fileHandling) {
                ioQueue.add(new IOOutput(processID, io, lineNumber));
            }else if(processType == Process.Type.commandLine){
                ioQueue.add(new IOOutput(processID, terminalCode));
            }



            //FCFS - Scheduling algorithm
//            for(IOOutput result : ioQueue){
//                if(result.getProcessID() == this.processID && result.getLineNumber() == this.lineNumber && result.getOutput().equals(this.io)){
//                    //for print variables/strings in code file
//                    handlePrintIOCodeFile(result);
//                    //for print data in command line
//                    if(processType == Process.Type.commandLine){
//
//                    }
//
//                }
//            }

            //FCFS
            while(!ioQueue.isEmpty()){
                IOOutput ioOutput = ioQueue.poll();
                if(ioOutput.getOutput() != null){
                    handlePrintIOCodeFile(ioOutput);
                }else if(ioOutput.getTerminalCode() != null){
                    handleTerminalCode(ioOutput);
                }

            }

        }catch (InterruptedException e){
            System.out.println(e);
        } finally{
            semaphore.release();
        }

    }

    private synchronized void handleTerminalCode(IOOutput output){
        String data = output.getTerminalCode().outputResult();
        IOOutput ioOutput = new IOOutput(data);

        Process process = new Process(processID, Process.Type.commandLine, ioOutput);
        process.setHandledByIO(true);

        Kernel.addProcess(process);
        Kernel.processCreation.interrupt();

    }

    //static makes it only run one at time
    public synchronized void handlePrintIOCodeFile(IOOutput result){

        int itemToOutputLength = result.getOutput().indexOf(" ");
        String output = result.getOutput().substring(itemToOutputLength, result.getOutput().length() - 1).trim();
        if(Pattern.matches(RegexExpressions.PRINT_STRING_REGEX, result.getOutput())){
            //will get added to ready queue
            Kernel.addProcess(new Process(process.getId(), Process.Type.fileHandling, result.getLineNumber(), new IOOutput(output, false, result.getLineNumber(), false, process)));
        }else if(Pattern.matches(RegexExpressions.PRINT_VARIABLE_REGEX, result.getOutput())){
            Kernel.addProcess(new Process(process.getId(), Process.Type.fileHandling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), false, process)));
        }else if(itemToOutputLength == 0){
            Kernel.addProcess(new Process(process.getId(), Process.Type.fileHandling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), true, process)));
        }
        Kernel.processCreation.interrupt();

    }


    public String getResult() {
        return result;
    }
}
