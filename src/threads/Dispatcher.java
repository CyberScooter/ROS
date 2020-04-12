package threads;

import threads.templates.Process;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class Dispatcher extends Thread {
    private final Semaphore semaphore = new Semaphore(1);
    public static ConcurrentHashMap<Process, Integer> cpuThreadInstances;

    @Override
    public synchronized void run() {

        while (true) {
            try {
                wait();
            } catch (InterruptedException f) {
                ConcurrentLinkedQueue<Process> readyQueue = ProcessCreation.getReadyQueue();
                while (!readyQueue.isEmpty()) {
                    try {
                        semaphore.acquire();

                        //PRIORITY QUEUE - Removes head of queue
                        Process process = readyQueue.poll();
                        Thread thread = new CPU(process);
                        thread.start();

                        //FCFS - Removes head of queue


                    } catch (InterruptedException e) {
                        System.out.println(e);
                    } finally {
                        semaphore.release();
                    }

                }
            }
        }

    }
}
