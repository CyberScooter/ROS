package threads;


import threads.templates.Process;
import threads.templates.Output;

import java.io.BufferedReader;
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
    //stores process ID  and lines read for each process id, will be archived
    //<ProcessID, numberOfLinesRead>
    public static ConcurrentHashMap<Integer, Integer> codeLinesReadTracker;

    //stores number of ios that are still being handled for a specific process ID
    //<ProcessID, NumberOfIOsStillBeingHandled>


    private static ConcurrentHashMap<Integer, Integer> ioHandlerTracker;


    private static ConcurrentHashMap<Integer, CountDownLatch> ioHandlerTracker2;

    //this is only stores values once all the processes of a given
    //processid is fully completed and sorted in terms of line code
    public static ConcurrentHashMap<Integer, Vector<Output>> finalResultsForGivenCodeFileProcess;

    //this vectorlist stores all the process results for every processID
    //this vector list means that after data has been processed by CPU it stores into this array
    private static Vector<Output> cpuResults;

    private Semaphore semaphore = new Semaphore(1);
    //tracks io in progress for specific id
    private int ioInProgress;

    private Thread referenceCPUThread;

    public CPU(Process process) {
        this.process = process;
        if(cpuResults == null){
            cpuResults = new Vector<>();
        }
        if(codeLinesReadTracker == null && ioHandlerTracker == null && finalResultsForGivenCodeFileProcess == null){
            codeLinesReadTracker = new ConcurrentHashMap<>();
            ioHandlerTracker = new ConcurrentHashMap<>();
            finalResultsForGivenCodeFileProcess = new ConcurrentHashMap<>();
            ioHandlerTracker2 = new ConcurrentHashMap<>();
        }
        if(process.getType() == Process.Type.fileHandling ){
            try {
                semaphore.acquire();
                //checks if  process has just started
                if (codeLinesReadTracker.get(process.getId()) == null) {
                    codeLinesReadTracker.put(process.getId(), 0);
                } else {
                    codeLinesReadTracker.put(process.getId(), codeLinesReadTracker.get(process.getId()) + 1);

                }
            }catch (InterruptedException e){
                System.out.println(e);
            }finally {
                semaphore.release();
            }
        }
    }

    @Override
    public void run() {
        process.setState(Process.State.running);
        System.out.println("Thread ID=" + process.getId() + process.getState());

        if(process.getType() == Process.Type.fileHandling){

            if(process.getIOOutput() == null){
                try {
                    //if the current process belongs/came from io queue -> ready queue -> this cpu thread
                    //then dont run code below
                        semaphore.acquire();

                        int lineToReadForThisThread = codeLinesReadTracker.get(process.getId());

                        String line = Files.readAllLines(Paths.get(process.getFile())).get(lineToReadForThisThread);



                        //variables/output
                        final String INTEGER_VARIABLE_REGEX = "int[ ][a-z][ ]=[0-9]+;";
                        final String OUTPUT_REGEX = "print[ ][a-zA-Z0-9]+";

                        if (Pattern.matches(line, INTEGER_VARIABLE_REGEX)) {
                            String type = line.substring(0, 3);
                            int value = Integer.parseInt(line.substring((line.indexOf("=") + 1), line.length() - 1));
                            cpuResults.add(new Output(process.getId(), lineToReadForThisThread, type, value));

                        } else if (Pattern.matches(OUTPUT_REGEX, line)) {
                            Thread ioProcess = new IO(process.getId(), process, line, lineToReadForThisThread, process.getType());
                            ioProcess.start();

                            //keeps track of io handler in progress
                            if(ioHandlerTracker2.get(process.getId()) == null){
                                CountDownLatch latch = new CountDownLatch(1);
                                ioHandlerTracker2.put(process.getId(), latch);
                                System.out.println(ioHandlerTracker2.get(process.getId()).getCount());
                            }

                            if (ioHandlerTracker.get(process.getId()) == null) {
                                ioHandlerTracker.put(process.getId(), 1);
                            } else {
                                ioHandlerTracker.put(process.getId(), ioHandlerTracker.get(process.getId()) + 1);
                            }

                        }


                    //calculation
                    final String CALCULATION_OUTPUT = "";

                    //code to change syntax


                }catch (IOException | InterruptedException e){
                    System.out.println(e);
                } finally{
                    semaphore.release();
                }
            }else{
                //if process is from IOOUTPUT CLASS AND COMPLETE THEN DECREMENT COUNTER
                if(ioHandlerTracker2.get(process.getId()) != null){
//                    int numberOfIOProcessesForMatchingID = ioHandlerTracker.get(process.getId());
//                    ioHandlerTracker.put(process.getId(), numberOfIOProcessesForMatchingID - 1);
//                    if(ioHandlerTracker.get(process.getId()) == 0){
//                        process.getIOOutput().processHandling.notifyAll();
//                    }
                    cpuResults.add(new Output(process.getId(), process.getIOOutput().getLineNumber(), process.getIOOutput().getOutput()));
                    System.out.println("added output");
                    ioHandlerTracker2.get(process.getId()).countDown();


                }



            }
            //final line of code
            //if readline is equals to maxline so if io of the same processid creates a new cpu thread
            //then the lines its read will be greater than the maxlines so it will decrement the io resource
            //but never run this code below
            if(codeLinesReadTracker.get(process.getId()) == process.getMaxLines()){

                if(ioHandlerTracker.get(process.getId()) != null && ioHandlerTracker.get(process.getId()) > 0){
                    try {
                        //once all io has not been handled then wait until it has
                        System.out.println("Thread" + getId());
//                        do {
//                            wait();
//                        } while (ioHandlerTracker.get(process.getId()) != 0);
                        System.out.println("fe");

                        System.out.println(ioHandlerTracker2.get(process.getId()).getCount() + " c");
                        if(ioHandlerTracker2.get(process.getId()).getCount() > 0){
                            ioHandlerTracker2.get(process.getId()).await();
                        }

                        ioHandlerTracker2.get(process.getId()).await();
                        System.out.println("f");

                    }catch (InterruptedException e) {
                        System.out.println(e);
                    }

                    System.out.println("hey");

                }

                //if it has no io to be handled
                Collections.sort(cpuResults);

                Vector<Output> resultsForGivenProcessID = new Vector<>();

                for(Output Output : cpuResults){
                    if(Output.getProcessID() == process.getId()){
                        resultsForGivenProcessID.add(Output);
                    }
                }

                //add to a hashmap which will be used by OSKernel to run code
                finalResultsForGivenCodeFileProcess.put(process.getId(), resultsForGivenProcessID);

                //remove processID from ioHandler tracker and codeLinesReaderTracker
                ioHandlerTracker.remove(process.getId());
                codeLinesReadTracker.remove(process.getId());


            }

        }else if(process.getType() == Process.Type.commandLine){

        }



    }

    public void calculateMaxLines() throws IOException{
        int count = 0;
        BufferedReader bufferedReader = null;
        try{
            bufferedReader = new BufferedReader(new FileReader(process.getFile()));
            String line = null;
            while((line = bufferedReader.readLine()) != null){
                count ++;
            }

        }catch (IOException e){
            System.out.println(e);
        }finally {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            process.setMaxLines(count);
        }
    }

    public String readLine(int lineNumber) throws IOException{
        BufferedReader reader = null;
        String line = null;
        try {
            reader = new BufferedReader(new FileReader(process.getFile()));
            String lineToRead = null;
            int count = 0;
            while ((lineToRead = reader.readLine()) != null) {
                count++;
                if (count == lineNumber) {
                    line = reader.readLine();
                    break;
                }
            }
        }catch (IOException e){
            System.out.println(e);

        }finally {
            if(reader != null){
                reader.close();
            }
            return line;
        }
    }

    public void setReferenceCPUThread(Thread referenceCPUThread) {
        this.referenceCPUThread = referenceCPUThread;
    }
}
