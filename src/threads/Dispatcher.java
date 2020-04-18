package threads;

import threads.templates.Process;
import threads.templates.ReadyQueueComparator;

import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class Dispatcher extends Thread {
    private final Semaphore semaphore = new Semaphore(1);
    private Process processComing;
    private static PriorityQueue<Process> processPriorityDispatch;
    private static PriorityQueue<Process> processFCFSDispatch;
    private static PriorityQueue<Process> ioDispatch;
    private ReadyQueueComparator.queueType type;

    public Dispatcher(ReadyQueueComparator.queueType type) {
        if(processPriorityDispatch == null && processFCFSDispatch == null && ioDispatch == null){
            processPriorityDispatch = new PriorityQueue<>(50, new ReadyQueueComparator(ReadyQueueComparator.queueType.priority));
            processFCFSDispatch = new PriorityQueue<>(50, new ReadyQueueComparator(ReadyQueueComparator.queueType.FCFS_process));
            ioDispatch = new PriorityQueue<>(50, new ReadyQueueComparator(ReadyQueueComparator.queueType.FCFS_io));
        }
        this.type = type;
    }

    @Override
    public synchronized void run() {
        while (true) {
            try {
                wait();
            } catch (InterruptedException f) {
                try {

                    semaphore.acquire();

                    while(!ProcessCreation.getReadyQueue().isEmpty()){
                        Process process = ProcessCreation.readyQueue.poll();
                        if (process.getIOOutput() != null || process.getType() == Process.Type.commandLine) {
                            new CPU(process).start();
                        }else if(type == ReadyQueueComparator.queueType.priority){
                            processPriorityDispatch.add(process);
                        }else if(type == ReadyQueueComparator.queueType.FCFS_process){
                            processFCFSDispatch.add(process);
                        }
                    }


                    if(type == ReadyQueueComparator.queueType.priority && !processPriorityDispatch.isEmpty()){
                        startCPUThreads(ReadyQueueComparator.queueType.priority);
                    }else if(type == ReadyQueueComparator.queueType.FCFS_process && !processFCFSDispatch.isEmpty()){
                        startCPUThreads(ReadyQueueComparator.queueType.FCFS_process);
                    }


                } catch (InterruptedException e) {
                    System.out.println(e);
                } finally {
                    semaphore.release();
                }

            }
        }

    }


    public void startCPUThreads(ReadyQueueComparator.queueType type){
        if(type == ReadyQueueComparator.queueType.FCFS_process){
            while(!processFCFSDispatch.isEmpty()){
                Process process = processFCFSDispatch.remove();
                new CPU(process).start();
            }
        } else if(type == ReadyQueueComparator.queueType.priority){
            while(!processPriorityDispatch.isEmpty()){
                Process process = processPriorityDispatch.remove();
                new CPU(process).start();
            }
        }


    }
}
