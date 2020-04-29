package main.java.mmu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VirtualMemory {
    //key is page number, data is bytes
    static HashMap<Integer, byte[]> virtualMemory;
    static int freePageNumber;

    public VirtualMemory() {
        virtualMemory = new HashMap<>();
        freePageNumber = 1;

        //turn text files into data
    }

    public void writeToDisk(byte[] data){
        if(true) {
            //8kb page size == 8kb frame size
            List<byte[]> offset = divideDataIntoFixedFrames(data, 8000);
            for(byte[] pageOffset: offset){
                virtualMemory.put(freePageNumber++, pageOffset);
            }
        }
    }

    //divides bytes in smaller chunk sizes
    private static List<byte[]> divideDataIntoFixedFrames(byte[] source, int chunksize) {

        List<byte[]> result = new ArrayList<>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }

        return result;
    }



}
