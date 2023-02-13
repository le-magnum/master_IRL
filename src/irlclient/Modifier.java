package irlclient;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Modifier {

    private char[] featuresArray;

    public void modifyChoiceLevel(State choiceState, int firstFeatureIndex, int secondFeatureIndex){
            choiceState.goals[1][5] = featuresArray[firstFeatureIndex];
            choiceState.goals[1][1] = featuresArray[secondFeatureIndex];
    }

    public Modifier(int amountOfFeatures) {
        this.featuresArray = new char[amountOfFeatures];
        char normalTile = ' ';
        featuresArray[1] = normalTile;
        int i = 0;
        char startingCharFeature = 'A';
        // minus 2 because the first index is taken by the normal tile and the last will be the agent goal.
        while (i < amountOfFeatures-3){
            startingCharFeature += i;
            featuresArray[i+2] = startingCharFeature;
            i++;
            startingCharFeature = 'A';
        }
        featuresArray[amountOfFeatures-1] = '0';

    }
}
