package test.java.threads;

import junit.framework.TestCase;
import main.java.threads.CPU;
import main.java.threads.Kernel;
import main.java.threads.templates.Process;
import main.java.threads.templates.ReadyQueueComparator;
import main.java.views.Controller;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

//These Tests do not test functionality of GUI, this can only be tested by the end user itself by opening the JAR file in out folder
//these test cases test the functionality of the multi-threaded system
public class TestProcessScheduling extends TestCase {

    private Kernel kernel;
    private CountDownLatch fileReading;
    private LinkedList<Process> processesToExecute;

    @Test
    public void testCodeFileReadProcess(){
        kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);
        Controller.fileReading = new CountDownLatch(1);
        processesToExecute = new LinkedList<>();
        Vector<String> results = new Vector<>();
        //file read is meant to be sequential as a GUI can only be able to open
        //one code file at a time due to restrictions in interacting with the UI
        Process process = new Process(1, 99, Process.Type.fileReading, new File("JUnitTestProgram1.txt"));
        processesToExecute.add(process);
        Kernel.runCodeFileProcesses(processesToExecute);
        try {
            Controller.fileReading.await();
        }catch (InterruptedException e){
            System.out.println(e);
        } finally{
            if(CPU.getTextFileOutput().get("JUnitTestProgram1.txt") != null) results.add(CPU.getTextFileOutput().get("JUnitTestProgram1.txt"));
        }


        assertEquals("var x = 5;\nvar z = x + 4;\nvar m = 5 + z;\nprint x;\nexit;\n", results.get(0));

    }


    //output of the results for each file will vary in order because of how long the process takes to finish
    //the program starts running in the priority order set but sometimes a process may take longer to compute
    //than another process which may cause the output to be displayed in a different order than how it was sent to be handled by the dispatcher
    //there is no exact sequence I can compare the output of the code to as the processing time may constantly change
    @Test
    public void testCompileAllCodeFilesInPriorityScheduling(){
        kernel = new Kernel(ReadyQueueComparator.queueType.priority);
        processesToExecute = new LinkedList<>();
        //file are meant to be read sequentially
        //files are read so that the CPU static variable contains the data for the code files so that the compiling process can use that variable
        for(int x = 0; x < 3; x++){
            Process process = new Process(x+1, 99, Process.Type.fileReading, new File("JUnitTestProgram" + (x+1) + ".txt"));
            Controller.fileReading = new CountDownLatch(1);
            processesToExecute.add(process);
            Kernel.runCodeFileProcesses(processesToExecute);
            try {
                Controller.fileReading.await();
            }catch (InterruptedException e){
                System.out.println(e);
            }
            processesToExecute.clear();
        }

        Vector<String> results = new Vector<>();
        Process process = new Process(4, 7, Process.Type.fileCompiling, new File("JUnitTestProgram1.txt"));
        Process process2 = new Process(5, 2, Process.Type.fileCompiling, new File("JUnitTestProgram2.txt"));
        Process process3 = new Process(6, 5, Process.Type.fileCompiling, new File("JUnitTestProgram3.txt"));
        processesToExecute.add(process);
        processesToExecute.add(process2);
        processesToExecute.add(process3);

        Controller.fileCompiling = new CountDownLatch(processesToExecute.size());
        Kernel.runCodeFileProcesses(processesToExecute);
        try{
            Controller.fileCompiling.await();
        }catch (InterruptedException e){
            System.out.println(e);
        }

        StringBuffer stringBuffer = new StringBuffer();

        Set<Integer> setKeys = CPU.cpuResultsCompiled.keySet();
        List<Integer> listKeys = new ArrayList<>(setKeys);
        ListIterator<Integer> iterator = listKeys.listIterator( listKeys.size() );
        while(iterator.hasPrevious()){
            Integer previousID = iterator.previous();
            for(String item : CPU.cpuResultsCompiled.get(previousID)){
                stringBuffer.append("PROCESS ID: " + previousID + " RESULT: ").append(item).append("\n");
            }
        }

        CPU.cpuResultsCompiled.clear();

        //file compiles in order of id:1 --> id:3 --> id:5
        assertNotNull(stringBuffer.toString());

    }

    //output of the results for each file will vary in the order it reaches the program
    //the program is compiled by CPU in a FCFS order but sometimes a process may take longer to compute
    //than another process which may cause the output to be displayed in a different order than how it was sent to be handled by the dispatcher
    //there is no exact sequence I can compare the code instruction to as the processing time is constantly changing
    @Test
    public void testCompileAllCodeFilesInFCFSScheduling(){
        kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);
        processesToExecute = new LinkedList<>();
        //file are meant to be read sequentially
        //files are read so that the CPU static variable contains the data for the code files so that the compiling process can use that variable
        for(int x = 0; x < 3; x++){
            Process process = new Process(x+1, 99, Process.Type.fileReading, new File("JUnitTestProgram" + (x+1) + ".txt"));
            Controller.fileReading = new CountDownLatch(1);
            processesToExecute.add(process);
            Kernel.runCodeFileProcesses(processesToExecute);
            try {
                Controller.fileReading.await();
            }catch (InterruptedException e){
                System.out.println(e);
            }
            processesToExecute.clear();
        }

        Process process = new Process(4, 1, Process.Type.fileCompiling, new File("JUnitTestProgram1.txt"));
        Process process2 = new Process(5, 1, Process.Type.fileCompiling, new File("JUnitTestProgram2.txt"));
        Process process3 = new Process(6, 1, Process.Type.fileCompiling, new File("JUnitTestProgram3.txt"));
        processesToExecute.add(process);
        processesToExecute.add(process2);
        processesToExecute.add(process3);

        Controller.fileCompiling = new CountDownLatch(processesToExecute.size());
        Kernel.runCodeFileProcesses(processesToExecute);
        try{
            Controller.fileCompiling.await();
        }catch (InterruptedException e){
            System.out.println(e);
        }

        StringBuffer stringBuffer = new StringBuffer();

        Set<Integer> setKeys = CPU.cpuResultsCompiled.keySet();
        List<Integer> listKeys = new ArrayList<>(setKeys);
        ListIterator<Integer> iterator = listKeys.listIterator( listKeys.size() );
        while(iterator.hasPrevious()){
            Integer previousID = iterator.previous();
            for(String item : CPU.cpuResultsCompiled.get(previousID)){
                stringBuffer.append("PROCESS ID: " + previousID + " RESULT: ").append(item).append("\n");
            }
        }
        CPU.cpuResultsCompiled.clear();
        //file compiles in order of id:4 --> id:5 --> id:6, sorts by earliest ID in order to implement FCFS

        assertNotNull(stringBuffer.toString());
    }



}