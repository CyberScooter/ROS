package main.java.mmu;
/**
 * VirtualMemory.java
 *
 * Virtual Memory address translation.
 *
 * Given an input parameter, this program extracts
 * the page number and offset. Then reading the byte in
 * the file BACKING_STORE residing at that position.
 *
 * Code adapted and modified from:
 *
 * @author Gagne, Galvin, Silberschatz
 * Operating System Concepts with Java - Eighth Edition
 * Copyright John Wiley & Sons - 2010.
 */


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
	private int pageFault;

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

	public int getPhysicalAddress(int virtualAddress) throws IOException {
		// determine the page number
		pageNumber = getPageNumber(virtualAddress);

		// determine the offset
		offset = getOffset(virtualAddress);

		if ( (frameNumber = checkTLB(pageNumber)) == -1 ) {
			// Check the page table
			if (pageTable[pageNumber].getValid()) {
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

				pageFault ++;

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
	public byte getValue(int physicalAddress) {
		return physicalMemory[((physicalAddress & 0x0000ff00) >> 8)].readWord(physicalAddress & 0x000000ff);
	}

	/**
	 * Generate statistics.
	 */
	public String generateStatistics() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Total Number of Translated Addresses = " + numberOfAddresses).append("\n");
		stringBuffer.append("Total Page Faults = " + replacementAlgorithm.getPageFaultCount()).append("\n");
		stringBuffer.append("Total Page Fault Rate = " + ((float) replacementAlgorithm.getPageFaultCount()) / numberOfAddresses).append("\n");
		stringBuffer.append("Total TLB Hits = " + TLBHits).append("\n");
		stringBuffer.append("Total TLB Hit Rate = " + ((float) TLBHits) / numberOfAddresses).append("\n");

		return stringBuffer.toString();
	}


	public PageTableEntry[] getPageTable() {
		return pageTable;
	}

	public static int getNumberOfFrames() {
		return NUMBER_OF_FRAMES;
	}

	public int getPageFault() {
		return pageFault;
	}

	public ReplacementAlgorithm getReplacementAlgorithm() {
		return replacementAlgorithm;
	}
}
