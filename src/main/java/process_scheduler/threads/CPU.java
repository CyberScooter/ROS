package main.java.process_scheduler.threads;

import main.java.process_scheduler.threads.templates.*;
import main.java.process_scheduler.threads.templates.Process;
import main.java.views.Controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class CPU extends Thread {
    Process process;

    //stores number of ios that are still being handled for a specific process ID
    //<ProcessID, NumberOfIOsStillBeingHandled>
    private static ConcurrentHashMap<Integer, CountDownLatch> ioHandlerTracker;

    //this vectorlist stores all the process results for every processID
    //this vector list means that after data has been processed by CPU it stores into this array
    private static Vector<Output> cpuResults;

    public static LinkedHashMap<Integer, LinkedList<String> > cpuResultsCompiled;

    private Semaphore fileCompilingSemaphore = new Semaphore(1);
    private Semaphore terminalSemaphore = new Semaphore(1);
    private Semaphore fileReadingSemaphore = new Semaphore(1);
    //tracks io in progress for specific id

    private Vector<Thread> ioProcesses = new Vector<>();

    private static ConcurrentHashMap<String, String> textFileOutput;

    //used to store result of command from terminal to be tested in junit test
    public static String junitTestOutput;

    public CPU(Process process) {
        this.process = process;
        if(cpuResults == null && ioHandlerTracker == null && textFileOutput == null && cpuResultsCompiled == null){
            cpuResults = new Vector<>();
            textFileOutput = new ConcurrentHashMap<>();
            ioHandlerTracker = new ConcurrentHashMap<>();
            cpuResultsCompiled = new LinkedHashMap<>();
        }
    }

    @Override
    public void run() {
        process.setState(Process.State.running);

        if(process.getType() == Process.Type.fileCompiling){
            try {
                fileCompilingSemaphore.acquire();
                    //if the current process belongs/came from io queue -> ready queue -> this cpu thread
                    //then dont run code below
                    if(process.getIOOutput() == null){

                        String[] data = textFileOutput.get(process.getFile().getName()).split("\n");

                        for(int x = 0; x < data.length; x++){
                            if (Pattern.matches(RegexExpressions.INTEGER_VARIABLE_REGEX, data[x])) {
                                int indexAtEquals = data[x].indexOf("=");
                                String variableName = data[x].substring(0, indexAtEquals).trim();
                                int value = Integer.parseInt(data[x].substring(indexAtEquals + 1, data[x].length() - 1).trim());
                                cpuResults.add(new Output(process.getId(), x+1, variableName, value));

                            } else if (data[x].length() >= 5 && data[x].trim().substring(0,5).equals("print")) {
                                Thread ioProcess = new IO(process, data[x], x+1);
                                ioProcesses.add(ioProcess);

                            } else if(Pattern.matches(RegexExpressions.CALCULATION_REGEX1, data[x])) {
                                int index = data[x].indexOf("=");
                                String calculation = data[x].substring(index + 1, data[x].length()-1).trim();
                                if(CodeCompiler.checkCalculationSyntax(calculation)){
                                    cpuResults.add(new Output(process.getId(), x+1, data[x].substring(0, index).trim(), calculation, Output.Type.addition));
                                }
                            } else if(Pattern.matches(RegexExpressions.EXIT_REGEX, data[x])){
                                Output exit = new Output(process.getId(), true);
                                exit.setLine(x+1);
                                cpuResults.add(exit);
                            } else{
                                Output err = new Output(process.getId(), true, "Syntax error at line: " + x+1);
                                err.setLine(x+1);
                                cpuResults.add(err);
                                break;
                            }
                        }

                        if(ioProcesses.size() > 0){
                            //executes IOThreads which handles the IO and sends it back to readyqueue in the ProcessCreation thread
                            executeIOProcesses();
                        }

                        //executes once the io processes are done, as io processes they will use different threads
                        if(cpuResults.size() > 0){
                            CodeCompiler codeCompiler = new CodeCompiler();
                            Vector<Output> cpuResultsForGivenId = new Vector<>();
                            try{
                                Thread.sleep(100);
                            }catch (InterruptedException e){

                            }

                            for(Output output : cpuResults){
                                if(output.getProcessID() == process.getId()){
                                    cpuResultsForGivenId.add(output);
                                }
                            }

                            Collections.sort(cpuResultsForGivenId);

                            codeCompiler.compile(cpuResultsForGivenId, CodeCompiler.Type.arithmetic);

                            cpuResultsCompiled.put(process.getId(), codeCompiler.getCodeResults());
                            Controller.fileCompiling.countDown();

//                            for(String result : codeCompiler.getCodeResults()){
//                                System.out.println(result);
//                            }
                        }

                    }else{
                        addIOToResultsList(process, ioHandlerTracker);
                    }
            }catch (InterruptedException e){
                System.out.println(e);
            } finally{
                process.setState(Process.State.terminated);
                fileCompilingSemaphore.release();
            }

        }else if(process.getType() == Process.Type.commandLine){
            try {
                terminalSemaphore.acquire();
                if(process.getTerminalCode() != null) {
                    boolean cdProcess = process.getTerminalCode().checkIfCDCommand();

                    if (cdProcess) {
                        process.getTerminalCode().outputResult();
                        Terminal.terminalLatch.countDown();
                    } else {
                        Thread thread = new IO(process, process.getTerminalCode());
                        thread.start();
                        thread.join();
                    }

                }else if (process.isHandledByIO()) {
                    Terminal.commandResult = process.getIOOutput().getOutput();
                    //This is used to store output data for terminal code so that it can be
                    //tested in junit test class
                    junitTestOutput = process.getIOOutput().getOutput();
                    Terminal.terminalLatch.countDown();
                }
            }catch (InterruptedException e){
                System.out.println(e) ;
            }finally {
                process.setState(Process.State.terminated);
                terminalSemaphore.release();
            }

        }else if(process.getType() == Process.Type.fileReading){
            try {
                fileReadingSemaphore.acquire();
                if (process.getIOOutput() == null) {
                    try {
                        Thread IO = new IO(process);
                        IO.start();
                        IO.join();
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                } else {
                    textFileOutput.put(process.getIOOutput().getFilename(), process.getIOOutput().getOutput());
                    Controller.fileReading.countDown();
                }
            }catch (InterruptedException e){
                System.out.println(e);
            }finally {
                process.setState(Process.State.terminated);
                fileReadingSemaphore.release();

            }

        }else if(process.getType() == Process.Type.fileWriting){
            try {
                fileReadingSemaphore.acquire();
                if(process.getIOOutput() == null) {
                    try {
                        Thread IO = new IO(process, process.toWrite());
                        IO.start();
                        IO.join();
                        Controller.fileWriting.countDown();
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }else{
                    saveToTextFileOutput(process.getIOOutput().getFilename(), process.getIOOutput().getOutput());
                }

            }catch (InterruptedException e){
                System.out.println(e);
            }finally {
                process.setState(Process.State.terminated);
                fileReadingSemaphore.release();
            }
        }

    }

    private void executeIOProcesses() throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(ioProcesses.size());
        ioHandlerTracker.put(process.getId(), latch);
        for(Thread thread : ioProcesses){
            //synchnorise threads so they dont use same resource at the same time
            thread.start();
            thread.join();
        }
        ioHandlerTracker.get(process.getId()).await();
    }

    private void addIOToResultsList(Process process, ConcurrentHashMap<Integer, CountDownLatch> latch){
        Output output = new Output(process.getId(), process.getIOOutput());
        output.setLine(process.getLineNumber());
        cpuResults.add(output);

        latch.get(process.getId()).countDown();
    }

    public static ConcurrentHashMap<String, String> getTextFileOutput() {
        return textFileOutput;
    }

    public static void saveToTextFileOutput(String filename, String data) {
        textFileOutput.put(filename, data);
    }

}
