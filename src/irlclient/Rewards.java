package irlclient;

public class Rewards
{
  public double[] weights;


  public Rewards(double[] weights){
    this.weights = weights;
  }

  public double calculateRewards(int[] features){
    double reward = 0;
    for (int i = 0; i < features.length; i++) {
      reward += weights[i] * features[i];
    }
    return reward;
  }

  public void updateWeights(double[] newWeights){
    for (int i = 0; i < newWeights.length; i++) {
      this.weights[i] += newWeights[i];
    }
  }
}
