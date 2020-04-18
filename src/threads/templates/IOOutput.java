package threads.templates;

public class IOOutput {
    private String output;
    private int processID;
    private boolean variable;
    private int lineNumber;
    private boolean error;
    public Process processHandling;
    private Process.Type processType;
    private CommandLine terminalCode;


    public IOOutput(int processID, CommandLine terminalCode){
        this.processID = processID;
        this.terminalCode = terminalCode;
    }

    //for string output on its own
    public IOOutput(String output, boolean error) {
        this.output = output;
        this.error = error;
    }

    //FILE-HANDLING, for adding iooutput to io queue
    public IOOutput(int processID, String output, int lineNumber) {
        this.processID = processID;
        this.output = output;
        this.lineNumber = lineNumber;
    }


    //FILE-HANDLING, for returning output to CPU
    public IOOutput(String output, boolean variable, int lineNumber, boolean error, Process processHandling) {
        this.output = output;
        this.variable = variable;
        this.lineNumber = lineNumber;
        this.error = error;
        this.processHandling = processHandling;
    }


    public IOOutput(String output){
        this.output = output;
    }
    

    public String getOutput() {
        return output;
    }

    public boolean isVariable() {
        return variable;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getProcessID(){
        return processID;
    }

    public boolean isError() {
        return error;
    }

    public CommandLine getTerminalCode() {
        return terminalCode;
    }
}

