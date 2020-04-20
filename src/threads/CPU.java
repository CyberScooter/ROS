package threads;


import javafx.scene.control.Control;
import threads.templates.*;
import threads.templates.Process;
import views.Controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
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

    public static ConcurrentHashMap<Integer, LinkedList<String>> cpuResultsCompiled;

    private Semaphore fileCompilingSemaphore = new Semaphore(1);
    private Semaphore terminalSemaphore = new Semaphore(1);
    private Semaphore fileReadingSemaphore = new Semaphore(1);
    //tracks io in progress for specific id

    private Vector<Thread> ioProcesses = new Vector<>();

    private static ConcurrentHashMap<String, String> textFileOutput;

    public CPU(Process process) {
        this.process = process;
        if(cpuResults == null && ioHandlerTracker == null && textFileOutput == null && cpuResultsCompiled == null){
            cpuResults = new Vector<>();
            textFileOutput = new ConcurrentHashMap<>();
            ioHandlerTracker = new ConcurrentHashMap<>();
            cpuResultsCompiled = new ConcurrentHashMap<>();
        }
    }

    @Override
    public void run() {
        process.setState(Process.State.running);

        if(process.getType() == Process.Type.fileCompiling){
            try {
                fileCompilingSemaphore.acquire();
            if(process.getIOOutput() == null){

                        //if the current process belongs/came from io queue -> ready queue -> this cpu thread
                        //then dont run code below


                        BufferedReader bufferedReader = new BufferedReader(new FileReader("resources/" + process.getFile()));
                        String line = null;
                        int count = 0;

                        while((line = bufferedReader.readLine()) != null) {
                            count++;

                            if (Pattern.matches(RegexExpressions.INTEGER_VARIABLE_REGEX, line)) {
                                int indexAtEquals = line.indexOf("=");
                                String variableName = line.substring(0, indexAtEquals).trim();
                                int value = Integer.parseInt(line.substring(indexAtEquals + 1, line.length() - 1).trim());
                                cpuResults.add(new Output(process.getId(), count, variableName, value));

                            } else if (line.length() >= 5 && line.trim().substring(0,5).equals("print")) {
                                Thread ioProcess = new IO(process.getId(), process, line, count, process.getType());
                                ioProcesses.add(ioProcess);

                            } else if(Pattern.matches(RegexExpressions.CALCULATION_REGEX1, line)) {
                                int index = line.indexOf("=");
                                String calculation = line.substring(index + 1, line.length()-1).trim();
                                if(CodeCompiler.checkCalculationSyntax(calculation)){
                                    cpuResults.add(new Output(process.getId(), count, line.substring(0, index).trim(), calculation, Output.Type.addition));
                                }
                            } else if(Pattern.matches(RegexExpressions.EXIT_REGEX, line)){
                                Output exit = new Output(process.getId(), true);
                                exit.setLine(count);
                                cpuResults.add(exit);
                            } else{
                                Output err = new Output(process.getId(), true, "Syntax error at line: " + count);
                                err.setLine(count);
                                cpuResults.add(err);
                                break;
                            }

                    }

                        bufferedReader.close();

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

//                        for(String result : codeCompiler.getCodeResults()){
//                            System.out.println(result);
//                        }
                    }


                }else{

                    addIOToResultsList(process, ioHandlerTracker);

                }
            }catch (IOException | InterruptedException e){
                System.out.println(e);
            } finally{
                fileCompilingSemaphore.release();
            }

        }else if(process.getType() == Process.Type.commandLine){
            try {
                terminalSemaphore.acquire();
                if(process.getTerminalCode() != null) {
                    boolean cdProcess = process.getTerminalCode().checkIfCDCommand();

                    if (cdProcess) {
                        process.getTerminalCode().outputResult();
                    } else {
                        Thread thread = new IO(process.getId(), process, process.getTerminalCode(), Process.Type.commandLine);
                        thread.start();
                        thread.join();
                    }

                }else if (process.isHandledByIO()) {
                    System.out.println(process.getIOOutput().getOutput());
                }
            }catch (InterruptedException e){
                System.out.println(e);
            }finally {
                terminalSemaphore.release();
            }
        }else if(process.getType() == Process.Type.fileReading){
            try {
                fileReadingSemaphore.acquire();
                if (process.getIOOutput() == null) {
                    try {
                        Thread IO = new IO(process.getId(), process, process.getType());
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
                fileReadingSemaphore.release();
            }
        }else if(process.getType() == Process.Type.fileWriting){
            try {
                fileReadingSemaphore.acquire();
                try {
                    Thread IO = new IO(process, process.toWrite(), process.getType());
                    IO.start();
                    IO.join();
                    Controller.fileWriting.countDown();
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

            }catch (InterruptedException e){
                System.out.println(e);
            }finally {
                fileReadingSemaphore.release();
            }
        }

    }

    private void executeTerminalIOProcess(IO ioThread) throws InterruptedException{

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

}
