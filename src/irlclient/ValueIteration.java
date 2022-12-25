package irlclient;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        int[] rewardWeights = new int[3];
        int[] stateFeatures;
        rewardWeights[0] = 0;
        rewardWeights[1] = -1;
        rewardWeights[2] = 1;
        double lastValue = -10000;
        rewards = new Rewards(rewardWeights);
        setupValueFunction();
        HashMap<State,Float> nextValueMap = new HashMap<State, Float>();
        int i = 0;
        while (i < 100 ) {
            double delta = 0;
            for (State state : set) {
                for (Action action : Action.values()) {
                    stateFeatures = state.extractFeatures(new int[3]);
                    int rewardOfStateAndAction = rewards.calculateRewards(stateFeatures);
                    int t = transitions.transitionFunction(state, action, 0);
                    if (t == 0) {
                        continue;
                    }
                    Map<Integer,Action> jointAction = new HashMap<>();
                    jointAction.put(0,action);
                    State nextState = new State(state,jointAction);
                    double value = t * (rewardOfStateAndAction + gemma * valueMap.get(nextState));
                    System.err.println("t = "+ t  + " reward = " +rewardOfStateAndAction +  " valueofnextstate = "
                     + valueMap.get(nextState));
                    System.err.println("and this is the calculated value: " + value);
                    lastValue = max(value, lastValue);

                }
                if (state.isGoalStateWithoutBoxes()){
                    nextValueMap.put(state,(float)1.0);
                    System.err.println("for goal state : " + "\n" + state);
                }
                else {
                    nextValueMap.put(state, (float) lastValue);
                }
                System.err.println("float value: " + lastValue);
                System.err.println("for state : " + "\n" + state);
                delta = max(delta,nextValueMap.get(state) - valueMap.get(state));
                System.err.println("Theta is " + theta + " delta is " + delta);
                System.err.println("---------------------------next state----------------------");
                lastValue = -10000;
            }
            i++;
            valueMap.putAll(nextValueMap);
            if (delta < theta){
                break;
            }
            System.err.println("---------------------------next iteration " + i + "----------------------");
        }
    }
    public Action extractPolicy(State s){
        float actionValue = 0;
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
}
