package irlclient;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Modifier {

    private int featureCounter = 0;

    public void modifyChoiceLevel(int[] featuresToTest, State choiceState){
        if (this.featureCounter == 0) {
            choiceState.goals[1][5] = 'A';
            choiceState.goals[1][1] = '0';
        }
        if (this.featureCounter == 1){
            choiceState.goals[1][1] = choiceState.goals[0][0];
        }
        featureCounter++;
    }
}
