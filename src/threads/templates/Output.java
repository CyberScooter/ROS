package threads.templates;

import java.util.concurrent.ConcurrentHashMap;

//each index represents order of execution for reading code from file
//class gives template for io and cpu results
public class Output implements Comparable<Output>{
    //line in which code is executed in code file
    private int line;
    private ConcurrentHashMap<String, Integer> variable;
    private int calculation;
    private int processID;
    private String IOOutput;

    //constructor for if line is variable assignment, cpu result
    public Output(int processID, int line, String type, int value) {
        this.processID = processID;
        this.line = line;
        this.variable = new ConcurrentHashMap<String, Integer>();
        this.variable.put(type, value);
    }

    //constructor for CPU calculation results, cpu result
    public Output(int processID, int line, int calculation) {
        this.processID = processID;
        this.line = line;
        this.calculation = calculation;
    }

    //output constructor for IO results from IO thread and IO queue back to CPU
    public Output(int processID, int line, String IOOutput){
        this.processID = processID;
        this.line = line;
        this.IOOutput = IOOutput;
    }

    public String getIOOutput() {
        return IOOutput;
    }

    @Override
    public int compareTo(Output o) {
        if(this.line > o.line){
            return  1;
        }else if(this.line < o.line){
            return -1;
        }
        return 0;
    }

    public int getLine() {
        return line;
    }

    public int getProcessID() {
        return processID;
    }
}
