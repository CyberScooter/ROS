package main.java.mmu.templates;

/**
 * MakeBACKING_STORE.java
 *
 * This program makes the file for the address translation assignment
 *
 * The name of the constructed file is BACKING_STORE.
 *
 * @author Gagne, Galvin, Silberschatz
 * Operating System Concepts with Java - Eighth Edition
 * Copyright John Wiley & Sons - 2010. 
 */

import java.io.*;

public class MakeBACKING_STORE
{

	public MakeBACKING_STORE() throws IOException{
		// the file representing the simulated  disk
		File fileName;
		RandomAccessFile disk = null;

		try {
			fileName = new File("resources/BACKING_STORE");
			disk = new RandomAccessFile(fileName, "rw");

			//size of int is 4 bytes so you divide by four in for loop to get exactly 256*256 bytes
			for (int i = 0; i < 256*256/4; i++) {
				disk.writeInt(i);
			}
		}
		catch (IOException e) {
			System.err.println ("Unable to create the file BACKING_STORE");
			System.exit(1);
		}
		finally {
			disk.close();
		}
	}
}
