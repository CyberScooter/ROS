package main.java.mmu.PageReplacementAlgorithm;

/**
 * ReplacementAlgorithm.java 
 *
 * Code adapted and modified from:
 *
 * Figure 9.32
 *
 * @author Gagne, Galvin, Silberschatz
 * Operating System Concepts with Java - Eighth Edition
 * Copyright John Wiley & Sons - 2010.
 */


public abstract class ReplacementAlgorithm
{
	// the number of page faults
	protected int pageFaultCount;
	
	// the number of physical page frame
	protected int pageFrameCount;
	
	/**
	 * @param pageFrameCount - the number of physical page frames
	 */
	public ReplacementAlgorithm(int pageFrameCount) {
		if (pageFrameCount < 0)
			throw new IllegalArgumentException();
		
		this.pageFrameCount = pageFrameCount;
		pageFaultCount = 0;
	}
	
	/**
	 * @return - the number of page faults that occurred.
	 */
	public int getPageFaultCount() {
		return pageFaultCount;
	}

	public int getPageFrameCount() {
		return pageFrameCount;
	}
	
	/**
	 * @param int pageNumber - the page number to be inserted
	 */
	public abstract void insert(int pageNumber);

	public abstract int findFrameNumber(int pageNumber);
}
