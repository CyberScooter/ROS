package threads;

import threads.templates.Process;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class Dispatcher extends Thread {
    private final Semaphore semaphore = new Semaphore(1);
    public static ConcurrentHashMap<Process, Integer> cpuThreadInstances;

    public Dispatcher() {
        cpuThreadInstances = new ConcurrentHashMap<>();
    }

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

                        //FCFS - Removes head of queue
                        Process process = readyQueue.poll();
                        Thread thread = new CPU(process);
                        cpuThreadInstances.put(process, process.getId());
                        thread.start();

                        //try implement round robin, add to ready queue again if not finished within time

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
