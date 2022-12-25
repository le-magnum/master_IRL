package irlclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class Generator
{
    private State originalState;

    ValueIteration vl = new ValueIteration();

    private Writer writer = new Writer("/Users/magnus/Coding/mavis/data/trajectories.txt");

    private Random random = new Random();

    public Generator(State originalState)
    throws IOException
    {
        this.originalState = originalState;
    }

    public void writeGoalState(State goal)
    {

        writer.addFileHeader(goal);
    }

    public void generateTrajectories(int amountOfTrajectories)
    throws IOException
    {
        vl.fillListOfStates(originalState);

        HashSet<State> setOfStates = vl.getSet();

        for (SuperState state: setOfStates) {
            System.err.println("This is the states found" + "\n" + state.toString() );
        }

        String solution = "";
        vl.calculateValueIteration();
        State[] states = new State[9];
        int i = 0;
        while (i < amountOfTrajectories){
            int stepsInSolution = 0;
            while (!originalState.isGoalStateWithoutBoxes()){
                Action bestAction =vl.extractPolicy(originalState);
                Map<Integer,Action> jointAction = new HashMap<>();
                jointAction.put(0,bestAction);
                states[stepsInSolution] = originalState;
                System.err.println("1. this is the state that the coming move is made on\n" + originalState);
                originalState = new State(originalState,jointAction);
                solution += bestAction.name + "\n";
                System.err.println("2. Solution iteration: " + i);
                System.err.println("3. and this is the move this" + bestAction.name);
                stepsInSolution++;
            }
            writer.addEntry(i,solution,states);

            int r = random.nextInt(1,originalState.walls.length-1);
            int c = random.nextInt(1, originalState.walls[0].length-1);
            while ((r == 3 && c == 1) || (r == 3 && c == 3)){
                r = random.nextInt(1,4);
            }
            originalState = new State(originalState, r, c);
            solution = "";
            i++;
        }

        writer.closeAndWrite();
    }
}
