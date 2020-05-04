package main.java.mmu.templates;


public class TLBEntry
{
    private int pageNumber;
    private int frameNumber;
    private boolean isValid;

    public TLBEntry() {
        pageNumber = -1;
        frameNumber = -1;
        isValid = false;
    }

    public boolean setMapping(int pageNumber, int frameNumber) {
        this.pageNumber = pageNumber;
        this.frameNumber = frameNumber;
        isValid = true;

        return isValid;
    }

    public boolean checkPageNumber(int pageNumber) {
        if (pageNumber == this.pageNumber)
            return true;
        else
            return false;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
