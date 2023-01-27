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

    private Rewards rewards = new Rewards();

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
        return this.queue.size();
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
    public void setupValueFunction(){
        for (State state : set){
            if (state.isGoalStateWithoutBoxes()) {
                float value = (float) rewards.calculateRewards(state.extractFeatures(new int[3]));
                valueMap.put(state,value);
            }
            valueMap.put(state,(float)0);
        }
    }


    public HashSet<State> getSet()
    {
        return set;
    }

    public void calculateValueIteration()
    {
        double gemma = 0.8;
        double theta = 0.001;

        int[] stateFeatures;
        int amountOfFeatures = rewards.ReadAmountOfFeatures();
        rewards.ReadRewardWeights();

        double lastValue = -10000;
        setupValueFunction();
        HashMap<State,Float> qMap = new HashMap<State, Float>();
        HashSet<State> hasBeenVisited = new HashSet<>();

        int i = 0;
        while (i < 10000 ) {
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
                    }/*
                    if (hasBeenVisited.contains(state)){
                        rewardOfStateAndAction = rewardWeights[0];
                    }
                    if (stateFeatures[1] == 1){
                        rewardOfStateAndAction = rewardWeights[1];
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
                lastValue = -10000;
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
        float actionValue = -10000;
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
}
