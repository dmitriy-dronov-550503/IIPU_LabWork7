package sample;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DeviceManager {

    private String slot, path;
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

    DeviceManager(String slot, String path){
        this.slot = slot;
        this.path = path;
    }

    private void prepareScript(String actionName){
        String appDirectory = System.getProperty("user.dir")+"/src/sample";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(appDirectory+"/"+actionName+".sh"));
            writer.write("wodim -eject -tao  speed=2 dev=/dev/sr1 -v -data diskImage.iso");
            writer.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void execScript(String actionName) throws Exception{
        prepareScript(actionName);
        ProcessBuilder builder = new ProcessBuilder();
        String appDirectory = System.getProperty("user.dir")+"/src/sample";
        builder.command("gksudo","bash", appDirectory+"/"+actionName+".sh");
        builder.directory(new File(appDirectory));
        Process process = builder.start();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
    }

    public void write() throws Exception{
        execScript("write");
    }
}
