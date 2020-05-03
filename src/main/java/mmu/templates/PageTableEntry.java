package main.java.mmu.templates;

public class PageTableEntry {
    private int frameNumber;
    //frame in memory
    private boolean valid;

    public PageTableEntry() {
        this.frameNumber = -1;
        valid = false;
    }


    public boolean getValid() {
        return valid;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameMapping(int frameNumber){
        this.frameNumber = frameNumber;
        valid = true;
    }

    public void clearFrameMapping(){
        this.frameNumber = -1;
        valid = false;
    }

}
