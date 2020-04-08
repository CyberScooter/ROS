package threads.templates;

public class Process {
    private int id;
    private String file;
    private Type type;
    private State state;
    private int maxLines;
    private IOOutput output = null;


    //FILE HANDLING PROCESSES
    public Process(int id, Type type, String file) {
        this.id = id;
        this.type = type;
        this.file = file;
    }

    //IO PROCESS BEING SENT TO READY QUEUE ONCE AT THE START
    public Process(int id, Type type) {
        this.id = id;
        this.type = type;
    }

    //IO PROCESSES COMING BACK TO READY QUEUE FROM IO THREAD
    public Process(int id, Type type, IOOutput output){
        this.id = id;
        this.type = type;
        this.output = output;
    }

    public static enum State {
        ready, running, terminated
    }

    public static enum Type {
        fileHandling, commandLine
    }

    public int getMaxLines() {
        return maxLines;
    }

    public String getFile() {
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

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }
}
