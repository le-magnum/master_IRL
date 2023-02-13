package irlclient;


import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;


class IRLClient {
    static BufferedReader serverMessages;
    static State originalState = null;
    static Generator generator;
    static Runner runner;

    static Rewards rewards;

    static Tester tester;


    public static void main(String[] args)
            throws IOException, ParseException {

        System.out.println("IRL client");
        JParser.SetUpJParser();
        boolean nestedRun = false;
        if (args.length != 0){
            nestedRun = true;
        }
        String solution = "";

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), false,
                StandardCharsets.US_ASCII));
        serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        originalState = Parser.parseLevel(serverMessages);

        if (JParser.runGeneration() || nestedRun) {
            generator = new Generator(originalState);
            System.err.println(originalState);
            JParser.GenerationParametersObject generationDAO = JParser.fetchGenerationParametersObject();
            generator.writeGoalState(originalState);
            if (nestedRun){
                System.err.println("running in nested mode");
                generator.generateTrajectories(generationDAO, 1);
            }else {
                generator.generateTrajectories(generationDAO, generationDAO.getAmountOfTrajectories());
            }
            System.out.println(Action.MoveE.name);
            System.out.flush();
        } else if (JParser.runActiveLearning()) {
            runner = new Runner();
            JParser.EstimationParameters estimationDAO = JParser.fetchEstimationParameters();
            runner.activeLearningMLIRL(estimationDAO);
            rewards = runner.calculateRewardWeightAverage();
        }
        else {
            runner = new Runner();
            JParser.EstimationParameters estimationDAO = JParser.fetchEstimationParameters();
            rewards = runner.runEstimation(0,0, estimationDAO);
        }

        if (JParser.runTesting() && (JParser.runActiveLearning() || (!(JParser.runGeneration() && !(JParser.runActiveLearning()))))){
            if (originalState.equals(null)){
                Reader reader = new Reader("/Users/magnus/Coding/master/levels/irltest/example1.lvl");
                originalState = reader.readChoiceLevel();
            }
            tester = new Tester();
            tester.testIfRewardFunctionsAreTheSame(originalState,rewards,JParser.fetchGenerationParametersObject());
            solution = tester.returnWrongSolution(originalState);

        } else {
            System.err.println("you cannot run testing without running estimation also. Set \"runGeneration: false \" or \"activeLearning : true \" ");
        }

        System.out.println(solution);
        System.out.flush();



    }

}