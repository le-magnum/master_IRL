package irlclient;

public class Rewards
{
  public int[] weights;


  public Rewards(int[] weights){
    this.weights = weights;
  }

  public int calculateRewards(int[] features){
    int reward = 0;
    for (int i = 0; i < features.length; i++) {
      reward += weights[i] * features[i];
    }
    return reward;
  }

  public void updateWeights(double[] newWeights){
    for (int i = 0; i < newWeights.length; i++) {
      this.weights[i] = (int)newWeights[i];
    }
  }
}
