package threads.templates;

import java.util.concurrent.ConcurrentHashMap;

//each index represents order of execution for reading code from file
//class gives template for io and cpu results
public class Output implements Comparable<Output>{
    //line in which code is executed in code file
    private int line;
    private ConcurrentHashMap<String, Integer> variable;
    private ConcurrentHashMap<String, String> arithmeticCalculation;
    private int calculation;
    private int processID;
    private IOOutput IOOutput;
    private Type calculationType;
    private boolean exit;
    private boolean error;
    private String errorMessage;
    private CommandLine commandLine;

    //constructor for if line is variable assignment, cpu result
    public Output(int processID, int line, String type, int value) {
        this.processID = processID;
        this.line = line;
        this.variable = new ConcurrentHashMap<String, Integer>();
        this.variable.put(type, value);
    }

    //constructor for CPU calculation results, cpu result
    public Output(int processID, int line, String variable, String calculation, Type calculationType) {
        this.processID = processID;
        this.line = line;
        arithmeticCalculation = new ConcurrentHashMap<>();
        arithmeticCalculation.put(variable, calculation);
        this.calculationType = calculationType;
    }
    //output constructor for IO results from IO thread -> IO Queue -> ReadyQueue -> CPU
    public Output(int processID, IOOutput IOOutput){
        this.IOOutput = IOOutput;
        this.processID = processID;
    }

    //output constructor for exit code
    public Output(int processID, boolean exit){
        this.exit = exit;
        this.processID = processID;
    }

    //output constructor for error handling
    public Output(int processID, boolean error, String errorMessage){
        this.processID = processID;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public static enum Type{
        addition,multiplication,subtraction
    }

    public IOOutput getIOOutput() {
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

    public ConcurrentHashMap<String, Integer> getVariable() {
        return variable;
    }

    public ConcurrentHashMap<String, String> getArithmeticCalculation() {
        return arithmeticCalculation;
    }

    public Type getCalculationType() {
        return calculationType;
    }

    public boolean getExit(){
        return this.exit;
    }

    public boolean isError() {
        return error;
    }



    public String getErrorMessage() {
        return errorMessage;
    }
}

