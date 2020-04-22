package threads.templates;

import javafx.scene.control.Control;
import threads.SimpleShell;
import views.Controller;
import views.Main;

import java.io.*;
import java.lang.Process;
import java.util.ArrayList;

public class CommandLine {
    private String command;
    private ProcessBuilder pb;

    public CommandLine(String command) {
        this.command = command;
    }

    public String outputResult(){
        if (checkIfCDCommand()) {
            this.pb = new ProcessBuilder("cmd.exe", "/c");
            changeDirectory(command.split(" ")[1]);
            return null;
        }else{
            this.pb = new ProcessBuilder("cmd.exe", "/c", command);
            return outputIOResult();
        }
    }

    public boolean checkIfCDCommand(){
        return this.command.split(" ")[0].equals("cd");
    }

    public void changeDirectory(String dir){
        String currdir = null;
        if(SimpleShell.cdir == null){
            currdir = System.getProperty("user.dir") + "/" + dir;
        }else{
            currdir = SimpleShell.cdir + "/" + dir;
        }
        if(new File(currdir).isDirectory()){
            pb.directory(new File(currdir));
            SimpleShell.cdir = currdir;
        }else {
            System.out.println("Not a valid dir");
        }
    }

    private String outputIOResult(){
        File f = new File(SimpleShell.cdir);
        if(f.exists()){
            pb.directory(f);
        }

        Process process= null;

        try{
            process = this.pb.start();
        }catch(IOException e){
            return "Enter a valid command";
        }
        StringBuffer stringBuffer = new StringBuffer();

        //response for command entered
        try {
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);


            String line;
            while ((line = br.readLine()) != null)
                stringBuffer.append(line).append("\n");

            br.close();
            return stringBuffer.toString();
        }catch (IOException e){
            stringBuffer.append(e);
        }

        return stringBuffer.toString();
    }

    public String getCommand() {
        return command;
    }

    public void setPb(ProcessBuilder pb) {
        this.pb = pb;
    }
}
