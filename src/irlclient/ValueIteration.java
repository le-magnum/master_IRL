package irlclient;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class ValueIteration
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    private HashMap<State,Float> valueMap = new HashMap<State, Float>();

    private Rewards rewards;

    private Transitions transitions = new Transitions();

    public void add(State state)
    {
        this.queue.addLast(state);
        this.set.add(state);
    }


    public State pop()
    {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }


    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    public int size()
    {
        return this.set.size();
    }

    public void fillListOfStates(State initialState)
    {
        HashSet<State> statesExplored = new HashSet<>();
        this.add(initialState);
        State lastState = null;
        while (true) {

            State currentState = this.pop();
            statesExplored.add(currentState);
            for (SuperState state : currentState.getExpandedStates()) {
                State state2 = (State)state;
                if (!(contains(state2)) && !(statesExplored.contains(state2))) {
                    this.add(state2);
                }
            }
            if (isEmpty())
                break;
        }
        this.set.addAll(statesExplored);

    }
    private void setupValueFunction(int amountOfFeatures){
        for (State state : set){
            if (state.isGoalStateWithoutBoxes()) {
                float value = (float) rewards.calculateRewards(state.extractFeatures(new int[amountOfFeatures]));
                valueMap.put(state,value);
            }else {
                valueMap.put(state, (float) 0);
            }
        }
    }


    public HashSet<State> getSet()
    {
        return set;
    }

    public void calculateValueIteration(JParser.GenerationParametersObject parameters, boolean isTesting, Rewards testingRewards)
    {

        double gemma = parameters.getGemma();
        double theta = parameters.getTheta();

        int[] stateFeatures;
        int amountOfFeatures = parameters.getRewardWeights().length;
        rewards = new Rewards(amountOfFeatures);
        if (!(isTesting)) {
            rewards.updateWeights(parameters.getRewardWeights());
        }else {
            rewards.updateWeights(testingRewards.weights);
        }

        double lastValue = -100000000;
        setupValueFunction(amountOfFeatures);
        HashMap<State,Float> qMap = new HashMap<State, Float>();
        HashSet<State> hasBeenVisited = new HashSet<>();

        int i = 0;
        while (i < parameters.getIterations() ) {
            double delta = 0;
            for (State state : set) {
                for (Action action : Action.values()) {
                    stateFeatures = state.extractFeatures(new int[amountOfFeatures]);
                    double value = 0;
                    double rewardOfStateAndAction = rewards.calculateRewards(stateFeatures);
                    int t = transitions.transitionFunction(state, action, 0);
                    Map<Integer,Action> jointAction = new HashMap<>();
                    jointAction.put(0,action);
                    State nextState = new State(state,jointAction);
                    if (t == 0 && !(state.isGoalStateWithoutBoxes())) {
                        continue;
                    }
                    /*if (hasBeenVisited.contains(state)){
                        rewardOfStateAndAction = rewards.weights[0];
                    }
                     */
                    if (state.isGoalStateWithoutBoxes()){
                        value = rewards.calculateRewards(stateFeatures);
                    }
                    else {
                        value = t * (rewardOfStateAndAction + gemma * valueMap.get(nextState));
                    }
                    /*
                    if (i < 3) {
                        for (int feature: stateFeatures) {
                            System.err.print(feature);
                        }
                    System.err.println("\n" + action.name);
                    System.err.println("t = " + t + " reward = " + rewardOfStateAndAction + " valueofnextstate = "
                    + valueMap.get(nextState));
                    System.err.println("and this is the calculated value: " + value);
                    }
                     */
                    lastValue = max(value, lastValue);
                    /*
                    if (i < 3) {
                        System.err.println("and this value will be put in the list: " + lastValue);
                        System.err.println("these calculations was for this state\n" + state);
                    }

                     */
                    qMap.put(state,(float) lastValue);

                }
                /*
                if (state.isGoalStateWithoutBoxes()){
                    nextValueMap.put(state,(float)rewardWeights[2]);
                    System.err.println("for goal state : " + "\n" + state);
                }
                else {
                    nextValueMap.put(state, (float) lastValue);
                }
                 */

                //System.err.println("float value: " + valueMap.get(state));
                //System.err.println("for state : " + "\n" + state);
                delta = max(delta, abs(qMap.get(state) - valueMap.get(state)));
               // System.err.println("Theta is " + theta + " delta is " + delta);
                //System.err.println("---------------------------next state----------------------");
                lastValue = -1000000000;
                hasBeenVisited.add(state);
            }
            valueMap.putAll(qMap);
            i++;
            if (delta < theta){
                //System.err.println(i);
                break;
            }
         //   System.err.println("---------------------------next iteration " + i + "----------------------");
        }
    }
    public Action extractPolicy(State s){
        float actionValue = -1000000000;
        Action bestAction = null;
        for (Action action: Action.values()) {
            if (s.isApplicable(0,action)){
                Map<Integer,Action> jointAction = new HashMap<>();
                jointAction.put(0,action);
                State nextState = new State(s,jointAction);
                if (valueMap.get(nextState) >=  actionValue){
                    bestAction = action;
                    actionValue = valueMap.get(nextState);
                }
            }
        }
        return bestAction;
    }

    public void clearSet(){
        this.set.clear();
    }

    public double RewardOfState(State state, int AmountOfFeatures){
        int[] stateFeatures = state.extractFeatures(new int[AmountOfFeatures]);
        double reward = this.rewards.calculateRewards(stateFeatures);
        return reward;
    }

    public void setGenerationRewards(JParser.GenerationParametersObject parameters){
        this.rewards.weights = parameters.getRewardWeights();
    }
}
