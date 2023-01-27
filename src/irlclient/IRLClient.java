package irlclient;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;


class IRLClient {
    static BufferedReader serverMessages;
    static State originalState = null;
    static Generator generator;
    static Runner runner;

    public static void main(String[] args)
            throws IOException {

        System.out.println("IRL client");

        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("-g")) {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), false,
                        StandardCharsets.US_ASCII));
                serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
                originalState = Parser.parseLevel(serverMessages);
                generator = new Generator(originalState);
                System.err.println(originalState);
                generator.writeGoalState(originalState);
                generator.generateTrajectories((Integer.parseInt(args[2])));
                System.out.println(Action.MoveE.name);
                System.out.flush();
            }
        } else {
            runner = new Runner();
            //runner.getLogLikelihoodForTestTrajectories();
            runner.runEstimation();
            runner.getLogLikelihoodForTestTrajectories();
            runner.runChoiceLevel();
            runner.runBashCmd();
            runner.runEstimation();
            runner.getLogLikelihoodForTestTrajectories();
            runner.runChoiceLevel();
            runner.runBashCmd();
            runner.runEstimation();
            runner.getLogLikelihoodForTestTrajectories();
        }



    }

}