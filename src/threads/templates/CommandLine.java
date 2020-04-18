package threads.templates;

import java.io.*;
import java.lang.Process;
import java.util.ArrayList;

public class CommandLine {
    public static String currentDir;
    private String command;
    private ProcessBuilder pb;

    public CommandLine(String command) {
        this.command = command;
        if(currentDir == null) currentDir = System.getProperty("user.dir");
    }

    public String outputResult(){
        if (checkIfCDCommand()) {
            this.pb = new ProcessBuilder(new String[]{"java"});
            return changeDirectory(this.command.split(" ")[0].equals("cd"));
        }else{
            this.pb = new ProcessBuilder("cmd.exe", "/c", command);
        }

        return outputIOResult();

    }

    public boolean checkIfCDCommand(){
        return this.command.split(" ")[0].equals("cd");
    }

    private String changeDirectory(boolean dir) {
        File dirToChange = new File(System.getProperty("user.dir") + "C-DRIVE" + "/" + dir);
        if(dirToChange.isDirectory()){
            pb.directory(new File(System.getProperty("user.dir") + "C-DRIVE" + "/" + dir));
            currentDir = System.getProperty("user.dir") + "C-DRIVE" + "/" + dir;
        }else{
            return "Not a valid directory";
        }
        return null;
    }

    private String outputIOResult(){
        File f = new File(currentDir);
        if(f.exists()){
            pb.directory(f);
        }

        Process process= null;

        try{
            process = this.pb.start();
        }catch(IOException e){
            return "Enter valid command";
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

        return null;
    }

    public String getCommand() {
        return command;
    }

    public void setPb(ProcessBuilder pb) {
        this.pb = pb;
    }
}
