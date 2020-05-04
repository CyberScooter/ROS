package main.java.mmu;

import main.java.mmu.PageReplacementAlgorithm.ReplacementAlgorithm;
import main.java.mmu.templates.Frame;
import main.java.mmu.templates.PageTableEntry;
import main.java.mmu.templates.TLBEntry;

import java.io.*;

public class VirtualMemory
{
	private static final int PAGE_TABLE_ENTRIES = 256;
	private static int NUMBER_OF_FRAMES;

	private ReplacementAlgorithm replacementAlgorithm;
	private static final int PAGE_SIZE = 256;

	private static final int TLB_SIZE = 16;

	private RandomAccessFile disk = null;

	private int pageNumber;
	private int frameNumber;
	private int offset;

	private int nextTLBEntry;

	private PageTableEntry[] pageTable;
	private Frame[] physicalMemory;

	private TLBEntry[] TLB;

	private byte[] buffer;

	private int TLBHits;
	public int numberOfAddresses;

	public VirtualMemory(ReplacementAlgorithm algorithm, RandomAccessFile disk) {
		NUMBER_OF_FRAMES = algorithm.getPageFrameCount();

		//index of pagetable is the page number
		pageTable = new PageTableEntry[PAGE_TABLE_ENTRIES];
		for (int i = 0; i < PAGE_TABLE_ENTRIES; i++)
			pageTable[i] = new PageTableEntry();

		//index is the element in the table which includes a class that stores the validity, frame and page number
		TLB = new TLBEntry[TLB_SIZE];
		for (int i = 0; i < TLB_SIZE; i++)
			TLB[i] = new TLBEntry();

		//index is a frame with the data in it
		physicalMemory = new Frame[NUMBER_OF_FRAMES];
		for (int i = 0; i < NUMBER_OF_FRAMES; i++)
			physicalMemory[i] = new Frame();


		// initialize the next available entry in the TLB
		nextTLBEntry = 0;

		// allocate a temporary buffer for reading in from disk
		buffer = new byte[PAGE_SIZE];

		// initialize the statistics counters
		TLBHits = 0;

		this.replacementAlgorithm = algorithm;
		this.disk = disk;
	}


	/**
	 * Extract the page number.
	 */
	public int getPageNumber(int virtualAddress) {
		return  (virtualAddress & 0x0000ff00) >> 8;
	}

	/**
	 * Extract the offset.
	 */
	public int getOffset(int virtualAddress) {
		return (virtualAddress & 0x000000ff);
	}


	/**
	 * Check the TLB for a mapping of
	 * page number to physical frame
	 *
	 * @return -1 if no mapping or the frame number >= 0
	 */
	public int checkTLB(int pageNumber) {
		int frameNumber = -1;

		for (int i = 0; i < TLB_SIZE; i++) {
			if (TLB[i].checkPageNumber(pageNumber)) {
				frameNumber = TLB[i].getFrameNumber();
				TLBHits++;

				break;
			}
		}

		return frameNumber;
	}

	/**
	 * Maps a page number to its frame number in the TLB.
	 */
	public void setTLBMapping(int pageNumber, int frameNumber) {
		// establish the mapping
		TLB[nextTLBEntry].setMapping(pageNumber, frameNumber);

		nextTLBEntry = (nextTLBEntry + 1) % TLB_SIZE;
	}


	/**
	 * Determine the physical address of a given virtual address
	 */
	public int getPhysicalAddress(int virtualAddress) throws IOException {
		// determine the page number
		pageNumber = getPageNumber(virtualAddress);

		// determine the offset
		offset = getOffset(virtualAddress);

		if ( (frameNumber = checkTLB(pageNumber)) == -1 ) {
			// Check the page table
			if (pageTable[pageNumber].getValid() == true) {
				/** Page Table Hit **/
				frameNumber = pageTable[pageNumber].getFrameNumber();
				//insert in LRU is used in case it already exists, the LRU scheduler takes in the pagenumber to calculate the LRU calculation
				//so that when the physical memory is full, it will understand how to replace the old page with the new page through LRU scheduling
				replacementAlgorithm.insert(pageNumber);
			} else {    /** Page Fault **/


				disk.seek(pageNumber * PAGE_SIZE);
				disk.readFully(buffer);

				//uses LRU to insert pageNumber
				replacementAlgorithm.insert(pageNumber);

				//retrieves the pageNumber from Algorithm PageFrameTable to add to the 'physicalMemory' which stores the frame data
				frameNumber = replacementAlgorithm.findFrameNumber(pageNumber);

				// copy the contents of the buffer
				// to the appropriate physical frame
				physicalMemory[frameNumber].setFrame(buffer);

				// now establish a mapping
				// of the frame in the page table
				pageTable[pageNumber].setFrameMapping(frameNumber);

			}

			// lastly, update the TLB
			setTLBMapping(pageNumber, frameNumber);

		}else{
			//insert in LRU is used in case it already exists, the LRU scheduler takes in the pagenumber to calculate the LRU calculation
			//so that when the physical memory is full, it will understand how to replace the old page with the new page through LRU scheduling
			replacementAlgorithm.insert(pageNumber);
		}

		//frameNumber variable will only be 2^7 bits long
		int physicalAddress = (frameNumber << 8) + offset;

		return physicalAddress;
	}

	/**
	 * Returns the signed byte value at the specified physical address.
	 */
	public byte getValue(int physicalAddress) throws IOException {
		return physicalMemory[((physicalAddress & 0x0000ff00) >> 8)].readWord(physicalAddress & 0x000000ff);
	}

	/**
	 * Generate statistics.
	 */
	public void generateStatistics() {
		System.out.println("Number of Translated Addresses = " + numberOfAddresses);
		System.out.println("Page Faults = " + replacementAlgorithm.getPageFaultCount());
		System.out.println("Page Fault Rate = " + ( (float) replacementAlgorithm.getPageFaultCount()) / numberOfAddresses);
		System.out.println("TLB Hits = " + TLBHits);
		System.out.println("TLB Hit Rate = " + ( (float) TLBHits) / numberOfAddresses);

		//testing
		System.out.println("Frame in which virtual address 515 is in: " + replacementAlgorithm.findFrameNumber(getPageNumber(515)));
		System.out.println("Frame in which virtual address 16916 is in: " + replacementAlgorithm.findFrameNumber(getPageNumber(16916)));
		System.out.println("Frame in which virtual address 62493 is in: " + replacementAlgorithm.findFrameNumber(getPageNumber(62493)));
		System.out.println("Frame in which virtual address 30198 is in: " + replacementAlgorithm.findFrameNumber(getPageNumber(30198)));
		System.out.println("===============================================================================================================");
		System.out.println("Frame number in which address 60746 is in frame: " + replacementAlgorithm.findFrameNumber(getPageNumber(60746)));
		System.out.println("Frame number in which address 18145 is in frame: " + replacementAlgorithm.findFrameNumber(getPageNumber(18145)));

		System.out.println("515 in tlb: " + TLB[0].checkPageNumber(515 / 256));
		System.out.println("16916 in tlb: " + TLB[1].checkPageNumber(16916 / 256));


		//output 0 for LRU
		//output -1 for FIFO
		System.out.println("515 in PT: " + pageTable[515/256].getFrameNumber());
		//output 1 for LRU
		//output -1 for FIFO
		System.out.println("16916 in PT: " + pageTable[16916/256].getFrameNumber());




	}

}
