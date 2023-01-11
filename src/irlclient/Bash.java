package irlclient;

import java.io.*;

public class Bash {

    public void executeCommands() throws IOException, InterruptedException {

        File tempScript = createTempScript();

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } finally {
            tempScript.delete();
        }
    }

    public File createTempScript() throws IOException {
        File tempScript = File.createTempFile("script", null);

        OutputStreamWriter streamWriter = new OutputStreamWriter(new FileOutputStream(
                tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#!/bin/bash");
        printWriter.println("cd src");
        printWriter.println("java -jar ../out/server.jar -c \"java irlclient.IRLClient -g -n 1\" -l \"../levels/irltest/choice.lvl\" ");

        printWriter.close();

        return tempScript;
    }
}
