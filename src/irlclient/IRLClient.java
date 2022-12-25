package irlclient;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;


class IRLClient {
    static BufferedReader serverMessages;
    static State originalState = null;

    static Generator generator;

    static Reader reader;

    static Trajectory[] trajectories;


    public static void main(String[] args)
    throws IOException
    {

        System.out.println("I am the IRL client");


        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("-g")) {
                 System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), false,
                                                     StandardCharsets.US_ASCII));
        serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        originalState = Parser.parseLevel(serverMessages);
                generator = new Generator(originalState);
                generator.writeGoalState(originalState);
                generator.generateTrajectories(1);
            }
        }
        else {
            //Action action = Action.valueOf("MoveN");
            //System.err.println(action.name);
            reader = new Reader();
            trajectories = reader.extractTrajectoriesInformation();
            Estimator estimator = new Estimator();
            Rewards rewards = estimator.maximumLikelihoodEstimation(trajectories);
            System.err.println(rewards.weights);
            /*
            Trajectory[] trajectories = new Trajectory[10];
            reader = new Reader();
            reader.read();
             */
        }

        System.out.println(Action.MoveE.name);
        System.out.flush();
    }

}