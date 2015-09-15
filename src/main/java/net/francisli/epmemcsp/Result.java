package net.francisli.epmemcsp;

/**
 * Created by francis on 7/07/2015.
 */
public class Result {
  public final int episode;
  public final int score;
  public final int maxScore;
  public final double cardinality;
  public final boolean structuralMatch;
  public final int maxEpisode;

  public Result(int episode, int maxEpisode, int score, int maxScore) {
    this.episode = episode;
    this.maxEpisode = maxEpisode;
    this.score = score;
    this.maxScore = maxScore;
    cardinality = (double) score / maxScore;
    structuralMatch = score == maxScore;
  }
}
