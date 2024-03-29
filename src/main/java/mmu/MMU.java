package main.java.mmu;

import main.java.mmu.PageReplacementAlgorithm.LRU;
import main.java.mmu.PageReplacementAlgorithm.ReplacementAlgorithm;
import main.java.mmu.templates.MakeBACKING_STORE;

import java.io.*;

public class MMU {
    private VirtualMemory virtualMemory;
    private RandomAccessFile disk;
    private String results;

    //for test case
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

    }

    //multiple virtual addresses
    public boolean allocateMemory(File file) throws IOException{
        BufferedReader reader = null;
        try {
            if(!new File("resources/" + file.getName()).exists()) return false;
            reader = new BufferedReader(new FileReader("resources/" + file.getName()));
            String address;
            StringBuffer stringBuffer = new StringBuffer();
            while ( (address = reader.readLine()) != null) {
                int currentPageFault = virtualMemory.getPageFault();

                // read in the virtual address
                int virtualAddress = Integer.parseInt(address);

                int physicalAddress = virtualMemory.getPhysicalAddress(virtualAddress);

                virtualMemory.numberOfAddresses++;

                int value = virtualMemory.getValue(physicalAddress);

                stringBuffer.append("Virtual address: " + virtualAddress)
                        .append(", Physical address: " + physicalAddress)
                        .append( ", Page Number: " + virtualAddress/256);

                if(currentPageFault != virtualMemory.getPageFault()){
                    stringBuffer.append(", Value: " + value)
                            .append(", Page Fault: true").append("\n");
                }else {
                    stringBuffer.append(", Page Fault: false").append("\n");
                }


            }
            results = stringBuffer.toString();

        }catch (IOException e){
            System.out.println(e);
        }finally {
            reader.close();
        }

        return true;

    }

    //single virtual address
    public void allocateMemory(int virtualAddress) throws IOException{
        int currentPageFault = virtualMemory.getPageFault();
        int physicalAddress = virtualMemory.getPhysicalAddress(virtualAddress);

        virtualMemory.numberOfAddresses++;

        try {
            int value = virtualMemory.getValue(physicalAddress);
            if(currentPageFault != virtualMemory.getPageFault()){
                results = "Virtual address: " + virtualAddress + ", Physical address: " + physicalAddress + ", Page Number: " + virtualAddress/256 + ", Value: " + value + ", Page Fault: true";
            }else {
                results = "Virtual address: " + virtualAddress + ", Physical address: " + physicalAddress + ", Page Fault: false";
            }


        }catch (ArrayIndexOutOfBoundsException e){
            results = e.getMessage();
        }


    }

    public String getStatistics(){
        return virtualMemory.generateStatistics();
    }

    public int getNumberOfFramesInMemory(){
        return VirtualMemory.getNumberOfFrames();
    }

    public void clearResults() {
        results = "";
    }

    public String getResults() {
        return results;
    }

    public VirtualMemory getVirtualMemory() {
        return virtualMemory;
    }


}
