package irlclient;

public class Trajectory
{
    private Action[] actions;

    private State[] states;


    public Trajectory() {}

    public Action[] getActions()
    {
        return this.actions;
    }

    public void setActions(Action[] actions)
    {
        this.actions = actions;
    }

    public State[] getStates()
    {
        return states;
    }

    public void setStates(State[] states)
    {
        this.states = states;
    }

    public void setSize(int size)
    {
        this.actions = new Action[size];
        this.states = new State[size];
    }

    public void setAction(int index, Action action)
    {
        this.actions[index] = action;
    }

    public void setState(int index, State state)
    {
        this.states[index] = state;
    }

    @Override
    public boolean equals(Object otherTrajectory){
        if (this == otherTrajectory) return true;
        if (!(otherTrajectory instanceof StateActionPair)) return false;

        Trajectory trajectory = (Trajectory) otherTrajectory;
        for (int i = 0; i < states.length; i++) {
            if (this.states[i] != trajectory.states[i] ||
                this.actions[i] != trajectory.actions[i]){
                return false;
            }
        }
        return true;
    }


}

