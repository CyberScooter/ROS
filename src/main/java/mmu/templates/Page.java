package main.java.mmu.templates;

public class Page {
    private byte[] data;
    private String tagName;

    public Page(byte[] data, String tagName){
        this.data = data;
        this.tagName = tagName;
    }

    public byte[] getData() {
        return data;
    }

    public String getTagName() {
        return tagName;
    }
}
