package main.java.process_scheduler.threads;

import main.java.process_scheduler.threads.templates.PCB;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class Dispatcher extends Thread {
    private final Semaphore semaphore = new Semaphore(1);
    private static PriorityQueue<PCB> priorityDispatcher;
    private static PriorityQueue<PCB> fcfsDispatcher;
    private ReadyQueueComparator.queueType type;

    //used for process scheduling junit test cases to determine order in which process is dispatched to CPU
    public static LinkedList<PCB> orderOfExecutionTest;

    public Dispatcher(ReadyQueueComparator.queueType type) {
        if(priorityDispatcher == null && fcfsDispatcher == null && orderOfExecutionTest == null){
            priorityDispatcher = new PriorityQueue<>(50, new ReadyQueueComparator(ReadyQueueComparator.queueType.priority));
            fcfsDispatcher = new PriorityQueue<>(50, new ReadyQueueComparator(ReadyQueueComparator.queueType.FCFS_process));
            orderOfExecutionTest = new LinkedList<>();
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
                        PCB pcb = ProcessCreation.readyQueue.poll();
                        if (pcb.getIOOutput() != null || pcb.getType() == PCB.Type.commandLine) {
                            new CPU(pcb).start();
                        }else if(type == ReadyQueueComparator.queueType.priority){
                            priorityDispatcher.add(pcb);
                        }else if(type == ReadyQueueComparator.queueType.FCFS_process){
                            fcfsDispatcher.add(pcb);

                        }
                    }

                    if(type == ReadyQueueComparator.queueType.priority && !priorityDispatcher.isEmpty()){
                        startCPUThreads(ReadyQueueComparator.queueType.priority);
                    }else if(type == ReadyQueueComparator.queueType.FCFS_process && !fcfsDispatcher.isEmpty()){
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

    //starts cpu threads based on priority or fcfs basis
    public synchronized void startCPUThreads(ReadyQueueComparator.queueType type){
        if(type == ReadyQueueComparator.queueType.FCFS_process){
            while(!fcfsDispatcher.isEmpty()){
                PCB pcb = fcfsDispatcher.poll();
                //added to list that is used in junit test, shows the order in which process is sent to CPU for execution
                orderOfExecutionTest.add(pcb);
                new CPU(pcb).start();
            }
        } else if(type == ReadyQueueComparator.queueType.priority){
            while(!priorityDispatcher.isEmpty()){
                PCB pcb = priorityDispatcher.poll();
                //added to list that is used in junit test, shows the order in which process is sent to CPU for execution
                orderOfExecutionTest.add(pcb);
                new CPU(pcb).start();
            }
        }


    }
}
