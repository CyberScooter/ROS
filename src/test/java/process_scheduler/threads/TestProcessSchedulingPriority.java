package test.java.process_scheduler.threads;

import main.java.Kernel;
import main.java.process_scheduler.threads.CPU;
import main.java.process_scheduler.threads.Dispatcher;
import main.java.process_scheduler.threads.templates.PCB;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;
import main.java.views.Controller;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class TestProcessSchedulingPriority {
    private Kernel kernel;
    private LinkedList<PCB> processesToExecute;

    @Test
    public void testCompileCodeFilesPriorityExecutionOrder(){
        kernel = new Kernel(ReadyQueueComparator.queueType.priority);
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

        processesToExecute.clear();
        //clear to ignore read values
        Dispatcher.orderOfExecutionTest.clear();

        PCB pcb = new PCB(4, 7, PCB.Type.fileCompiling, new File("JUnitTestProgram1.txt"));
        PCB pcb2 = new PCB(5, 2, PCB.Type.fileCompiling, new File("JUnitTestProgram2.txt"));
        PCB pcb3 = new PCB(6, 5, PCB.Type.fileCompiling, new File("JUnitTestProgram3.txt"));
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
            int id = Dispatcher.orderOfExecutionTest.poll().getId();
            stringBuffer.append(count + ") PROCESS OF ID: " + id).append(" RAN").append("\n");
            count++;
        }

        CPU.cpuResultsCompiled.clear();

        //output should be the id of the processes that are executed in priority order:
        // id:4 -> id:6 -> id:5
        Assert.assertEquals("1) PROCESS OF ID: 4 RAN\n2) PROCESS OF ID: 6 RAN\n3) PROCESS OF ID: 5 RAN\n", stringBuffer.toString());
    }

    @Test
    public void testCompileCodeFilesPriorityOutput(){
        kernel = new Kernel(ReadyQueueComparator.queueType.priority);
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

        Vector<String> results = new Vector<>();
        PCB pcb = new PCB(4, 7, PCB.Type.fileCompiling, new File("JUnitTestProgram1.txt"));
        PCB pcb2 = new PCB(5, 2, PCB.Type.fileCompiling, new File("JUnitTestProgram2.txt"));
        PCB pcb3 = new PCB(6, 5, PCB.Type.fileCompiling, new File("JUnitTestProgram3.txt"));
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

        //file compiles in order of id:1 --> id:3 --> id:5
        Assert.assertNotNull(stringBuffer.toString());

    }
}
