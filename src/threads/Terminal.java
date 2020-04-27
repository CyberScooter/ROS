package threads;

import threads.CPU;
import threads.templates.CommandLine;
import threads.templates.Process;
import threads.templates.ReadyQueueComparator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class Terminal
{
	public static String cdir = null;
	static int processId = 1;
	public static CountDownLatch terminalLatch;
	
	public static void main(String[] args) throws java.io.IOException {
		String commandLine;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		Kernel kernel = new Kernel(ReadyQueueComparator.queueType.FCFS_process);;
		// we break out with <control><C>
		while (true) {
			// read what they entered
			System.out.print("jsh>");
			commandLine = console.readLine();

			// if they entered a return, just loop again
			if (commandLine.equals("")) 
				continue;


			if(cdir == null) cdir = System.getProperty("user.dir");

			terminalLatch = new CountDownLatch(1);
			Kernel.runTerminalProcess(new Process(processId++, Process.Type.commandLine, new CommandLine(commandLine)));

			try{
				terminalLatch.await();
			}catch (InterruptedException e){
				System.out.println(e);
			}

	 	}
	}

}
