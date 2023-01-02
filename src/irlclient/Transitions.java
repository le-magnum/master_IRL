package irlclient;

import java.util.HashMap;

public class Transitions
{
    public int transitionFunction(SuperState currentState, Action wantedAction,int agentNumber){
        if (currentState.isApplicable(agentNumber,wantedAction)) {
            return 1;
        }
        else
            return 0;
        }

    public State nextState(State currentState, Action wantedAction){
        int numberOfAgents = currentState.agentRows.size();
        HashMap<Integer, Action> jointActions = new HashMap<>();
        for (int i = 0; i < numberOfAgents; i++) {
            if (!(currentState.isApplicable(i,wantedAction))){
                return currentState;
            }
            jointActions.put(i,wantedAction);
        }
        State newState = new State(currentState,jointActions);
        return newState;
    }


    }

