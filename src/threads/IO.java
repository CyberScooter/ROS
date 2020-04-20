package threads;

import threads.templates.*;
import threads.templates.Process;

import java.io.*;
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
    String input;

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

    //reading text file to display onto GUI
    public IO(int processID, Process process, Process.Type processType){
        this.processID = processID;
        this.process = process;
        this.processType = processType;
        if(ioQueue == null){
//            ioQueue = new Vector<>();
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    //writing to textfile from gui
    public IO(Process process, String input, Process.Type processType){
        this.process = process;
        this.input = input;
        this.processType = processType;
    }

    @Override
    public void run() {

        try {

            semaphore.acquire();


            if(processType == Process.Type.fileCompiling) {
                ioQueue.add(new IOOutput(processID, io, lineNumber));
            }else if(processType == Process.Type.commandLine){
                ioQueue.add(new IOOutput(processID, terminalCode));
            }else if(processType == Process.Type.fileReading){
                ioQueue.add(new IOOutput(processID, process));
            }else if(processType == Process.Type.fileWriting){
                ioQueue.add(new IOOutput(input, process.getFile().getName()));
            }

            //FCFS
            while(!ioQueue.isEmpty()){
                IOOutput ioOutput = ioQueue.poll();
                if(ioOutput.getProcessType() == Process.Type.fileCompiling){
                    handlePrintIOCodeFile(ioOutput);
                }else if(ioOutput.getProcessType() == Process.Type.commandLine){
                    handleTerminalCode(ioOutput);
                }else if(ioOutput.getProcessType() == Process.Type.fileReading){
                    handleFileReading(ioOutput);
                }else if(ioOutput.getProcessType() == Process.Type.fileWriting){
                    handleFileWriting(ioOutput);
                }

            }

        }catch (InterruptedException e){
            System.out.println(e);
        } finally{
            semaphore.release();
        }

    }

    private synchronized void handleFileWriting(IOOutput output) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("resources/" + output.getFilename())));
            bufferedWriter.write(output.getOutput());

            bufferedWriter.close();

        }catch (IOException e){

        }
    }

    private synchronized void handleFileReading(IOOutput output){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(new File("resources/" + output.getProcessHandling().getFile())));
            String line = null;
            StringBuffer data = new StringBuffer();
            while((line = reader.readLine()) != null){
                data.append(line).append("\n");
            }

            reader.close();
            Kernel.addProcess(new Process(output.getProcessHandling().getId(), Process.Type.fileReading, 0, new IOOutput(data.toString(), output.getProcessHandling().getFile().getName())));
            Kernel.processCreation.interrupt();

        }catch (IOException e){

        }
    }

    private synchronized void handleTerminalCode(IOOutput output){
        String data = output.getTerminalCode().outputResult();
        IOOutput ioOutput = new IOOutput(data);

        Process process = new Process(output.getProcessID(), Process.Type.commandLine, ioOutput);
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
            Kernel.addProcess(new Process(process.getId(), Process.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, false, result.getLineNumber(), false, process)));
        }else if(Pattern.matches(RegexExpressions.PRINT_VARIABLE_REGEX, result.getOutput())){
            Kernel.addProcess(new Process(process.getId(), Process.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), false, process)));
        }else if(itemToOutputLength == 0){
            Kernel.addProcess(new Process(process.getId(), Process.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), true, process)));
        }
        Kernel.processCreation.interrupt();

    }


    public String getResult() {
        return result;
    }
}
