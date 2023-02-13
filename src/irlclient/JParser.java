package irlclient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public final class JParser {
    static private final JSONParser jsonParser = new JSONParser();

    static private Reader reader;

    static private JSONObject jObject;


    public static void SetUpJParser() throws IOException, ParseException {
        try {
            reader = new FileReader("../src/irlclient/config.json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Object temp = jsonParser.parse(reader);

        jObject = (JSONObject) temp;

    }

    public static boolean runGeneration() {
        boolean runGeneration = (boolean) jObject.get("runGeneration");

        return runGeneration;
    }

    public static boolean runTesting() {
        boolean runTesting = (boolean) jObject.get("runTesting");

        return runTesting;
    }

    public static boolean runActiveLearning() {
        boolean activeLearning = (boolean) jObject.get("runActiveLearning");

        return activeLearning;
    }

    public static int getAmountOfFeatures() {
        return (int) (long) jObject.get("amountOfFeatures");
    }


    public static GenerationParametersObject fetchGenerationParametersObject() throws IOException, ParseException {

        int amountOfFeatures = (int) (long) jObject.get("amountOfFeatures");

        JSONArray jsonArray = (JSONArray) jObject.get("generationParameters");

        GenerationParametersObject DAO = new GenerationParametersObject(jsonArray, amountOfFeatures);

        return DAO;
    }

    public static EstimationParameters fetchEstimationParameters() {

        JSONArray jsonArray = (JSONArray) jObject.get("estimatorParameters");

        EstimationParameters DAO = new EstimationParameters(jsonArray);

        return DAO;
    }

    public static class GenerationParametersObject {
        private final double[] rewardWeights;
        private int iterations;
        private int amountOfTrajectories;
        private double theta;
        private double gemma;

        private GenerationParametersObject(JSONArray generationParameters, int amountOfFeatures) {
            rewardWeights = new double[amountOfFeatures];
            for (int i = 0; i < generationParameters.size(); i++) {
                JSONObject tempJObject = (JSONObject) generationParameters.get(i);

                String str = tempJObject.toJSONString();
                String[] strings = str.split("(\")");
                str = strings[1].replaceAll("\"", "");

                switch (str) {
                    case "iterations":
                        iterations = Integer.parseInt(strings[2].replaceAll("[:}]", ""));
                        break;
                    case "thetaValue":
                        theta = Double.parseDouble(strings[2].replaceAll("[:}]", ""));
                        break;
                    case "gemma":
                        gemma = Double.parseDouble(strings[2].replaceAll("[:}]", ""));
                        break;
                    case "amountOfTrajectories":
                        amountOfTrajectories = Integer.parseInt(strings[2].replaceAll("[:}]", ""));
                    case "weightValues":
                        String values = strings[2].replaceAll("[:} \\[ \\] ]", "");
                        strings = values.split(",");
                        for (int j = 0; j < strings.length; j++) {
                            rewardWeights[j] = Double.parseDouble(strings[j]);
                        }
                }
            }
        }

        public int getIterations() {
            return iterations;
        }

        public double getTheta() {
            return theta;
        }

        public double getGemma() {
            return gemma;
        }

        public double[] getRewardWeights() {
            return rewardWeights;
        }

        public int getAmountOfTrajectories() {
            return amountOfTrajectories;
        }
    }

    public static class EstimationParameters {
        private int estimationIterations;

        private int valueIterations;

        private double gemma;

        private double boltzmannTemperature;

        private double stepSize;


        private EstimationParameters(JSONArray estimationParameters) {
            for (int i = 0; i < estimationParameters.size(); i++) {
                JSONObject tempJObject = (JSONObject) estimationParameters.get(i);

                String str = tempJObject.toJSONString();
                String[] strings = str.split("(\")");
                str = strings[1].replaceAll("\"", "");

                switch (str) {
                    case "estimationIterations":
                        estimationIterations = Integer.parseInt(strings[2].replaceAll("[:}]", ""));
                        break;
                    case "valueIterations":
                        valueIterations = Integer.parseInt(strings[2].replaceAll("[:}]", ""));
                        break;
                    case "gemma":
                        gemma = Double.parseDouble(strings[2].replaceAll("[:}]", ""));
                        break;
                    case "boltzmannTemperature":
                        boltzmannTemperature = Double.parseDouble(strings[2].replaceAll("[:}]", ""));
                    case "stepSize":
                        stepSize = Double.parseDouble(strings[2].replaceAll("[:}]", ""));
                }
            }
        }

        public int getEstimationIterations() {
            return estimationIterations;
        }

        public int getValueIterations() {
            return valueIterations;
        }

        public double getGemma() {
            return gemma;
        }

        public double getBoltzmannTemperature() {
            return boltzmannTemperature;
        }

        public double getStepSize() {
            return stepSize;
        }
    }

}
