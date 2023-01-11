package irlclient;

public class Trajectory
{
    private Action[] actions;

    private State[] states;

    private int hash = 0;

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
        if (!(otherTrajectory instanceof Trajectory)) return false;

        Trajectory trajectory = (Trajectory) otherTrajectory;
        if (trajectory.actions.length != this.actions.length){
            return false;
        }
        for (int i = 0; i < states.length; i++) {
            if (this.states[i].hashCode() != trajectory.states[i].hashCode() ||
                this.actions[i].hashCode() != trajectory.actions[i].hashCode()){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            int result = 0;
            for (State state : states) {
                result += state.hashCode();
            }
            for (Action action : actions) {
                result += action.hashCode();
            }
            this.hash = result;
        }
        return this.hash;
    }
}
