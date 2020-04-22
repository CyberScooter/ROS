package threads.templates;

import java.io.File;

public class Process {
    private int id;
    private int priority;
    private File file;
    private Type type;
    private State state;
    private CommandLine terminalCode;
    private IOOutput output = null;
    private int lineNumber;
    private String toWrite;
    private boolean handledByIO;

    //FILE HANDLING PROCESSES - USED BY CODE FILES
    public Process(int id, int priority, Type type, File file) {
        this.id = id;
        this.type = type;
        this.file = file;
        this.priority = priority;
    }

    //IO PROCESSES COMING BACK TO READY QUEUE FROM IO THREAD - USED FOR COMPILING CODE FILES
    //or reading code file to display on GUI
    public Process(int id, Type type, int lineNumber, IOOutput output){
        this.id = id;
        this.type = type;
        this.output = output;
        this.lineNumber = lineNumber;
    }

    //FILE WRITING
    public Process(int id, Type type, File file, String toWrite){
        this.id = id;
        this.type = type;
        this.toWrite = toWrite;
        this.file = file;
    }

    //====================COMMAND LINE PROCESSES=====================================
    //for code being sent to CPU to execute
    public Process(int id, Type type, CommandLine terminalCode) {
        this.id = id;
        this.type = type;
        this.terminalCode = terminalCode;
    }

    //process coming back after being processed by IO to the CPU again to be executed
    public Process(int id, Type type, IOOutput output){
        this.id = id;
        this.type = type;
        this.output = output;
    }

    public static enum State {
        ready, running, terminated
    }

    public static enum Type {
        fileCompiling, commandLine, fileReading, fileWriting
    }

    public String toWrite(){
        return this.toWrite;
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

    public CommandLine getTerminalCode() {
        return terminalCode;
    }

    public boolean isHandledByIO() {
        return handledByIO;
    }

    public void setHandledByIO(boolean handledByIO) {
        this.handledByIO = handledByIO;
    }

    @Override
    public String toString() {
        return "Process{" +
                "id=" + id +
                ", priority=" + priority +
                ", type=" + type +
                '}';
    }
}
