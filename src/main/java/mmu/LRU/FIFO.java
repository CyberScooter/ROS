package main.java.mmu.LRU;

/**
 * This class implements the FIFO page-replacement strategy.
 *
 * @author Gagne, Galvin, Silberschatz
 * Operating System Concepts with Java - Eighth Edition
 * Copyright John Wiley & Sons - 2010.
 */

public class FIFO extends ReplacementAlgorithm
{
	// FIFO list of page frames
	private FIFOList frameList;

	/**
	 * @param pageFrameCount - the number of physical page frames
	 */
	public FIFO(int pageFrameCount) {
		super(pageFrameCount);
		frameList = new FIFOList(pageFrameCount);
	}


	/**
	 * insert a page into a page frame.
	 * @param int pageNumber - the page number being inserted.
	 */
	public void insert(int pageNumber) {
		frameList.insert(pageNumber);
		if (System.getProperty("debug") != null) {
			System.out.print("Inserting " + pageNumber);
			frameList.dump();
			System.out.println();
		}
	}

	//finds frame number in list
	public int findFrameNumber(int pageNumber){
		return 1;
	}

	public void remove(int pageNumber){

	}


	class FIFOList
	{
		// the page frame list
		int[] pageFrameList;

		// the number of elements in the page frame list
		int elementCount;

		FIFOList(int pageFrameCount) {
			pageFrameList = new int[pageFrameCount];
			elementCount = 0;
		}

		/**
		 * @param pageNumber the number of the page to be 
		 *	inserted into the page frame list.
		 */
		void insert(int pageNumber) {
			if (!search(pageNumber)) {
				// an asterisk indicates a page fault
				if (System.getProperty("debug") != null)
					System.out.print("*");
				pageFaultCount++;
				pageFrameList[(elementCount++ % pageFrameCount)] = pageNumber;
			}
		}

		// dump the page frames
		void dump() {
			for (int i = 0; i < pageFrameList.length; i++)
				System.out.print("["+i+"]"+pageFrameList[i]+", ");
		}


		/**
		 * Searches for page pageNumber in the page frame list
		 * @return true if pageNumber was found
		 * @return false if pageNumber was not found
		 */
		boolean search(int pageNumber) {
			boolean returnVal = false;

			for (int i = 0; i < pageFrameList.length; i++) {
				if (pageNumber == pageFrameList[i]) {
					returnVal = true;
					break;
				}
			}
			return returnVal;
		}
	}
}
