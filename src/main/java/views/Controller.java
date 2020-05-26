package main.java.views;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.java.mmu.PageReplacementAlgorithm.FIFO;
import main.java.mmu.PageReplacementAlgorithm.LRU;
import main.java.process_scheduler.threads.CPU;
import main.java.Kernel;
import main.java.process_scheduler.threads.ProcessCreation;
import main.java.process_scheduler.threads.templates.PCB;
import main.java.process_scheduler.threads.templates.ReadyQueueComparator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Controller {
    private Kernel pss;
    private Kernel MMU;
    public static CountDownLatch fileReading;
    public static CountDownLatch fileWriting;
    public static CountDownLatch fileCompiling;

    private LinkedList<PCB> processesToExecute;
    private HashMap<Integer, String> processBelongingToProgram;
    private static ArrayList<String> archivedResults;

    private static int processID = 1;

    public Controller(){
        //BOOTING OS, INITIALISING SUBMODULES IN KERNEL
        //sets up process scheduling simulator with a chosen scheduling algorithm
        if(pss == null) pss = new Kernel(ReadyQueueComparator.queueType.priority);
        //sets up mmu with the chosen physical page count
        if(MMU == null) MMU = new Kernel(new LRU(128));
        processesToExecute = new LinkedList<>();
        processBelongingToProgram = new HashMap<>();
        archivedResults = new ArrayList<>();
    }

    @FXML
    private MenuItem program1;

    @FXML
    private MenuItem program2;

    @FXML
    private MenuItem program3;

    @FXML
    private MenuItem mmuMenuItem;

    public void initialize() {
        program1.setOnAction(e -> openTextFile("Program1"));
        program2.setOnAction(e -> openTextFile("Program2"));
        program3.setOnAction(e -> openTextFile("Program3"));
        mmuMenuItem.setOnAction(e -> openMMU());

    }

    protected void openTextFile(String fileName){
        Stage stage = new Stage();
        VBox root = new VBox();
        HBox toolbar = new HBox();
        HBox schedulingToolbar = new HBox();
        HBox secondToolbar = new HBox();
        Scene scene = new Scene(root, 400, 550);

        TextArea fileData = new TextArea();
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setText("Output for results shown here\nScheduling algorithm set to Priority");
        fileData.setMinHeight(200);

        TextField setPriority = new TextField();
        setPriority.setPromptText("Set priority for process");

        Button addToProcessQueue = new Button("Add to process list");
        Button save = new Button("Save file");
        Button runProcessList = new Button("Run processes in list");
        Button clear = new Button("Clear list");
        Button FIFO = new Button("FIFO");
        Button priorityScheduling = new Button("Priority");

        schedulingToolbar.getChildren().addAll(FIFO, priorityScheduling);
        toolbar.getChildren().addAll(save, setPriority);
        secondToolbar.getChildren().addAll(addToProcessQueue, runProcessList, clear);
        secondToolbar.setPadding(new Insets(0, 10, 10, 10));
        secondToolbar.setSpacing(10);
        toolbar.setPadding(new Insets(10, 10, 10, 10));
        toolbar.setSpacing(10);
        schedulingToolbar.setPadding(new Insets(10, 10, 0, 10));
        schedulingToolbar.setSpacing(10);
        root.getChildren().addAll(schedulingToolbar, toolbar, secondToolbar, fileData, output);
        root.setPadding(new Insets(10,10,10,10));
        root.setSpacing(10);

        FIFO.setOnAction(e -> {
            processesToExecute.clear();
            pss.setQueueType(ReadyQueueComparator.queueType.FCFS_process);
            output.setText("Scheduling algorithm set to FIFO\nList is cleared!");
        });

        priorityScheduling.setOnAction(e -> {
            processesToExecute.clear();
            pss.setQueueType(ReadyQueueComparator.queueType.priority);
            output.setText("Scheduling algorithm set to Priority\nList is cleared!");
        });

        save.setOnAction(f -> {
            fileWriting = new CountDownLatch(1);
            PCB pcb = new PCB(getProcessID(), PCB.Type.fileWriting, new File(fileName + ".txt"), fileData.getText());
            processesToExecute.add(pcb);
            Kernel.runCodeFileProcesses(processesToExecute);
            try {
                fileWriting.await();
            } catch (InterruptedException e) {
                output.setText(e.getMessage());
            }
            output.setText("File saved");
        });

        clear.setOnAction(e -> {
            processesToExecute.clear();
            output.clear();
        });

        addToProcessQueue.setOnAction(g -> {
            output.clear();
            boolean found = false;
            PCB pcb = null;
            for (PCB processes : processesToExecute) {
                if (processes.getFile().equals(new File(fileName + ".txt"))) found = true;
            }
            if (!found) {
                int id =getProcessID();
                if (this.pss.getQueueType() == ReadyQueueComparator.queueType.FCFS_process) {
                    pcb = new PCB(id, 1, PCB.Type.fileCompiling, new File(fileName + ".txt"));
                } else if (this.pss.getQueueType() == ReadyQueueComparator.queueType.priority) {
                    pcb = new PCB(id, Integer.parseInt(setPriority.getText()), PCB.Type.fileCompiling, new File(fileName + ".txt"));
                }
                processesToExecute.add(pcb);
                processBelongingToProgram.put(id, fileName);

                StringBuffer processes = new StringBuffer("Processes in list:").append("\n");
                for (PCB PCB1 : processesToExecute) {
                    processes.append(PCB1).append("\n");
                }

                output.setText(processes.toString());
            } else {
                output.setText("PROCESS ALREADY IN list");
            }

            runProcessList.setOnAction(e -> {
                if (!processesToExecute.isEmpty()) {
                    fileCompiling = new CountDownLatch(processesToExecute.size());
                    Kernel.runCodeFileProcesses(processesToExecute);
                    try {
                        fileCompiling.await();
                    } catch (InterruptedException f) {
                        System.out.println(f);
                    }

                    StringBuffer stringBuffer = new StringBuffer();

                    for(Map.Entry<Integer, LinkedList<String>> compiledResult : CPU.cpuResultsCompiled.entrySet()){
                        for(String item : compiledResult.getValue()){
                            stringBuffer.append(processBelongingToProgram.get(compiledResult.getKey()) + " RESULT : ").append(item).append("\n");
                        }
                    }

                    archivedResults.add(stringBuffer.toString());
                    output.setText(stringBuffer.toString());
                    CPU.cpuResultsCompiled.clear();

                    processesToExecute.clear();


                } else {
                    output.setText("NO PROCESSES IN LIST");
                }

            });
        });

        fileReading = new CountDownLatch(1);
        int id = getProcessID();
        PCB pcb = new PCB(id, 99, PCB.Type.fileReading, new File(fileName + ".txt"));
        processesToExecute.add(pcb);
        Kernel.runCodeFileProcesses(processesToExecute);
        try {
            fileReading.await();
        }catch (InterruptedException e){
            System.out.println(e);
        }finally {
            fileData.setText(CPU.getTextFileOutput().get(fileName+ ".txt"));
        }

        stage.setTitle(fileName);
        stage.setScene(scene);
        stage.show();
    }

    public void openMMU(){
        Stage stage = new Stage();
        VBox root = new VBox();
        VBox textVBox = new VBox();
        HBox firstRow = new HBox();
        HBox secondRow = new HBox();
        HBox thirdRow = new HBox();
        Scene scene = new Scene(root, 560, 525);

        Text text = new Text("Using RandomAccessFile populated with bytes as hard disk");
        Text text2 = new Text("Runs a simulation of paging with " + MMU.getMmu().getNumberOfFramesInMemory() + " frames in main memory using LRU");
        TextField setFramesInMemory = new TextField();
        setFramesInMemory.setMinWidth(200);
        setFramesInMemory.setPromptText("Enter number of frames in memory");
        TextField filename = new TextField();
        filename.setMinWidth(200);
        filename.setPromptText("Enter virtual addresses file name");
        TextField virtualAddress = new TextField();
        virtualAddress.setMinWidth(200);
        virtualAddress.setPromptText("Enter virtual address to add");
        Button addToMemoryFileInput = new Button("Add to memory");
        Button addToMemoryTextInput = new Button("Add to memory");
        Button initialiseLru = new Button("Initialise LRU");
        Button initialiseFifo = new Button("Initialise FIFO");
        TextArea output = new TextArea();
        TextArea realTimeStatistics = new TextArea();
        realTimeStatistics.setEditable(false);
        realTimeStatistics.setText("Real time statistics of results");
        output.setEditable(false);
        output.setText("Real time results");
        output.setMinHeight(200);

        textVBox.getChildren().addAll(text, text2);
        firstRow.getChildren().addAll(setFramesInMemory, initialiseLru,initialiseFifo);
        secondRow.getChildren().addAll(filename, addToMemoryFileInput);
        thirdRow.getChildren().addAll(virtualAddress, addToMemoryTextInput);
        root.getChildren().addAll(textVBox, firstRow, secondRow, thirdRow, output, realTimeStatistics);

        textVBox.setSpacing(10);
        root.setPadding(new Insets(10,10,10,10));
        firstRow.setSpacing(10);
        firstRow.setPadding(new Insets(0,10,10,10));
        secondRow.setSpacing(10);
        secondRow.setPadding(new Insets(0,10,10,10));
        thirdRow.setSpacing(10);
        thirdRow.setPadding(new Insets(0,10,10,10));
        root.setSpacing(10);

        initialiseLru.setOnAction(e -> {
            MMU = new Kernel(new LRU(Integer.parseInt(setFramesInMemory.getText())));
            output.setText(setFramesInMemory.getText() + " frames in memory initialised with LRU page replacement algorithm");
            text2.setText("Runs a simulation of paging with " + MMU.getMmu().getNumberOfFramesInMemory() + " frames in main memory using LRU");
            realTimeStatistics.setText(null);
        });

        initialiseFifo.setOnAction(e -> {
            MMU = new Kernel(new FIFO(Integer.parseInt(setFramesInMemory.getText())));
            output.setText(setFramesInMemory.getText() + " frames in memory initialised with FIFO page replacement algorithm");
            text2.setText("Runs a simulation of paging with " + MMU.getMmu().getNumberOfFramesInMemory() + " frames in main memory using FIFO");
            realTimeStatistics.setText(null);
        });

        addToMemoryFileInput.setOnAction(e -> {
            try {
                if(MMU.getMmu().allocateMemory(new File(filename.getText()))){
                    output.setText(MMU.getMmu().getResults());
                    realTimeStatistics.setText(MMU.getMmu().getStatistics());
                    MMU.getMmu().clearResults();
                }
            }catch (IOException f){
                System.out.println(f);
            }
        });

        addToMemoryTextInput.setOnAction(e -> {
            try {
                MMU.getMmu().allocateMemory(Integer.parseInt(virtualAddress.getText()));
                output.setText(MMU.getMmu().getResults());
                realTimeStatistics.setText(MMU.getMmu().getStatistics());
                MMU.getMmu().clearResults();
            }catch (IOException f){
                System.out.println(f);
            }
        });




        stage.setTitle("MMU");
        stage.setScene(scene);
        stage.show();

    }

    public static Integer getProcessID(){
        return processID++;
    }

    public static void closeConfirmation(Stage stage){
        stage.setOnCloseRequest((WindowEvent we) ->
        {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Confirmation");
            a.setHeaderText("Do you want to power off system?");
            Optional<ButtonType> closeResponse = a.showAndWait();
            if (stage.getTitle().equals("ROS") && closeResponse.get() == ButtonType.OK){
                Platform.exit();
                System.exit(0);
            }
            else{
                we.consume();
            }
        });
    }
}
