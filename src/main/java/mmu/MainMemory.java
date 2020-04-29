package main.java.mmu;

import main.java.mmu.templates.Frame;

public class MainMemory {
    Frame[] frames;

    private static int freeFrame;


    public MainMemory() {
        // 1 byte for storing page number therefore 256 combinations
        this.frames = new Frame[256];
        freeFrame = 0;
    }

    public int addFrame(Frame frame){
        this.frames[this.freeFrame] = frame;
        this.freeFrame++;
        return freeFrame - 1;
    }

    public int getValue(int frameNumber){
        return 0;
    }
}
