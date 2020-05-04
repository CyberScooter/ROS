package main.java.mmu;

import main.java.mmu.PageReplacementAlgorithm.FIFO;
import main.java.mmu.PageReplacementAlgorithm.LRU;
import main.java.mmu.PageReplacementAlgorithm.ReplacementAlgorithm;

import java.io.*;
import java.util.ArrayList;

public class MMU {
    private VirtualMemory virtualMemory;
    private RandomAccessFile disk;
    private ArrayList<String> results;

    public MMU(ReplacementAlgorithm algorithm) {
        if(!new File("resources/BACKING_STORE").exists()) {
            try{
                new MakeBACKING_STORE();
            }catch (IOException e){
                System.out.println(e);
            }
        }
        try {
            disk = new RandomAccessFile("resources/BACKING_STORE", "r");
            virtualMemory = new VirtualMemory(algorithm, disk);
        }catch (IOException e){
            System.out.println(e);
        }
        results=new ArrayList<>();

    }


    public static void main(String[] args) throws IOException{
        MMU mmu = new MMU(new LRU(4));
        mmu.allocateMemory(new File("InputFile4.txt"));
    }


    //multiple virtual addresses
    public void allocateMemory(File file) throws IOException{
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("resources/" + file.getName()));
            String address;
            while ( (address = reader.readLine()) != null) {

                // read in the virtual address
                int virtualAddress = Integer.parseInt(address);

                int physicalAddress = virtualMemory.getPhysicalAddress(virtualAddress);

                virtualMemory.numberOfAddresses++;

                int value = virtualMemory.getValue(physicalAddress);

                results.add("Virtual address: " + virtualAddress + " Physical address: " + physicalAddress + " Value: " + value);

            }
            for(String item: results){
                System.out.println(item);
            }

            virtualMemory.generateStatistics();
        }catch (IOException e){
            System.out.println(e);
        }finally {
            reader.close();
        }

    }

    //single virtual address
    public void allocateMemory(int virtualAddress) throws IOException{
        int physicalAddress = virtualMemory.getPhysicalAddress(virtualAddress);

        virtualMemory.numberOfAddresses++;

        int value = virtualMemory.getValue(physicalAddress);

        System.out.println("Virtual address: " + virtualAddress + " Physical address: " + physicalAddress + " Value: " + value);

        virtualMemory.generateStatistics();
    }
}
