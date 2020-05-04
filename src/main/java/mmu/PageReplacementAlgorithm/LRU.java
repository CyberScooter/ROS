package main.java.mmu.PageReplacementAlgorithm;

/**
 * This class implements the LRU page-replacement strategy.
 *
 * @author Gagne, Galvin, Silberschatz
 * Operating System Concepts with Java - Eighth Edition
 * Copyright John Wiley & Sons - 2010.
 */

public class LRU extends ReplacementAlgorithm
{
	// LRU list of page frames
	private LRUList frameList;

	/**
	 * @param pageFrameCount - the number of physical page frames
	 */
	public LRU(int pageFrameCount) {
		super(pageFrameCount);
		frameList = new LRUList(pageFrameCount);
	}

	/**
	 * Insert a page into a page frame.
	 */
	public void insert(int pageNumber) {
		frameList.insert(pageNumber);
		if (System.getProperty("debug") != null) {
			System.out.print("Inserting " + pageNumber);
			frameList.dump();
			System.out.println();
		}
	}

	//finds frame number in pageFrameList in LRUList
	//to save frame number to physical disk when inserting
	public int findFrameNumber(int pageNumber){
		return frameList.search(pageNumber);
	}


	class LRUList
	{
		// the page frame list
		int[] pageFrameList;

		// the number of elements in the page frame list
		int elementCount;

		// the last page inserted
		int lastInserted = -1;

		LRUList(int pageFrameCount) {
			pageFrameList = new int[pageFrameCount];
			elementCount = 0;
		}

		/**
		 * @param pageNumber the number of the page to be 
		 *	inserted into the page frame list.
		 */
		void insert(int pageNumber) {
			int searchVal;

			// if we didn't find it, replace the LRU page
			if ((searchVal = search(pageNumber)) == -1) { 
				if (System.getProperty("debug") != null)
					System.out.print("*");

				pageFaultCount++;
				pageFrameList[(elementCount++ % pageFrameCount)] = pageNumber;
				lastInserted = pageNumber;
			}
			else if (pageNumber != lastInserted) 
				/**
				 * We only update the page table if the page being
				 * referenced was not the last one referenced.
				 * If it was the last page referenced, leave the
				 * page table as is.
				 */
				updatePageTable(searchVal);
		}

		/**
		 * @param int searchVal - the index to be updated
		 */
		void updatePageTable(int searchVal) {
			// first save the next page to be replaced  
			int savedPage = pageFrameList[elementCount % pageFrameList.length];

			int insertedPage = pageFrameList[searchVal];
	
			// if the page to be updated is the next page to be modified
			// just increment elementCount and return
			if (savedPage == insertedPage) {
				elementCount++;
				return;
			}

			// now copy the page just referenced to this position
			//pageFrameList[elementCount % pageFrameList.length] = pageFrameList[searchVal];
			if (System.getProperty("debug") != null) 
				System.out.println("sp = " + savedPage + " ec = " + elementCount + " sv = " + searchVal);

			// now shift all elements
			int rightIndex = searchVal;
			int leftIndex = (rightIndex==0)?pageFrameList.length-1:searchVal-1;
			
			while (rightIndex != (elementCount % pageFrameList.length) ) {
				pageFrameList[rightIndex] = pageFrameList[leftIndex];
				rightIndex = leftIndex;

				leftIndex = (leftIndex==0)?pageFrameList.length-1:leftIndex-1;
			}
			pageFrameList[rightIndex] = insertedPage;
			elementCount++;
		}

		void dump() {
			for (int i = 0; i < pageFrameList.length; i++)
				System.out.print("["+i+"]"+pageFrameList[i]+", ");
			System.out.print(" element count = " + elementCount);
		}


		/**
		 * Searches for page pageNumber in the page frame list
		 * @return non-negative integer if pageNumber was found
		 * @return -1 if pageNumber was not found
		 */
		int search(int pageNumber) {
			int returnVal = -1;

			for (int i = 0; i < pageFrameList.length; i++) {
				if (pageNumber == pageFrameList[i]) {
					returnVal = i;
					break;
				}
			}
			return returnVal;
		}


	}
}
