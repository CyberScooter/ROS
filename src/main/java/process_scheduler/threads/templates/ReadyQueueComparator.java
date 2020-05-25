package main.java.process_scheduler.threads.templates;
/**
 *
 * Comparator used for sorting queues
 *
 * Code adapted from:
 * @author Mohammed Mayla, https://github.com/MMayla:
 *
 * https://github.com/MMayla/Process-Scheduling-Simulator/blob/master/mini_pack/ReadyQueueComparator.java
 */


import java.util.Comparator;

public class ReadyQueueComparator implements Comparator<PCB> {
    private queueType type;

    // algorithm comparing methods

    //uses processID to execute processes in FCFS basis, the lower process ids are executed
    private int comparing_FCFS_process(PCB p1, PCB p2)
    {
        if (p1.getId() < p2.getId())
            return -1;
        if (p1.getId() > p2.getId())
            return 1;
        return 0;
    }

    //sorts so highest priority is at the start of the list
    private int comparing_priority(PCB p1, PCB p2)
    {
        if(p1.getPriority() < p2.getPriority())
            return 1;
        if(p1.getPriority() > p2.getPriority())
            return -1;
        return 0;
    }
    public static enum queueType
    {
        FCFS_process,
        priority
    }

    public ReadyQueueComparator(queueType qt)
    {
        super();
        type = qt;
    }

    public void setType(queueType qt)
    {
        type = qt;
    }

    @Override
    public int compare(PCB p1, PCB p2)
    {
        switch (type)
        {
            case FCFS_process:
                return comparing_FCFS_process(p1, p2);

            case priority:
                return comparing_priority(p1, p2);
        }

        return 0;
    }
}
