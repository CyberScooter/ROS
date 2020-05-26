package test.java.process_scheduler.threads;

import main.java.Kernel;
import main.java.process_scheduler.threads.CPU;
import main.java.process_scheduler.threads.Terminal;
import main.java.process_scheduler.threads.templates.CommandLine;
import main.java.process_scheduler.threads.templates.PCB;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class TestSingleTaskProcess {

    private Kernel kernel;

    @Test
    public void testTerminalProcess(){
        //in theory a single task operation is FCFS
        kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);
        if(Terminal.cdir == null) Terminal.cdir = System.getProperty("user.dir");
        Terminal.terminalLatch = new CountDownLatch(1);
        Kernel.runTerminalProcess(new PCB(1, PCB.Type.commandLine, new CommandLine("dir")));

        try{
            Terminal.terminalLatch.await();
        }catch (InterruptedException e){
            System.out.println(e);
        }

        Assert.assertNotNull(CPU.junitTestOutput);

    }
}
