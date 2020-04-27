package mmu.templates;

public class Frame {
    private int number;
    private byte[] pageOffset;
    private boolean inMainMemory;

    public Frame(int number, byte[] pageOffset){
        this.number = number;
        this.pageOffset = pageOffset;
    }

    public void setInMainMemory(boolean inMainMemory) {
        this.inMainMemory = inMainMemory;
    }
}
