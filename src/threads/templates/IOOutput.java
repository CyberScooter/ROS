package threads.templates;

public class IOOutput {
    private String output;
    private int processID;
    private boolean variable;
    private int lineNumber;
    private boolean error;
    public Process processHandling;

    //for string output on its own
    public IOOutput(String output, boolean error) {
        this.output = output;
        this.error = error;
    }

    //for adding iooutput to io queue
    public IOOutput(int processID, String output, int lineNumber) {
        this.processID = processID;
        this.output = output;
        this.lineNumber = lineNumber;
    }


    //for returning output
    public IOOutput(String output, boolean variable, int lineNumber, boolean error, Process processHandling) {
        this.output = output;
        this.variable = variable;
        this.lineNumber = lineNumber;
        this.error = error;
        this.processHandling = processHandling;
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
}

