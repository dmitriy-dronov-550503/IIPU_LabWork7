package sample;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.control.ProgressIndicator;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DiskWriteControl {

    private double time;
    private DoubleProperty progress = new SimpleDoubleProperty();
    private double simpleProgress = 0;

    DiskWriteControl(double time){
        this.time = time;
    }

    public DoubleProperty getProgress() {
        return progress;
    }

    private void runProgressTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                simpleProgress+=0.01;
                progress.set(simpleProgress);
            }

        };
        long period = (new Double((time / 100) * 1000)).longValue();
        timer.schedule(task, period, period);
    }

    private void prepareScript(String actionName, String command){
        String appDirectory = System.getProperty("user.dir")+"/src/sample";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(appDirectory+"/"+actionName+".sh"));
            writer.write(command);
            writer.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String execScript(String actionName) throws Exception{
        ProcessBuilder builder = new ProcessBuilder();
        String appDirectory = System.getProperty("user.dir")+"/src/sample";
        builder.command("gksudo","bash", appDirectory+"/"+actionName+".sh");
        builder.directory(new File(appDirectory));
        Process process = builder.start();

        InputStream stdin = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdin);
        BufferedReader br = new BufferedReader(isr);
        String line="";
        while((line = br.readLine()) != null){
            if(line.isEmpty()){
                runProgressTimer();
            }
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        assert exitCode == 0;
        return line;
    }

    public String exec(String scriptName, String command) throws Exception{
        prepareScript(scriptName, command);
        return execScript(scriptName);
    }
}
