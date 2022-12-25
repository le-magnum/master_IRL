package irlclient;

import java.util.ArrayList;

public interface SuperState
{
    ArrayList<SuperState> getExpandedStates();

    int[] extractFeatures(int[] emptyFeatures);

    boolean isApplicable(int agent, Action action);

}
