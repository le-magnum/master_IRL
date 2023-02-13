package irlclient;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Runner {

    private Reader reader = new Reader("");

    private Trajectory[] trajectories;

    private Bash bash = new Bash();

    private Estimator estimator = new Estimator();

    private Modifier modifier;

    private Rewards globalRewardWeights;

    private HashMap<Integer, Double> comparisonBetweenFeatures;

    private int runCounter = 0;

    public Rewards runEstimation(int firstWeightIndex, int secondWeightIndex, JParser.EstimationParameters parameters) throws IOException {
        reader.setPath("/Users/magnus/Coding/master/data/trajectories.txt");
        trajectories = reader.extractTrajectoriesInformation();

        Rewards rewards = estimator.maximumLikelihoodEstimation(trajectories, parameters);

        if (firstWeightIndex != secondWeightIndex) {
            for (int i = 0; i < rewards.weights.length; i++) {
                if (firstWeightIndex == i || secondWeightIndex == i) {
                    globalRewardWeights.weights[i] += rewards.weights[i];
                }
            }
        }
        runCounter++;
        return rewards;
    }

    private void createRewardWeights(int amountOfFeatures){
        this.globalRewardWeights = new Rewards(amountOfFeatures);
    }

    public void getLogLikelihoodForTestTrajectories(JParser.EstimationParameters parameters) throws IOException {
        reader.setPath("/Users/magnus/Coding/master/data/testTrajectories.txt");
        trajectories = reader.extractTrajectoriesInformation();
        double logLikelihood = estimator.getLogLikelihoodForTestTrajectories(trajectories, parameters);
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

    public void activeLearningMLIRL(JParser.EstimationParameters parameters) throws IOException {
        int i = JParser.getAmountOfFeatures();
        int j = 1;

        createRewardWeights(i);
        comparisonBetweenFeatures = new HashMap<>(i*2);
        modifier = new Modifier(i);
        while (j < i){
            for (int k = j+1; k < i; k++) {
                int[] comparisonIndex = new int[3];
                comparisonIndex[0] = j;
                comparisonIndex[1] = k;
                runChoiceLevel(j,k);
                Rewards localRewards = runEstimation(j,k, parameters);
                double resultBetweenFeatures = Math.abs(localRewards.weights[k]-localRewards.weights[j]);
                if (globalRewardWeights.weights[j] > globalRewardWeights.weights[k]){
                   comparisonIndex[2] = j;
                   comparisonBetweenFeatures.put(Arrays.hashCode(comparisonIndex),resultBetweenFeatures);
                }else {
                    comparisonIndex[2] = k;
                    comparisonBetweenFeatures.put(Arrays.hashCode(comparisonIndex), resultBetweenFeatures);
                }

            }
            j++;
        }
    }

    public Rewards calculateRewardWeightAverage(){
        int divisor = globalRewardWeights.weights.length-2;
        for (int i = 0; i < globalRewardWeights.weights.length; i++) {
            globalRewardWeights.weights[i] = globalRewardWeights.weights[i] / divisor;
        }
        Rewards correctedRewards = new Rewards(globalRewardWeights.weights.length);
        int[] indexWithHighestReward = new int[3];
        for (int i = 1; i < globalRewardWeights.weights.length; i++) {
            for (int j = i+1; j < globalRewardWeights.weights.length; j++) {
                indexWithHighestReward[0] = i;
                indexWithHighestReward[1] = j;
                if (globalRewardWeights.weights[i] > globalRewardWeights.weights[j]){
                    indexWithHighestReward[2] = i;
                }else {
                    indexWithHighestReward[2] = j;
                }
                int hashcode = Arrays.hashCode(indexWithHighestReward);
                if (!comparisonBetweenFeatures.containsKey(hashcode)){
                    int wrongWightIndex;
                    if (indexWithHighestReward[0] == indexWithHighestReward[2]){
                        wrongWightIndex = indexWithHighestReward[0];
                        indexWithHighestReward[2] = indexWithHighestReward[1];
                    }
                    else {
                        wrongWightIndex = indexWithHighestReward[1];
                        indexWithHighestReward[2] = indexWithHighestReward[0];
                    }
                    hashcode = Arrays.hashCode(indexWithHighestReward);
                    correctedRewards.weights[indexWithHighestReward[2]] = globalRewardWeights.weights[wrongWightIndex] + comparisonBetweenFeatures.get(hashcode);
                }
            }
            for (int j = 0; j < globalRewardWeights.weights.length; j++) {
                if (correctedRewards.weights[j] != 0.0){
                    globalRewardWeights.weights[j] = correctedRewards.weights[j];
                }
            }
            System.err.println(globalRewardWeights.weights[i]);

        }
        return globalRewardWeights;
    }

    public void runChoiceLevel(int firstFeatureToTest, int secondFeatureToTest) throws IOException {
        reader.setPath("/Users/magnus/Coding/master/levels/irltest/choice.lvl");
        State choiceState = reader.readChoiceLevel();
        modifier.modifyChoiceLevel(choiceState, firstFeatureToTest, secondFeatureToTest);
        Writer writer = new Writer("/Users/magnus/Coding/master/levels/irltest/choice.lvl");
        writer.addFullState(choiceState);
        writer.closeAndWrite();
        runBashCmd();

    }

}
