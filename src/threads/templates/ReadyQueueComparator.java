package threads.templates;

import java.util.Comparator;

public class ReadyQueueComparator implements Comparator<Process> {
    private queueType type;

    // algorithm comparing methods
    private int comparing_FCFS_process(Process p1, Process p2)
    {
        if (p1.getId() < p2.getId())
            return -1;
        if (p1.getId() > p2.getId())
            return 1;
        return 0;
    }

    private int comparing_priority(Process p1, Process p2)
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
    public int compare(Process p1, Process p2)
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
