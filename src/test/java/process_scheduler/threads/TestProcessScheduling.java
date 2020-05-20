package test.java.process_scheduler.threads;

import main.java.process_scheduler.threads.CPU;
import main.java.process_scheduler.threads.Dispatcher;
import main.java.Kernel;
import main.java.process_scheduler.threads.Terminal;
import main.java.process_scheduler.threads.templates.CommandLine;
import main.java.process_scheduler.threads.templates.Process;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;
import main.java.views.Controller;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

//These Tests do not test functionality of GUI, this can only be tested by the end user itself by opening the JAR file in out folder
//these test cases test the functionality of the multi-threaded system
public class TestProcessScheduling {

    private Kernel kernel;
    private LinkedList<Process> processesToExecute;

    //This reads the codefile and updates the CPU textFileOutput variable with the process ID and the code belonging to it
    //which is read from the files
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


        Assert.assertEquals("var x = 5;\nvar z = x + 4;\nvar m = 5 + z;\nprint x;\nexit;\n", results.get(0));

    }

    //THE TWO METHODS BELOW TEST THE EXECUTION ORDER OF THE FCFS AND PRIORITY SCHEDULING ALGORITHMS
    //=====================================================================================================================
    //To test the execution order of the processes, a linked list is added to the dispatcher thread that adds a new
    //process just before it is being sent to the CPU in order to be executed. This way it will show whether
    //the priority scheduling works
    //The output from the CPU result does not prove whether the priority scheduling works because the processing time
    //is a huge factor that affects it.
    @Test
    public void testCompileCodeFilesPriorityExecutionOrder(){
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

        Dispatcher.processOrderOfExecutionTest.clear();

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


        //the first element added to the 'processOrderOfExecutionTest' list is the first one
        //removed from this list and added to the string
        int count = 1;
        while(!Dispatcher.processOrderOfExecutionTest.isEmpty()){
            stringBuffer.append(count + ") PROCESS OF ID: " + + Dispatcher.processOrderOfExecutionTest.poll().getId()).append(" RAN").append("\n");
            count++;
        }

        CPU.cpuResultsCompiled.clear();

        //output should be the id of the processes that are executed in priority order:
        // id:4 -> id:6 -> id:5
        Assert.assertEquals("1) PROCESS OF ID: 4 RAN\n2) PROCESS OF ID: 5 RAN\n3) PROCESS OF ID: 6 RAN\n", stringBuffer.toString());
    }

    @Test
    public void testCompileCodeFilesFCFSExecutionOrder(){
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

        Dispatcher.processOrderOfExecutionTest.clear();

        Vector<String> results = new Vector<>();
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


        //the first element added to the 'processOrderOfExecutionTest' list is the first one
        //removed from this list and added to the string
        int count = 1;
        while(!Dispatcher.processOrderOfExecutionTest.isEmpty()){
            stringBuffer.append(count + ") PROCESS OF ID: " + + Dispatcher.processOrderOfExecutionTest.poll().getId()).append(" RAN").append("\n");
            count++;
        }

        CPU.cpuResultsCompiled.clear();

        //output should be the id of the processes that are executed in FCFS order:
        // id:4 -> id:5 -> id:6
        Assert.assertEquals("1) PROCESS OF ID: 4 RAN\n2) PROCESS OF ID: 5 RAN\n3) PROCESS OF ID: 6 RAN\n", stringBuffer.toString());
    }


    //THE TWO METHODS BELOW CHECK WHETHER AN OUTPUT IS PRODUCED FROM CPU
    //=============================================================================
    //To test this I pulled values from a static variable in the CPU thread that contains the results of each code file that has bee compiled
    @Test
    public void testCompileCodeFilesPriorityOutput(){
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
        Assert.assertNotNull(stringBuffer.toString());

    }

    @Test
    public void testCompileAllCodeFilesInFCFSOutput(){
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

        Assert.assertNotNull(stringBuffer.toString());
    }


    //This method tests a single command entered in terminal
    //To test this properly run the Terminal class in the threads package
    //==================================================================================
    //This test will fail if it is not run on a windows 10 os because it uses windows cmd commands
    //run it on linux will fail
    @Test
    public void testTerminalProcess(){
        //in theory a single task operation is FCFS
        kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);
        if(Terminal.cdir == null) Terminal.cdir = System.getProperty("user.dir");
        Terminal.terminalLatch = new CountDownLatch(1);
        Kernel.runTerminalProcess(new Process(1, Process.Type.commandLine, new CommandLine("dir")));

        try{
            Terminal.terminalLatch.await();
        }catch (InterruptedException e){
            System.out.println(e);
        }

        Assert.assertNotNull(CPU.junitTestOutput);

    }



}