package threads.templates;

import java.io.File;

public class IOOutput {
    private String output;
    private int processID;
    private boolean variable;
    private int lineNumber;
    private boolean error;
    public Process processHandling;
    private Process.Type processType;
    private CommandLine terminalCode;
    private String filename;


    public IOOutput(int processID, CommandLine terminalCode){
        this.processID = processID;
        this.terminalCode = terminalCode;
        this.processType = Process.Type.commandLine;
    }

    //for string output on its own
    public IOOutput(String output, boolean error) {
        this.output = output;
        this.error = error;
    }

    //FILE-HANDLING, COMPILING CODE FILE, for adding iooutput to io queue
    public IOOutput(int processID, String output, int lineNumber) {
        this.processID = processID;
        this.output = output;
        this.lineNumber = lineNumber;
        this.processType = Process.Type.fileCompiling;
    }

    //FILE-HANDLING, READING CODE FILE takes id only
    public IOOutput(int processID, Process process) {
        this.processID = processID;
        this.processHandling = process;
        this.processType = Process.Type.fileReading;
    }


    //FILE-HANDLING (COMPILING FILES), for returning output to CPU used for file compiling
    public IOOutput(String output, boolean variable, int lineNumber, boolean error) {
        this.output = output;
        this.variable = variable;
        this.lineNumber = lineNumber;
        this.error = error;
    }

    //FILE-HANDING (READING FILES/WRITING FILES)
    public IOOutput(String output, String filename){
        this.output = output;
        this.filename = filename;
        this.processType = Process.Type.fileWriting;
    }

    public IOOutput(String output){
        this.output = output;
    }

    public String getFilename(){
        return this.filename;
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

    public Process.Type getProcessType() {
        return processType;
    }

    public Process getProcessHandling() {
        return processHandling;
    }
}

