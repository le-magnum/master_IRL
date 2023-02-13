package irlclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Generator
{
    private State originalState;

    ValueIteration vl = new ValueIteration();

    private Writer writer = new Writer("/Users/magnus/Coding/master/data/trajectories.txt");

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

    public void generateTrajectories(JParser.GenerationParametersObject parameters, int amountOfTrajectories)
    throws IOException
    {
        vl.fillListOfStates(originalState);

        String solution = "";
        vl.calculateValueIteration(parameters, false, null);
        State[] states = new State[50];
        int startCoordinateRow;
        int startCoordinateCol;
        int i = 0;
        while (i < amountOfTrajectories){
            int stepsInSolution = 0;
            while (!originalState.isGoalStateWithoutBoxes() && stepsInSolution < states.length-1){
                if (originalState.noGoalState() && stepsInSolution > 1){
                    break;
                }
                Action bestAction =vl.extractPolicy(originalState);
                Map<Integer,Action> jointAction = new HashMap<>();
                jointAction.put(0,bestAction);
                states[stepsInSolution] = originalState;
               // System.err.println("1. this is the state that the coming move is made on\n" + originalState);
                //System.err.println("1.5 this is the best action: " + bestAction.name);
                originalState = new State(originalState,jointAction);
                solution += bestAction.name + "\n";
                //System.err.println("2. Solution iteration: " + i);
                // System.err.println("3. and this is the move this" + bestAction.name);
                stepsInSolution++;
            }
            writer.addEntry(i,solution,states);
            startCoordinateRow = random.nextInt(1,originalState.walls.length-1);
            startCoordinateCol = random.nextInt(1, originalState.walls[0].length-1);
            while (originalState.walls[startCoordinateRow][startCoordinateCol] || originalState.goals[startCoordinateRow][startCoordinateCol] == '0'){
                startCoordinateRow = random.nextInt(1,originalState.walls.length-1);
                startCoordinateCol = random.nextInt(1, originalState.walls[0].length-1);
            }
            originalState = new State(originalState,startCoordinateRow,startCoordinateCol);
            solution = "";
            i++;
        }

        writer.closeAndWrite();
    }
}
