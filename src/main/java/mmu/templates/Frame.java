package main.java.mmu.templates;

public class Frame {
    public static final int FRAME_SIZE = 256;
    private byte[] frameValue;

    public Frame() {
        frameValue = new byte[FRAME_SIZE];
    }

    public void setFrame(byte[] bytes) {
        /**
         * Make sure we use System.arraycopy() as we don't
         * want the frame to be a unique refernece.
         */
        System.arraycopy(bytes, 0, frameValue, 0, FRAME_SIZE);
    }

    public byte readWord(int offset) {
        return frameValue[offset];
    }

    public String getFrameValue(){
        return new String(frameValue);
    }

}
