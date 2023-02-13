package irlclient;

import java.util.HashMap;
import java.util.Map;

public class Tester {
    private ValueIteration vl = new ValueIteration();

    private HashMap<State, Double> accumulatedRewards;

    private HashMap<State, String> wrongSolutionsForEstimatedPolicy;

    private State startingState ;


    public void testIfRewardFunctionsAreTheSame(State originalState, Rewards rewardsToTest, JParser.GenerationParametersObject parameters) {
        this.startingState = originalState;
        vl.fillListOfStates(originalState);
        vl.calculateValueIteration(parameters, false, null);

        testOptimalPolicy(parameters);

        vl.calculateValueIteration(parameters,true, rewardsToTest);
        vl.setGenerationRewards(parameters);
        calculateDifferenceBetweenPolicies(parameters.getRewardWeights().length);

    }

    private void testOptimalPolicy(JParser.GenerationParametersObject parameters){
        int amountOfStates = vl.size();
        accumulatedRewards = new HashMap<>(amountOfStates);
        wrongSolutionsForEstimatedPolicy = new HashMap<>(amountOfStates);

        int amountOfFeatures = parameters.getRewardWeights().length;

        for (State state: vl.getSet()) {
            double accumulatedRewardForSolution = runPolicyOnStateAndGetReward(state,amountOfFeatures);
            accumulatedRewards.put(state,accumulatedRewardForSolution);
        }
    }

    public String returnWrongSolution(State stateWithSolution){
        String solution = "";
        if (this.wrongSolutionsForEstimatedPolicy.get(stateWithSolution).equals(null)){
            System.err.println("this state has the same solution as the optimal one");
        }else {
            solution = wrongSolutionsForEstimatedPolicy.get(stateWithSolution);
        }
        return solution;
    }

    private void routeInWrongSolution(State stateWithNonOptimalSolution) {
        State[] states = new State[50];
        String solution = "";
        int stepsInSolution = 0;
        while ((!stateWithNonOptimalSolution.isGoalStateWithoutBoxes() && stepsInSolution < states.length - 1)) {
            if (stateWithNonOptimalSolution.noGoalState() && stepsInSolution > 1) {
                break;
            }
            Action bestAction = vl.extractPolicy(stateWithNonOptimalSolution);
            Map<Integer, Action> jointAction = new HashMap<>();
            solution += bestAction.name + "\n";
            jointAction.put(0, bestAction);
            states[stepsInSolution] = stateWithNonOptimalSolution;
            // System.err.println("1. this is the state that the coming move is made on\n" + originalState);
            //System.err.println("1.5 this is the best action: " + bestAction.name);
            stateWithNonOptimalSolution = new State(stateWithNonOptimalSolution, jointAction);
            //System.err.println("2. Solution iteration: " + i);
            // System.err.println("3. and this is the move this" + bestAction.name);
            stepsInSolution++;
        }
        this.wrongSolutionsForEstimatedPolicy.put(states[0],solution);
    }
    private void calculateDifferenceBetweenPolicies(int amountOfFeatures){
        boolean isEstimatedPolicyOptimal = true;
        int amountOfEqualSolutions = 0;
        for (State state: vl.getSet()) {
            double accumulatedRewardForSolution = runPolicyOnStateAndGetReward(state,amountOfFeatures);
            double accumulatedRewardForOptimalSolution = accumulatedRewards.get(state);

            if (accumulatedRewardForOptimalSolution == accumulatedRewardForSolution){
                amountOfEqualSolutions++;
            }
            else {
                isEstimatedPolicyOptimal = false;
                routeInWrongSolution(state);
            }
        }
        System.err.println("is the estimated Solution optimal? Answer: " + isEstimatedPolicyOptimal);
        double OverallEqualness = ((double) amountOfEqualSolutions / (double) vl.size()) * 100;
        System.err.println("How much of the experts behaviour has been replicated? Answer: " + OverallEqualness + "%");
    }

    private double runPolicyOnStateAndGetReward(State stateToTest, int amountOfFeatures) {
        State[] states = new State[50];
        int stepsInSolution = 0;
        int stateNumber = 0;
        double accumulatedRewardForSolution = 0;
        while ((!stateToTest.isGoalStateWithoutBoxes() && stepsInSolution < states.length - 1)) {
            if (stateToTest.noGoalState() && stepsInSolution > 1) {
                break;
            }
            accumulatedRewardForSolution += vl.RewardOfState(stateToTest, amountOfFeatures);
            Action bestAction = vl.extractPolicy(stateToTest);
            Map<Integer, Action> jointAction = new HashMap<>();
            jointAction.put(0, bestAction);
            states[stepsInSolution] = stateToTest;
            // System.err.println("1. this is the state that the coming move is made on\n" + originalState);
            //System.err.println("1.5 this is the best action: " + bestAction.name);
            stateToTest = new State(stateToTest, jointAction);
            //System.err.println("2. Solution iteration: " + i);
            // System.err.println("3. and this is the move this" + bestAction.name);
            stepsInSolution++;
        }
        return accumulatedRewardForSolution;
    }
}
