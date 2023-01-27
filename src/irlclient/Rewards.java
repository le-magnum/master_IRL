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


    public Rewards() {
        this.weights = new double[ReadAmountOfFeatures()];
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

    public int ReadAmountOfFeatures() {
        JSONParser jPaser = new JSONParser();
        try {
            Reader reader = new FileReader("../src/irlclient/config.json");
            Object temp = jPaser.parse(reader);
            JSONObject jsonObject = (JSONObject) temp;

            int i = (int) (long) jsonObject.get("amountOfFeatures");

            return i;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void ReadRewardWeights() {
        JSONParser jPaser = new JSONParser();
        try {
            Reader reader = new FileReader("../src/irlclient/config.json");
            Object temp = jPaser.parse(reader);

            JSONObject jsonObject = (JSONObject) temp;

            JSONArray jsonArray = (JSONArray) jsonObject.get("weightValues");
            int i = 0;
            for (Object weight : jsonArray) {
                this.weights[i] = (double) weight;
                i++;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
