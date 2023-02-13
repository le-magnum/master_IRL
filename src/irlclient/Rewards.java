package irlclient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


public class Rewards {
    public double[] weights;


    public Rewards(int amountOfFeatures) {
        this.weights = new double[amountOfFeatures];
    }

    public double calculateRewards(int[] features) {
        double reward = 0;
        for (int i = 0; i < features.length; i++) {
            reward += weights[i] * features[i];
        }
        return reward;
    }

    public void updateWeights(double[] newWeights) {
        for (int i = 0; i < newWeights.length; i++) {
            this.weights[i] += newWeights[i];
        }
    }



}
