package main.java.process_scheduler.threads;

import main.java.Kernel;
import main.java.process_scheduler.threads.templates.CommandLine;
import main.java.process_scheduler.threads.templates.IOOutput;
import main.java.process_scheduler.threads.templates.PCB;
import main.java.process_scheduler.threads.templates.RegexExpressions;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class IO extends Thread {
    private Semaphore semaphore = new Semaphore(1);
    private static ConcurrentLinkedQueue<IOOutput> ioQueue;
    private String io;
    private int lineNumber;
    private PCB pcb;
    private CommandLine terminalCode;
    private String input;

    public IO(PCB PCB, String io, int lineNumber) {
        this.io = io;
        this.lineNumber= lineNumber;
        this.pcb = PCB;
        if(ioQueue == null){
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    public IO(PCB PCB, CommandLine terminalCode) {
        this.pcb = PCB;
        this.terminalCode = terminalCode;
        if(ioQueue == null){
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    //reading text file to display onto GUI
    public IO(PCB PCB){
        this.pcb = PCB;
        if(ioQueue == null){
            ioQueue = new ConcurrentLinkedQueue<>();
        }
    }

    //writing to textfile from gui
    public IO(PCB pcb, String input){
        this.pcb = pcb;
        this.input = input;
    }

    @Override
    public void run() {

        try {

            semaphore.acquire();

            if(pcb.getType() == PCB.Type.fileCompiling) {
                ioQueue.add(new IOOutput(pcb.getId(), io, lineNumber));
            }else if(pcb.getType() == PCB.Type.commandLine){
                ioQueue.add(new IOOutput(pcb.getId(), terminalCode));
            }else if(pcb.getType() == PCB.Type.fileReading){
                ioQueue.add(new IOOutput(pcb.getId(), pcb));
            }else if(pcb.getType() == PCB.Type.fileWriting){
                ioQueue.add(new IOOutput(input, pcb.getFile().getName()));
            }

            //FCFS
            while(!ioQueue.isEmpty()){
                IOOutput ioOutput = ioQueue.poll();
                if(ioOutput.getProcessType() == PCB.Type.fileCompiling){
                    handlePrintIOCodeFile(ioOutput);
                }else if(ioOutput.getProcessType() == PCB.Type.commandLine){
                    handleTerminalCode(ioOutput);
                }else if(ioOutput.getProcessType() == PCB.Type.fileReading){
                    handleFileReading(ioOutput);
                }else if(ioOutput.getProcessType() == PCB.Type.fileWriting){
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

            Kernel.addProcess(new PCB(output.getProcessID(), PCB.Type.fileReading, new IOOutput(stringBuffer.toString(), output.getFilename())));

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
            Kernel.addProcess(new PCB(output.getProcessHandling().getId(), PCB.Type.fileReading, new IOOutput(data.toString(), output.getProcessHandling().getFile().getName())));

        }catch (IOException e){

        }
    }

    private synchronized void handleTerminalCode(IOOutput output){
        String data = output.getTerminalCode().outputResult();
        IOOutput ioOutput = new IOOutput(data);

        PCB pcb = new PCB(output.getProcessID(), PCB.Type.commandLine, ioOutput);
        pcb.setHandledByIO(true);

        Kernel.addProcess(pcb);

    }

    //static makes it only run one at time
    private synchronized void handlePrintIOCodeFile(IOOutput result){

        int itemToOutputLength = result.getOutput().indexOf(" ");
        String output = result.getOutput().substring(itemToOutputLength, result.getOutput().length() - 1).trim();
        if(Pattern.matches(RegexExpressions.PRINT_STRING_REGEX, result.getOutput()) || Pattern.matches(RegexExpressions.PRINT_NUMBER_REGEX, result.getOutput())){
            //will get added to ready queue
            Kernel.addProcess(new PCB(result.getProcessID(), PCB.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, false, result.getLineNumber(), false)));
        }else if(Pattern.matches(RegexExpressions.PRINT_VARIABLE_REGEX, result.getOutput())){
            Kernel.addProcess(new PCB(result.getProcessID(), PCB.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), false)));
        }else {
            Kernel.addProcess(new PCB(result.getProcessID(), PCB.Type.fileCompiling, result.getLineNumber(), new IOOutput(output, true, result.getLineNumber(), true)));
        }

    }

}
