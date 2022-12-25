package irlclient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reader
{

    private Path path = Paths.get("/Users/magnus/Coding/mavis/data/trajectories.txt");

    private StringBuilder builder = new StringBuilder();

    private Parser parser = new Parser();

    public Reader()
    {
    }

    public Trajectory[] extractTrajectoriesInformation()
    throws IOException
    {
        int amountOfTrajectories = 0;
        int statesAndActionsCounter = 0;
        int headerlines = 0;
        int g = 0;
        ArrayList<Integer> amountOfStatesAndActions = new ArrayList<>(Collections.nCopies(10,0));
        List<String> readTrajectories = Files.readAllLines(path);
        parser.readColorHeader(readTrajectories);



        for (String line : readTrajectories) {
            if (line.isEmpty()) {
                g++;
                continue;
            }
            else if (line.startsWith("trajectory")) {
                amountOfTrajectories++;
            }
            else if (line.contains("(") || line.contains("NoOP")){
                int j = amountOfStatesAndActions.get(statesAndActionsCounter);
                amountOfStatesAndActions.set(statesAndActionsCounter,j + 1);
            }
            else if (line.contains("]")){
                statesAndActionsCounter++;
            }
            else if (line.contains("#end")) {
                headerlines = g;
            }
            g++;

        }
        Trajectory[] trajectories = new Trajectory[amountOfTrajectories];
        int i = 0;
        int actionIndex = 0;
        for (int j = headerlines; j < readTrajectories.size(); j++) {
            if (readTrajectories.get(j).isEmpty()) {
                continue;
            }
            else if (readTrajectories.get(j).startsWith("trajectory")) {
                System.err.println("we are now making trajectory number "+ i);
                trajectories[i] = new Trajectory();
                System.err.println("and this the amount of states and actions it has "+ amountOfStatesAndActions.get(i));
                trajectories[i].setSize(amountOfStatesAndActions.get(i));

            }
            else if (readTrajectories.get(j).contains("(") || readTrajectories.get(j).contains("NoOP")){
                trajectories[i].setAction(actionIndex,stringToAction(readTrajectories.get(j)));
                actionIndex++;
            }
            else if (readTrajectories.get(j).contains("+")) {
                StringBuilder strBuilder = new StringBuilder();
                int k = j;
                while (readTrajectories.get(k).contains("+")){
                    strBuilder.append(readTrajectories.get(k)+"\n");
                    k++;
                }

                trajectories[i].setState(actionIndex,parser.parseState(strBuilder));
                j = k-1;
            }
            else if (readTrajectories.get(j).contains("]")){
                actionIndex = 0;
                i++;
            }

        }

        return trajectories;
    }
    private Action stringToAction(String actionString){
        Action act;
        if (actionString.contains("(")){
            actionString = actionString.replaceAll("[()]","");
            act = Action.valueOf(actionString);
        }
        else {
            act = Action.valueOf(actionString);
        }
        return act;
    }
}
