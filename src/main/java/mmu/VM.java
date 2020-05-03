package main.java.mmu; /**
 * VM.java
 *
 * Virtual Memory address translation.
 *
 * Given an input parameter, this program extracts
 * the page number and offset. Then reading the byte in
 * the file BACKING_STORE residing at that position.
 *
 * Usage:
 *	java VM <input file>
 *
 * @author Gagne, Galvin, Silberschatz
 * Operating System Concepts with Java - Eighth Edition
 * Copyright John Wiley & Sons - 2010. 
 */

import main.java.mmu.LRU.LRU;
import main.java.mmu.LRU.ReplacementAlgorithm;
import main.java.mmu.templates.Frame;
import main.java.mmu.templates.PageTableEntry;
import main.java.mmu.templates.TLBEntry;

import java.io.*;

public class VM
{
	private static final int PAGE_TABLE_ENTRIES = 256;
	private static final int NUMBER_OF_FRAMES = 128;
	//private static final int PHYSICAL_MEMORY_SIZE = 256*256;
	private static final int PHYSICAL_MEMORY_SIZE = Frame.FRAME_SIZE * NUMBER_OF_FRAMES;
	private ReplacementAlgorithm LRU = new LRU(128);
	private static final int PAGE_SIZE = 256;
	//private static final int NUMBER_OF_FRAMES = PHYSICAL_MEMORY_SIZE / PAGE_SIZE;
	private static final int TLB_SIZE = 16;

	private File fileName;				/* the file representing the simulated  disk */
	private RandomAccessFile disk = null;	/* the input file of logical addresses */
	private BufferedReader r = null;

	private int virtualAddress;			/* the virtual address being translated */
	private int physicalAddress;			/* the physical address */

	private int pageNumber;				/* virtual page number */
	private int frameNumber;				/* physical frame number */
	private int offset;					/* offset in page/frame */

	private byte value;					/* the value stored at the physical address */

	private int nextFrameNumber;			/* the next available frame number */
	private int nextTLBEntry;			/* the next available entry in the TLB */

	private PageTableEntry[] pageTable;	/* the page table */
	private Frame[] physicalMemory;		/* physical memory (organized in frames) */

	private TLBEntry[] TLB;				/* the TLB */

	private byte[] buffer;				/* the buffer for storing the page from disk */

	private int pageFaults;				/* the number of page faults */
	private int TLBHits;				/* the number of TLB hits */
	private int numberOfAddresses;		/* the number of addresses that were translated */

	/**
	 * Constructor.
	 *
	 * Intiailizes the various data structures including:
	 *
	 * (1) Page table
	 * (2) TLB
	 * (3) Physical memory
	 */
	public VM() {
		// create the page table
		pageTable = new PageTableEntry[PAGE_TABLE_ENTRIES];
		for (int i = 0; i < PAGE_TABLE_ENTRIES; i++)
			pageTable[i] = new PageTableEntry();

		// create the TLB
		TLB = new TLBEntry[TLB_SIZE];
		for (int i = 0; i < TLB_SIZE; i++)
			TLB[i] = new TLBEntry();

		// allocate the physical memory
		physicalMemory = new Frame[NUMBER_OF_FRAMES];
		for (int i = 0; i < NUMBER_OF_FRAMES; i++)
			physicalMemory[i] = new Frame();

		// initialize the next frame number
		nextFrameNumber = 0;

		// initialize the next available entry in the TLB
		nextTLBEntry = 0;

		// allocate a temporary buffer for reading in from disk
		buffer = new byte[PAGE_SIZE];

		// initialize the statistics counters
		pageFaults = 0;
		TLBHits = 0;
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
	 * Return the number of the next available frame.
	 * This just uses a simple approach of assigning
	 * the next frame in memory.
	 */
	public int getNextFrame() {
		return nextFrameNumber++;
	}

	/**
	 * Check the TLB for a mapping of
	 * page number to physical frame
	 *
	 * @return -1 if no mapping or the frame number >= 0
	 */
	public int checkTLB(int pageNumber) {
		int frameNumber = -1;

		/**
		 * A "real" TLB would use associative memory
		 * where we could check all values in the
		 * TLB memory at the same time. We have to
		 * in fact do a linear search of our TLB
		 */
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

		/**
		 * Update the next TLB entry.
		 *
		 * This uses a very simple FIFO approach for
		 * managing entries in the TLB.
		 */
		nextTLBEntry = (nextTLBEntry + 1) % TLB_SIZE;
	}


	/**
	 * Determine the physical address of a given virtual address
	 */
	public int getPhysicalAddress(int virtualAddress) throws IOException {
		// determine the page number
		pageNumber = getPageNumber(virtualAddress);
		//System.out.println("Page number = " + pageNumber);

		// determine the offset
		offset = getOffset(virtualAddress);
		//System.out.println("offset = " + offset);

		/**
		 * First check the TLB. We only have to run the
		 * algorithm to extract the frame in the case of
		 * a TLB miss. Where we have a TLB hit, we can
		 * directly obtain the associated frame from the
		 * given page number.
		 */
		if ( (frameNumber = checkTLB(pageNumber)) == -1 ) {
			// Check the page table
			if (pageTable[pageNumber].getValid() == true) {
				/** Page Table Hit **/
				frameNumber = pageTable[pageNumber].getFrameNumber();
			} else {    /** Page Fault **/


				disk.seek(pageNumber * PAGE_SIZE);
				disk.readFully(buffer);

				//uses LRU to insert pageNumber
				LRU.insert(pageNumber);
				//retrieves the pageNumber from LRU to add to the 'physicalMemory' which stores the frame data
				frameNumber = LRU.findFrameNumber(pageNumber);

				// copy the contents of the buffer
				// to the appropriate physical frame
				physicalMemory[frameNumber].setFrame(buffer);

				// now establish a mapping
				// of the frame in the page table
				pageTable[pageNumber].setFrameMapping(frameNumber);

			}

			// lastly, update the TLB
			setTLBMapping(pageNumber, frameNumber);

		}

		//frameNumber variable will only be 2^7 bits long
		physicalAddress = (frameNumber << 8) + offset;

		return physicalAddress;
	}

	/**
	 * Returns the signed byte value at the specified physical address.
	 */
	public byte getValue(int physicalAddress) throws IOException {
		/* disk.seek(virtualAddress); */
		// read() returns a byte, but since bytes
		// in Java are signed, we use an integer
		// to store its value to obtain the signed
		// value of the byte
		/* return disk.read(); */

		/**
		 * Essentially, the code below performs the following:
		 * return physicalMemory[frameNumber].readWord(offset);
		 */


		return physicalMemory[((physicalAddress & 0x0000ff00) >> 8)].readWord(physicalAddress & 0x000000ff);
	}

	public void allocateMemory(int virtualAddress){
		try {
			physicalAddress = getPhysicalAddress(virtualAddress);

			numberOfAddresses++;

			value = getValue(physicalAddress);

			System.out.println("Virtual address: " + virtualAddress + " Physical address: " + physicalAddress + " Value: " + value);
		}catch (IOException e){
			System.out.println(e);
		}
	}

	/**
	 * Generate statistics.
	 */
	public void generateStatistics() {
		System.out.println("Number of Translated Addresses = " + numberOfAddresses);
		System.out.println("Page Faults = " + LRU.getPageFaultCount());
		System.out.println("Page Fault Rate = " + ( (float) LRU.getPageFaultCount()) / numberOfAddresses);
		System.out.println("TLB Hits = " + TLBHits);
		System.out.println("TLB Hit Rate = " + ( (float) TLBHits) / numberOfAddresses);
	}

	/**
	 * The primary method that runs the translation of logical to physical addresses.
	 */
	public void runTranslation(String inputFile) throws IOException {

		try {
			r = new BufferedReader(new FileReader("resources/" +inputFile));
			fileName = new File("resources/BACKING_STORE");
			disk = new RandomAccessFile(fileName, "r");
			String stringValue;

			while ( (stringValue = r.readLine()) != null) {

				// read in the virtual address
				virtualAddress = Integer.parseInt(stringValue);

				allocateMemory(virtualAddress);
			}

			generateStatistics();
		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
		finally {
			disk.close();
			r.close();
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: java VM <input file>");
			System.exit(-1);
		}
		else {
			VM vm = new VM();
			vm.runTranslation(args[0]);
		}
	}
}
