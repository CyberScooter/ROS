package main.java.mmu.templates;

public class Frame {
    //logical address
    private byte[] offset;

    public Frame(byte[] data){
        addOffset(data);
    }


    //OFSET IS THE SPECIFIED LOCATION IN ARRAY OF WHERE DATA IS!!!
    public void addOffset(byte[] offsetData){
        //lower 14 bytes =
        if(offsetData.length <= 14){
            this.offset = offsetData;
        }
    }


}
