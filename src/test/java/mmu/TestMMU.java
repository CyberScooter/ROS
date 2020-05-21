package test.java.mmu;

import main.java.mmu.MMU;
import main.java.mmu.PageReplacementAlgorithm.FIFO;
import main.java.mmu.PageReplacementAlgorithm.LRU;
import main.java.mmu.templates.PageTableEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

//These Tests do not test functionality of GUI, this can only be tested by the end user itself by opening the JAR file in out folder
//these test cases test the functionality of the MMU code directly
public class TestMMU {
    //used MMU class directly to make it easier to test
    private MMU mmu;

    //this tests whether the virtual address translation produces results
    @Test
    public void testVirtualAddressTranslation(){
        mmu = new MMU(new LRU(128));


        try {
            //'InputFile128.txt' is a file that has 172 virtual addresses which cause 128 page faults, meaning that 128 frames in main memory will be
            //fully occupied. Once the physical memory is set to this fixed frame count of 128, the MMU allows more than 128 page faults to occur
            //it uses the LRU and FIFO algorithms to replace existing frames in memory, there are tests to support this in this test class.
            mmu.allocateMemory(new File("InputFile128.txt"));
        }catch (IOException e){
            System.out.println(e);
        }

        //value at offset in physical address is dependent on the backing store data
        Assert.assertNotNull(mmu.getResults());

    }

    @Test
    public void testPageFault(){
        mmu = new MMU(new LRU(128));

        try{
            mmu.allocateMemory(new File("InputFile128.txt"));
        }catch (IOException e){
            System.out.println(e);
        }

        Assert.assertEquals(128, mmu.getVirtualMemory().getPageFault());
    }


    @Test
    public void testPageTableEntryFill(){
        mmu = new MMU(new LRU(128));

        //currently pageTable can store up to 256 entries but in this case the frames in main memory
        //is 128 so there should only be 128 entries in page table, code below will test this

        int pageTableEntries = 0;
        try {
            mmu.allocateMemory(new File("InputFile128.txt"));
        }catch (IOException e){
            System.out.println(e);
        }
        PageTableEntry[] pageTable = mmu.getVirtualMemory().getPageTable();

        for(PageTableEntry item : pageTable){
            if(item.getFrameNumber() != -1) pageTableEntries ++;
        }

        Assert.assertEquals(128, pageTableEntries );
    }

    @Test
    public void testPagingLRUAlgorithm(){
        //4 frames in main memory, this LRU argument is passed onto physical memory size as well in virtual memory class
        //frames index start from 0
        mmu = new MMU(new LRU(4));

        try {
            //this will add the first 4 virtual addresses via paging (into pagetables, tlb and main memory)
            mmu.allocateMemory(new File("InputFile4.txt"));

            //this virtual address has the same page number as '515' in the 'InputFile4.txt', so there will be a page hit
            mmu.allocateMemory(585);

            //this virtual address has the same page number as '16916' in the 'InputFile4.txt', so there will be a page hit
            mmu.allocateMemory(17071);

            //The addresses allocated below are the ones that will be added to main memory and will need to replace currently occupying frames in memory

            //however this virtual address has a different page number to all of the addresses currently in the page table so it will give a page fault
            mmu.allocateMemory(60746);

            //however this virtual address has a different page number to all of the addresses currently in the page table so it will give a page fault
            mmu.allocateMemory(18145);

        }catch (IOException e){
            System.out.println(e);
        }

        // THE physical memory only stores up to 4 FRAMES in this case
        // in main memory the first two frames were recently accessed with the virtual addresses of : 585 and 17071 that had the same page numbers as allocated above
        // so according to LRU algorithm they will be untouched from main memory
        // however the virtual addresses in lines 3 and 4 from the file were not recently used so those frames will be replaced in main memory
        // to prove this I will extract the page number and frame number data from the LRU class
        // this class ONLY stores the mapping of which page goes into which frame in main memory, this information is fed directly to the main memory
        // LRU will be used to display the mapping for this test

        String[] actual = new String[2];

        //this is the 5th page fault and should replace the Least recently used frame which is in frame index 2 (3rd frame in memory as as index start from 0 -> 4 in this case)
        //converts virtual address to page number by diving by 256 giving the higher bits
        actual[0] = "FRAME NUMBER IN MEMORY: " + mmu.getVirtualMemory().getReplacementAlgorithm().findFrameNumber(60746/256);

        //this is the 6th page fault and should replace the Least recently used frame which is in frame index 3 (4th frame in memory as index start from 0 -> 4 in this case)
        //converts virtual address to page number by diving by 256 giving the higher bits
        actual[1] = "FRAME NUMBER IN MEMORY: " + mmu.getVirtualMemory().getReplacementAlgorithm().findFrameNumber(18145/256);

        String[] expected = new String[2];
        expected[0] = "FRAME NUMBER IN MEMORY: 2";
        expected[1] = "FRAME NUMBER IN MEMORY: 3";

        Assert.assertArrayEquals(expected, actual);

    }

    //exactly like test above but using FIFO instead
    @Test
    public void testPagingFIFOAlgorithm(){
        //4 frames in main memory, this LRU argument is passed onto physical memory size as well in virtual memory class
        //frames index start from 0
        mmu = new MMU(new FIFO(4));

        try {
            //this will add the first 4 virtual addresses via paging (into pagetables, tlb and main memory)
            mmu.allocateMemory(new File("InputFile4.txt"));

            //this virtual address has the same page number as '515' in the 'InputFile4.txt', so there will be a page hit
            mmu.allocateMemory(585);

            //this virtual address has the same page number as '16916' in the 'InputFile4.txt', so there will be a page hit
            mmu.allocateMemory(17071);

            //The addresses allocated below are the ones that will be added to main memory and will need to replace currently occupying frames in memory

            //however this virtual address has a different page number to all of the addresses currently in the page table so it will give a page fault
            mmu.allocateMemory(60746);

            //however this virtual address has a different page number to all of the addresses currently in the page table so it will give a page fault
            mmu.allocateMemory(18145);

        }catch (IOException e){
            System.out.println(e);
        }

        // THE physical memory only stores up to 4 FRAMES in this case
        // according to FIFO algorithm the first two frames added to memory will be removed and replaced by the two virtual addresses that caused the page fault
        // to prove this I will extract the page number and frame number data from the FIFO class
        // this class ONLY stores the mapping of which page goes into which frame in main memory, this information is fed directly to the main memory
        // FIFO will be used to display the mapping for this test

        String[] actual = new String[2];

        //this is the 5th page fault and should replace the first in frame which is in frame index 0 ( 1st frame in memory as as index start from 0 -> 4 )
        //converts virtual address to page number by diving by 256 giving the higher bits
        actual[0] = "FRAME NUMBER IN MEMORY: " + mmu.getVirtualMemory().getReplacementAlgorithm().findFrameNumber(60746/256);

        //this is the 6th page fault and should replace the next first in frame which is in frame index 1 ( 2nd frame in memory as index start from 0 -> 4 )
        //converts virtual address to page number by diving by 256 giving the higher bits
        actual[1] = "FRAME NUMBER IN MEMORY: " + mmu.getVirtualMemory().getReplacementAlgorithm().findFrameNumber(18145/256);

        String[] expected = new String[2];
        expected[0] = "FRAME NUMBER IN MEMORY: 0";
        expected[1] = "FRAME NUMBER IN MEMORY: 1";

        Assert.assertArrayEquals(expected, actual);

    }
}
