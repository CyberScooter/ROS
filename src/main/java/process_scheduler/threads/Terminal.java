package main.java.process_scheduler.threads;

import main.java.Kernel;
import main.java.process_scheduler.threads.templates.CommandLine;
import main.java.process_scheduler.threads.templates.Process;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class Terminal
{
	public static String cdir = null;
	static int processId = 1;
	public static CountDownLatch terminalLatch;
	public static String commandResult;
	
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

			if(commandLine.equals("exit")) break;


			if(cdir == null) cdir = System.getProperty("user.dir");

			terminalLatch = new CountDownLatch(1);
			Kernel.runTerminalProcess(new Process(processId++, Process.Type.commandLine, new CommandLine(commandLine)));

			try{
				terminalLatch.await();
			}catch (InterruptedException e){
				System.out.println(e);
			}

			System.out.println(commandResult);

	 	}
	}

}
