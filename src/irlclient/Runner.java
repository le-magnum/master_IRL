package irlclient;

import java.io.IOException;

public class Runner {

    private Reader reader = new Reader("");

    private Trajectory[] trajectories;

    private Bash bash = new Bash();

    private Estimator estimator = new Estimator();

    private Modifier modifier = new Modifier();

    private Rewards globalRewardWeights;

    private int runCounter = 0;

    public void runEstimation() throws IOException {
        reader.setPath("/Users/magnus/Coding/master/data/trajectories.txt");
        trajectories = reader.extractTrajectoriesInformation();
        if (runCounter > 0) {

        }

        Rewards rewards = estimator.maximumLikelihoodEstimation(trajectories);
        for (int i = 0; i < rewards.weights.length; i++) {
            System.err.println(rewards.weights[i]);
        }
    }

    public void getLogLikelihoodForTestTrajectories() throws IOException {
        reader.setPath("/Users/magnus/Coding/master/data/testTrajectories.txt");
        trajectories = reader.extractTrajectoriesInformation();
        double logLikelihood = estimator.getLogLikelihoodForTestTrajectories(trajectories);
        System.err.println(logLikelihood);
    }

    public void runBashCmd() {
        try {
            bash.executeCommands();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void runChoiceLevel() throws IOException {
        reader.setPath("/Users/magnus/Coding/master/levels/irltest/choice.lvl");
        State choiceState = reader.readChoiceLevel();
        int[] arr = new int[3];
        modifier.modifyChoiceLevel(arr, choiceState);
        Writer writer = new Writer("/Users/magnus/Coding/master/levels/irltest/choice.lvl");
        writer.addFullState(choiceState);
        writer.closeAndWrite();

    }

}
