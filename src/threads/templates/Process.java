package threads.templates;

import java.io.File;

public class Process {
    private int id;
    private int priority;
    private File file;
    private Type type;
    private State state;
    private String terminalCode;
    private IOOutput output = null;
    private int lineNumber;


    //FILE HANDLING PROCESSES - USED BY CODE FILES
    public Process(int id, int priority, Type type, File file) {
        this.id = id;
        this.type = type;
        this.file = file;
        this.priority = priority;
    }

    //IO PROCESS BEING SENT TO READY QUEUE ONCE AT THE START
    public Process(int id, int priority, Type type, String terminalCode) {
        this.id = id;
        this.type = type;
    }

    //IO PROCESSES COMING BACK TO READY QUEUE FROM IO THREAD - USED BY CODE FILES
    public Process(int id, Type type, int lineNumber, IOOutput output){
        this.id = id;
        this.type = type;
        this.output = output;
        this.lineNumber = lineNumber;
    }

    public static enum State {
        ready, running, terminated
    }

    public static enum Type {
        fileHandling, commandLine
    }

    public int getPriority() {
        return priority;
    }

    public int getLineNumber(){ return lineNumber; }

    public File getFile() {
        return file;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public IOOutput getIOOutput() {
        return output;
    }

}
