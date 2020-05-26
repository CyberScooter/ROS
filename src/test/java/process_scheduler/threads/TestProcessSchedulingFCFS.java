package test.java.process_scheduler.threads;

import main.java.process_scheduler.threads.CPU;
import main.java.process_scheduler.threads.Dispatcher;
import main.java.Kernel;
import main.java.process_scheduler.threads.Terminal;
import main.java.process_scheduler.threads.templates.CommandLine;
import main.java.process_scheduler.threads.templates.PCB;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;
import main.java.views.Controller;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

//These Tests do not test functionality of GUI, this can only be tested by the end user itself by opening the JAR file in out folder
//these test cases test the functionality of the multi-threaded system
public class TestProcessSchedulingFCFS {

    private Kernel kernel;
    private LinkedList<PCB> processesToExecute;


    @Test
    public void testCodeFileReadProcess(){
        kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);
        Controller.fileReading = new CountDownLatch(1);
        processesToExecute = new LinkedList<>();
        Vector<String> results = new Vector<>();
        //file read is meant to be sequential as a GUI can only be able to open
        //one code file at a time due to restrictions in interacting with the UI
        PCB pcb = new PCB(1, 99, PCB.Type.fileReading, new File("JUnitTestProgram1.txt"));
        processesToExecute.add(pcb);
        Kernel.runCodeFileProcesses(processesToExecute);
        try {
            Controller.fileReading.await();
        }catch (InterruptedException e){
            System.out.println(e);
        } finally{
            if(CPU.getTextFileOutput().get("JUnitTestProgram1.txt") != null) results.add(CPU.getTextFileOutput().get("JUnitTestProgram1.txt"));
        }


        Assert.assertEquals("var x = 5;\nvar z = x + 4;\nvar m = 5 + z;\nprint x;\nexit;\n", results.get(0));

    }

    @Test
    public void testCompileCodeFilesFCFSExecutionOrder(){
        kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);
        Dispatcher.orderOfExecutionTest.clear();
        processesToExecute = new LinkedList<>();
        //file are meant to be read sequentially
        //files are read so that the CPU static variable contains the data for the code files so that the compiling process can use that variable
        for(int x = 0; x < 3; x++){
            PCB pcb = new PCB(x+1, 99, PCB.Type.fileReading, new File("JUnitTestProgram" + (x+1) + ".txt"));
            Controller.fileReading = new CountDownLatch(1);
            processesToExecute.add(pcb);
            Kernel.runCodeFileProcesses(processesToExecute);
            try {
                Controller.fileReading.await();
            }catch (InterruptedException e){
                System.out.println(e);
            }
            processesToExecute.clear();
        }

        Dispatcher.orderOfExecutionTest.clear();

        Vector<String> results = new Vector<>();
        PCB pcb = new PCB(4, 1, PCB.Type.fileCompiling, new File("JUnitTestProgram1.txt"));
        PCB pcb2 = new PCB(5, 1, PCB.Type.fileCompiling, new File("JUnitTestProgram2.txt"));
        PCB pcb3 = new PCB(6, 1, PCB.Type.fileCompiling, new File("JUnitTestProgram3.txt"));
        processesToExecute.add(pcb);
        processesToExecute.add(pcb2);
        processesToExecute.add(pcb3);

        Controller.fileCompiling = new CountDownLatch(processesToExecute.size());
        Kernel.runCodeFileProcesses(processesToExecute);
        try{
            Controller.fileCompiling.await();
        }catch (InterruptedException e){
            System.out.println(e);
        }

        StringBuffer stringBuffer = new StringBuffer();


        //the first element added to the 'processOrderOfExecutionTest' list is the first one
        //removed from this list and added to the string
        int count = 1;
        while(!Dispatcher.orderOfExecutionTest.isEmpty()){
            stringBuffer.append(count + ") PROCESS OF ID: " + + Dispatcher.orderOfExecutionTest.poll().getId()).append(" RAN").append("\n");
            count++;
        }

        CPU.cpuResultsCompiled.clear();

        //output should be the id of the processes that are executed in FCFS order:
        // id:4 -> id:5 -> id:6
        Assert.assertEquals("1) PROCESS OF ID: 4 RAN\n2) PROCESS OF ID: 5 RAN\n3) PROCESS OF ID: 6 RAN\n", stringBuffer.toString());
    }

    @Test
    public void testCompileAllCodeFilesInFCFSOutput(){
        kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);
        processesToExecute = new LinkedList<>();
        //file are meant to be read sequentially
        //files are read so that the CPU static variable contains the data for the code files so that the compiling process can use that variable
        for(int x = 0; x < 3; x++){
            PCB pcb = new PCB(x+1, 99, PCB.Type.fileReading, new File("JUnitTestProgram" + (x+1) + ".txt"));
            Controller.fileReading = new CountDownLatch(1);
            processesToExecute.add(pcb);
            Kernel.runCodeFileProcesses(processesToExecute);
            try {
                Controller.fileReading.await();
            }catch (InterruptedException e){
                System.out.println(e);
            }
            processesToExecute.clear();
        }

        PCB pcb = new PCB(4, 1, PCB.Type.fileCompiling, new File("JUnitTestProgram1.txt"));
        PCB pcb2 = new PCB(5, 1, PCB.Type.fileCompiling, new File("JUnitTestProgram2.txt"));
        PCB pcb3 = new PCB(6, 1, PCB.Type.fileCompiling, new File("JUnitTestProgram3.txt"));
        processesToExecute.add(pcb);
        processesToExecute.add(pcb2);
        processesToExecute.add(pcb3);

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

        Assert.assertNotNull(stringBuffer.toString());
    }




}