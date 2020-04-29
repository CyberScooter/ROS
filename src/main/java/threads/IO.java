package main.java.threads;

import main.java.threads.templates.CommandLine;
import main.java.threads.templates.IOOutput;
import main.java.threads.templates.Process;
import main.java.threads.templates.RegexExpressions;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class IO extends Thread {
    private Semaphore semaphore = new Semaphore(1);
    private static ConcurrentLinkedQueue<IOOutput> ioQueue;
    private String io;
    private int lineNumber;
    private Process process;
    private CommandLine terminalCode;
    private String input;

    public IO(Process process, String io, int lineNumber) {
        this.io = io;
        this.lineNumber= lineNumber;
        this.process = process;
        if(ioQueue == null){
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    public IO(Process process, CommandLine terminalCode) {
        this.process = process;
        this.terminalCode = terminalCode;
        if(ioQueue == null){
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    //reading text file to display onto GUI
    public IO(Process process){
        this.process = process;
        if(ioQueue == null){
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    //writing to textfile from gui
    public IO(Process process, String input){
        this.process = process;
        this.input = input;
    }

    @Override
    public void run() {

        try {

            semaphore.acquire();

            if(process.getType() == Process.Type.fileCompiling) {
                ioQueue.add(new IOOutput(process.getId(), io, lineNumber));
            }else if(process.getType() == Process.Type.commandLine){
                ioQueue.add(new IOOutput(process.getId(), terminalCode));
            }else if(process.getType() == Process.Type.fileReading){
                ioQueue.add(new IOOutput(process.getId(), process));
            }else if(process.getType() == Process.Type.fileWriting){
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
            //updates file
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("resources/" + output.getFilename())));
            bufferedWriter.write(output.getOutput());

            bufferedWriter.close();

            //reads updated file to update textFileOutput attribute in CPU so that updated code file can be retrieved by CPU
            StringBuffer stringBuffer = new StringBuffer();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("resources/" + output.getFilename())));
            String line = null;
            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line).append("\n");
            }
            bufferedReader.close();

            Kernel.addProcess(new Process(output.getProcessID(), Process.Type.fileReading, new IOOutput(stringBuffer.toString(), output.getFilename())));
            Kernel.processCreation.interrupt();


        }catch (IOException e){
            System.out.println(e);
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
            Kernel.addProcess(new Process(output.getProcessHandling().getId(), Process.Type.fileReading, new IOOutput(data.toString(), output.getProcessHandling().getFile().getName())));
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
    private synchronized void handlePrintIOCodeFile(IOOutput result){

        int itemToOutputLength = result.getOutput().indexOf(" ");
        String output = result.getOutput().substring(itemToOutputLength, result.getOutput().length() - 1).trim();
        if(Pattern.matches(RegexExpressions.PRINT_STRING_REGEX, result.getOutput()) || Pattern.matches(RegexExpressions.PRINT_NUMBER_REGEX, result.getOutput())){
            //will get added to ready queue
            Kernel.addProcess(new Process(result.getProcessID(), Process.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, false, result.getLineNumber(), false)));
        }else if(Pattern.matches(RegexExpressions.PRINT_VARIABLE_REGEX, result.getOutput())){
            Kernel.addProcess(new Process(result.getProcessID(), Process.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), false)));
        }else if(itemToOutputLength == 0){
            Kernel.addProcess(new Process(result.getProcessID(), Process.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), true)));
        }
        Kernel.processCreation.interrupt();

    }

}
