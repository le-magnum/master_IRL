package irlclient;

import java.util.*;

public class Estimator {
    private Rewards rewards;

    private ValueIteration vl = new ValueIteration();

    private Transitions transitions = new Transitions();
    private HashMap<StateActionPair, Double> qEstimations = new HashMap<>();

    private HashMap<State, Double> valueEstimations = new HashMap<>();

    private HashSet<StateActionPair> pairs = new HashSet<>();

    private HashMap<State, Double> zValues = new HashMap<>();

    private HashMap<State, double[]> zValuesDifferentiated = new HashMap<>();

    private HashMap<StateActionPair, double[]> estimationDifferentiated = new HashMap<>();

    private HashMap<State, double[]> valueDifferentiated = new HashMap<>();

    private HashMap<StateActionPair, Double> policyValues = new HashMap<>();

    private HashMap<StateActionPair, double[]> policyDifferentiated = new HashMap<>();

    private HashMap<Trajectory, Double> trajectoryProbability = new HashMap<>();


    public Rewards maximumLikelihoodEstimation(Trajectory[] trajectories) {
        vl.fillListOfStates(trajectories[0].getStates()[0]);
        HashSet<State> states = vl.getSet();

        double gemma = 1;
        double boltzTemp = 1;
        double[] rewardWeights = new double[3];
        int[] stateFeatures = new int[3];
        //normal field
        rewardWeights[0] = 0;
        //box goal
        rewardWeights[1] = 0;
        //agent goal
        rewardWeights[2] = 0;
        rewards = new Rewards(rewardWeights);
        int n = 50;
        int k = 5;
        double[] likelihoodWeights = new double[rewardWeights.length];
        double learningRate = 1;
        setupEstimationTables(states, rewards, stateFeatures);
        double previousLikelihood = -1000;
        double logLikelihood = 0;
        System.err.println("this is estimator function");
        calculateProbabilitiesOfTrajectories(trajectories);

        for (int t = 0; t < n; t++) {
            setupEstimationTables(states, rewards, stateFeatures);
            for (int i = 1; i < k; i++) {
                for (StateActionPair pair : pairs) {
                    // first let calculate Q_i(s,a)
                    double stateEstimation;
                    double reward = rewards.calculateRewards(pair.getState()
                            .extractFeatures(new int[stateFeatures.length]));
                    int transition = transitions.transitionFunction(pair.getState(), pair.getAction(), 0);
                    if (transition == 0) {
                        stateEstimation = reward;
                    }
                    State nextState = transitions.nextState(pair.getState(), pair.getAction());
                    stateEstimation = reward + gemma * transition *
                            valueEstimations.get(transitions.nextState(pair.getState(),
                                    pair.getAction()));
                    qEstimations.put(pair, stateEstimation);
                }


                for (Map.Entry<StateActionPair, Double> pair : qEstimations.entrySet()) {
                    // secondly let calculate dQ_i(s,a)/dw_j
                    double[] differentiatedEstimationsOnWeight = new double[stateFeatures.length];
                    for (int j = 0; j < stateFeatures.length; j++) {
                        int feature = pair.getKey().getState().extractFeatures(new int[stateFeatures.length])[j];
                        int tran = transitions.transitionFunction(pair.getKey().getState(),
                                pair.getKey().getAction(),
                                0);
                        if (tran == 0) {
                            differentiatedEstimationsOnWeight[j] = feature;
                            continue;
                        }
                        double nextValue = valueDifferentiated.get(transitions.nextState(pair.getKey().getState(),
                                pair.getKey().getAction()))[j];

                        differentiatedEstimationsOnWeight[j] = feature + gemma * tran * nextValue;
                    }
                    estimationDifferentiated.put(pair.getKey(), differentiatedEstimationsOnWeight);
                }


                for (Map.Entry<StateActionPair, Double> pair : qEstimations.entrySet()) {
                    // then the distribution sample Z_i(s)
                    double z = 0;
                    for (Action action : Action.values()) {
                        StateActionPair permutation = new StateActionPair(pair.getKey().getState(), action);
                        z += Math.exp(boltzTemp * qEstimations.get(permutation));
                    }
                    zValues.put(pair.getKey().getState(), z);
                }

                for (Map.Entry<State, Double> pair : zValues.entrySet()) {
                    // then distribution sample differentiated
                    double[] differentiatedZVal = new double[stateFeatures.length];
                    for (int j = 0; j < stateFeatures.length; j++) {
                        for (Action action : Action.values()) {
                            StateActionPair stateActionPair = new StateActionPair(pair.getKey(), action);
                            double diffZVal = boltzTemp *
                                    Math.exp(boltzTemp * qEstimations.get(stateActionPair)) *
                                    estimationDifferentiated.get(stateActionPair)[j];
                            differentiatedZVal[j] += diffZVal;
                        }
                    }
                    this.zValuesDifferentiated.put(pair.getKey(), differentiatedZVal);
                }
                for (Map.Entry<StateActionPair, Double> pair : qEstimations.entrySet()) {
                    // the policy $\pi(s,a)$
                    double policyValue = Math.exp(boltzTemp * qEstimations.get(pair.getKey())) /
                            zValues.get(pair.getKey().getState());
                    policyValues.put(pair.getKey(), policyValue);
                }

                for (Map.Entry<StateActionPair, Double> pair : qEstimations.entrySet()) {
                    // the policy differentiated
                    stateFeatures = pair.getKey().getState().extractFeatures(stateFeatures);
                    for (int j = 0; j < stateFeatures.length; j++) {
                        double zValue = zValues.get(pair.getKey().getState());
                        double zDiff = zValuesDifferentiated.get(pair.getKey().getState())[j];
                        double exp = Math.exp(boltzTemp * qEstimations.get(pair.getKey()));
                        double qDiff = estimationDifferentiated.get(pair.getKey())[j];
                        policyDifferentiated.get(pair.getKey())[j] = (boltzTemp * zValue * exp * qDiff -
                                exp * zDiff) / Math.pow(zValue, 2);

                    }
                }

                for (Map.Entry<State, Double> pair : this.zValues.entrySet()) {
                    // value function
                    double previousValue = 0;
                    for (Action action : Action.values()) {
                        StateActionPair permutation = new StateActionPair(pair.getKey(), action);
                        double newValue = this.policyValues.get(permutation) * qEstimations.get(permutation);
                        valueEstimations.put(pair.getKey(), previousValue + newValue);
                    }
                }
                for (State state : states) {
                    // value function differentiated
                    double[] values = valueDifferentiated.get(state);

                    for (int j = 0; j < stateFeatures.length; j++) {
                        double vdfWeight = 0;
                        for (Action action : Action.values()) {
                            StateActionPair permutation = new StateActionPair(state, action);
                            vdfWeight += qEstimations.get(permutation) * policyDifferentiated.get(permutation)[j] +
                                    this.policyValues.get(permutation) *
                                            estimationDifferentiated.get(permutation)[j];
                        }
                        values[j] = vdfWeight;
                    }
                    valueDifferentiated.put(state, values);
                }

                // then the log likelihood
                for (int j = 0; j < trajectories.length; j++) {
                    // TODO: 13/12/2022 calculate for each trajectory the policy of seeing this state action pair
                    Double policyV = 0.0;
                    for (int l = 0; l < trajectories[j].getActions().length; l++) {
                        StateActionPair trajectoryPair = new StateActionPair(trajectories[j].getStates()[l],
                                trajectories[j].getActions()[l]);

                        policyV += Math.log(this.policyValues.get(trajectoryPair));
                    }
                    logLikelihood += this.trajectoryProbability.get(trajectories[j]) * policyV;

                }

                // the likelihood differentiated
                for (int j = 0; j < stateFeatures.length; j++) {
                    double likelihoodDifferentiated = 0;
                    for (int g = 0; g < trajectories.length; g++) {
                        double policyChainRule = 0;
                        for (int l = 0; l < trajectories[g].getActions().length; l++) {
                            StateActionPair trajectoryPair = new StateActionPair(trajectories[g].getStates()[l],
                                    trajectories[g].getActions()[l]);
                            policyChainRule += 1 / this.policyValues.get(trajectoryPair) *
                                    policyDifferentiated.get(trajectoryPair)[j];

                        }
                        likelihoodDifferentiated = policyChainRule * trajectoryProbability.get(trajectories[g]);
                    }
                    //if (logLikelihood >= previousLikelihood) {
                        likelihoodWeights[j] = likelihoodDifferentiated;
                        previousLikelihood = logLikelihood;
                 //   }
                }
                logLikelihood = 0;
            }
            for (int i = 0; i < rewardWeights.length; i++) {
                likelihoodWeights[i] = likelihoodWeights[i] * learningRate;
            }
            rewards.updateWeights(likelihoodWeights);
        }
        return rewards;
    }

    private void setupEstimationTables(HashSet<State> states, Rewards rewards, int[] features) {
        System.err.println("this is setup function");
        for (State state : states) {
            double[] individualFeatures = new double[features.length];
            for (Action action : Action.values()) {
                StateActionPair pair = new StateActionPair(state, action);
                pairs.add(pair);
                this.qEstimations.put(pair, rewards.calculateRewards(state.extractFeatures(new int[features.length])));
                this.estimationDifferentiated.put(pair, new double[features.length]);
                this.policyDifferentiated.put(pair, new double[features.length]);
                this.policyValues.put(pair, 0.0);
            }

            this.valueEstimations.put(state, rewards.calculateRewards(state.extractFeatures(new int[features.length])));
            this.zValuesDifferentiated.put(state, new double[features.length]);
            int j = 0;
            features = state.extractFeatures(new int[features.length]);
            for (int i : features) {
                individualFeatures[j] = i;
                j++;
            }
            this.valueDifferentiated.put(state, new double[features.length]);
        }
    }

    private void calculateProbabilitiesOfTrajectories(Trajectory[] trajectories) {
        for (int i = 0; i < trajectories.length; i++) {
            double probability = 1;
            for (int j = 1; j < trajectories.length; j++) {
                if (trajectories[i].equals(trajectories[j])) {
                    probability++;
                }
            }
            if (!(this.trajectoryProbability.containsKey(trajectories[i]))) {
                trajectoryProbability.put(trajectories[i], probability / trajectories.length);
            }

        }
    }
}



