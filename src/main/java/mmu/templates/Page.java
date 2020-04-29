package main.java.mmu.templates;

public class Page {
    private int number;
    private byte[] offset;

    public Page(int number, byte[] offset){
        this.number = number;
        this.offset = offset;
    }
}
