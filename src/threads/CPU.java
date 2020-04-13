package threads;


import threads.templates.*;
import threads.templates.Process;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
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

    private Semaphore semaphore = new Semaphore(1);
    //tracks io in progress for specific id

    private Vector<Thread> ioProcesses = new Vector<>();

    public CPU(Process process) {
        this.process = process;
        if(cpuResults == null && ioHandlerTracker == null){
            cpuResults = new Vector<>();
            ioHandlerTracker = new ConcurrentHashMap<>();
        }
    }

    @Override
    public synchronized void run() {
        process.setState(Process.State.running);

        if(process.getType() == Process.Type.fileHandling){

            if(process.getIOOutput() == null){
                try {
                        //if the current process belongs/came from io queue -> ready queue -> this cpu thread
                        //then dont run code below
                        semaphore.acquire();

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(process.getFile()));
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
                                cpuResults.add(new Output(process.getId(), true));
                            } else{
                                cpuResults.add(new Output(process.getId(), true, "Syntax error at line: " + count));
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
                        for(Output output : cpuResults){
                            if(output.getProcessID() == process.getId()){
                                cpuResultsForGivenId.add(output);
                            }
                        }

                        codeCompiler.compile(cpuResultsForGivenId, CodeCompiler.Type.arithmetic);
                    }

                }catch (IOException | InterruptedException e){
                    System.out.println(e);
                } finally{
                    semaphore.release();
                }
            }else{
                addIOResult(process, ioHandlerTracker);

            }

        }else if(process.getType() == Process.Type.commandLine){
            //terminal



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

    private void addIOResult(Process process, ConcurrentHashMap<Integer, CountDownLatch> latch){
        cpuResults.add(new Output(process.getId(), process.getIOOutput()));

        latch.get(process.getId()).countDown();


    }


}
