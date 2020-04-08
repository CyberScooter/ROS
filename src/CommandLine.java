import java.io.*;
import java.lang.Process;

public class CommandLine {
    static String currentDir;
    private String command;
    private ProcessBuilder pb;

    public CommandLine(String command) {
        this.command = command;
    }

    public String outputResult(){
        pb = new ProcessBuilder();
        if (this.command.split(" ")[0].equals("cd")) {
            return changeDirectory(this.command.split(" ")[0].equals("cd"));
        }

        return outputIOResult();

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

    public String outputIOResult(){
        Process process = null;

        try{
            process = pb.start();
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



}
