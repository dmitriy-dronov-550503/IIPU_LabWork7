package sample;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SudoExecutor {

    protected static class StreamGobbler implements Runnable {
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

    private static void prepareScript(String actionName, String command){
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

    private static String execScript(String actionName) throws Exception{
        ProcessBuilder builder = new ProcessBuilder();
        String appDirectory = System.getProperty("user.dir")+"/src/sample";
        builder.command("gksudo","bash", appDirectory+"/"+actionName+".sh");
        builder.directory(new File(appDirectory));
        Process process = builder.start();

        InputStream stdin = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdin);
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();

        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
        return line;
    }

    public static String exec(String scriptName, String command) throws Exception{
        prepareScript(scriptName, command);
        return execScript(scriptName);
    }
}
