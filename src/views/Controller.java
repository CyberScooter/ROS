package views;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import threads.CPU;
import threads.Dispatcher;
import threads.Kernel;
import threads.ProcessCreation;
import threads.templates.Process;
import threads.templates.ReadyQueueComparator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Controller {
    private Kernel kernel;
    public static CountDownLatch fileReading;
    public static CountDownLatch fileWriting;
    public static CountDownLatch fileCompiling;

    private int processID = 1;
    private LinkedList<Process> processesToExecute;
    private HashMap<Integer, String> processBelongingToProgram;
    private static ArrayList<String> archivedResults;
    private int resultCounter;

    public Controller(){
        if(kernel == null){
            kernel = new Kernel(ReadyQueueComparator.queueType.priority);
        }
        processesToExecute = new LinkedList<>();
        processBelongingToProgram = new HashMap<>();
        resultCounter = 0;
        archivedResults = new ArrayList<>();
    }

    @FXML
    private MenuItem program1;

    @FXML
    private MenuItem program2;

    public void initialize() {
        program1.setOnAction(e -> openTextFile("Program1"));
        program2.setOnAction(e -> openTextFile("Program2"));
    }

    protected void openTextFile(String fileName){
        Stage stage = new Stage();
        VBox root = new VBox();
        HBox toolbar = new HBox();
        Scene scene = new Scene(root, 900, 700);

        TextArea fileData = new TextArea();
        TextArea output = new TextArea();
        output.setEditable(false);
        output.setText("Output for results shown here");
        fileData.setMinHeight(400);

        TextField setPriority = new TextField();
        setPriority.setPromptText("Set priority for process");

        Button addToProcessQueue = new Button("Add to process queue");
        Button save = new Button("Save file");
        Button runProcessQueue = new Button("Run processes in queue");
        Button clear = new Button("Clear queue");

        toolbar.getChildren().addAll(save, setPriority, addToProcessQueue, runProcessQueue, clear);
        toolbar.setPadding(new Insets(10, 10, 10, 10));
        toolbar.setSpacing(10);
        root.getChildren().addAll(toolbar, fileData, output);
        root.setPadding(new Insets(10,10,10,10));
        root.setSpacing(10);


        save.setOnAction(f -> {
            fileWriting = new CountDownLatch(1);
            Process process = new Process(2, Process.Type.fileWriting, new File(fileName+".txt"), fileData.getText());
            processesToExecute.add(process);
            Kernel.compileCodeFileProcess(processesToExecute);
            try{
                fileWriting.await();
            }catch (InterruptedException e){
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
            Process process = null;
            for(Process processes : processesToExecute){
                if(processes.getFile().equals(new File(fileName + ".txt"))) found = true;
            }
            if(!found) {
                int id = this.getProcessID();
                if(this.kernel.getQueueType() == ReadyQueueComparator.queueType.FCFS_process){
                    process = new Process(id, 1, Process.Type.fileCompiling, new File(fileName+".txt"));
                }else if(this.kernel.getQueueType() == ReadyQueueComparator.queueType.priority){
                    process = new Process(id, Integer.parseInt(setPriority.getText()), Process.Type.fileCompiling, new File(fileName+".txt"));
                }
                processesToExecute.add(process);
                processBelongingToProgram.put(id, fileName);

                StringBuffer processes = new StringBuffer("Processes in queue:").append("\n");
                for(Process process1 : processesToExecute){
                    processes.append(process1).append("\n");
                }

                output.setText(processes.toString());
            }else{
                output.setText("PROCESS ALREADY IN QUEUE");
            }

            runProcessQueue.setOnAction(e -> {
                if(!processesToExecute.isEmpty()){
                    fileCompiling = new CountDownLatch(processesToExecute.size());
                    Kernel.compileCodeFileProcess(processesToExecute);
                    try{
                        fileCompiling.await();
                    }catch (InterruptedException f){
                        System.out.println(f);
                    }

                    StringBuffer stringBuffer = new StringBuffer();
                    for (Map.Entry<Integer, String> entry : processBelongingToProgram.entrySet()) {
                        for(String item : CPU.cpuResultsCompiled.get(entry.getKey())) {
                            stringBuffer.append(entry.getValue() + " RESULT : ").append(item).append("\n");
                        }
                    }
                    archivedResults.add(stringBuffer.toString());
                    output.setText(stringBuffer.toString());
                    CPU.cpuResultsCompiled.clear();

                    processesToExecute.clear();


                }else{
                    output.setText("NO PROCESSES IN QUEUE");
                }

            });



        });


        fileReading = new CountDownLatch(1);
        Process process = new Process(1, 99, Process.Type.fileReading, new File(fileName + ".txt"));
        processesToExecute.add(process);
        Kernel.compileCodeFileProcess(processesToExecute);
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


    protected void compile(ActionEvent e) throws IOException {
        Stage stage = new Stage();
        VBox root = new VBox();
        Scene scene = new Scene(root, 600, 600);


        Button button = new Button();


//        Process process = new Process(1, 2, Process.Type.fileHandling, new File("Program1.txt"));
//        Process process2 = new Process(2, 3, Process.Type.fileHandling, new File("Program2.txt"));
//
//        Kernel.compileCodeFileProcess(process, process2);

        stage.setTitle("Terminal");
        stage.setScene(scene);
        stage.show();

    }

    protected Integer getProcessID(){
        return processID++;
    }
}
