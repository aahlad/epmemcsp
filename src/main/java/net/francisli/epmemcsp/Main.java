package net.francisli.epmemcsp;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Main {

  /**
   * Usage: java -jar Csp.jar [db] [cue] [#runs (optional)]
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Error: invalid arguments");
      System.out.println("Usage: java -jar Csp.jar [db] [cue] [#runs (optional)]");
      return;
    }
    Set<Wme> q;
    try {
      q = Utils.readQuery(args[1]);
    } catch (IOException e) {
      System.out.println("Csp: Error reading cue file at " + args[1]);
      return;
    } catch (Utils.WmeFormatException e) {
      System.out.println("Csp: Error processing line in cue file with contents " + e.getMessage());
      return;
    }
    int runs = 1;
    if (args.length > 2) {
      try {
        runs = Integer.parseInt(args[2]);
        if (runs < 1) {
          System.err.println("Csp: Warning. #runs passed in was not a valid integer, defaulting to 1 run");
          runs = 1;
        }
      } catch (NumberFormatException e) {
        System.err.println("Csp: Warning. #runs passed in was not a valid integer, defaulting to 1 run");
      }
    }
//    System.out.println("Querying [" + args[0] + "] with [" + args[1] + "] " + runs + " times.");
    try (Csp csp = new Csp(args[0])) {
      Result r = null;
      List<Long> runTimes = new ArrayList<>(runs);
      for (int z = 0; z < runs; z++) {
        long startTime = System.nanoTime();
        r = csp.go(q);
        runTimes.add(System.nanoTime() - startTime);
      }
      DoubleSummaryStatistics stats = runTimes.stream().mapToDouble(l -> l.doubleValue() * 1e-6).summaryStatistics();
//      runTimes.sort((e1, e2) -> (int) (e1 - e2));
//      System.out.println(r.maxEpisode + "," + stats.getAverage());
      System.out.println(stats.getAverage());
//      System.out.println(String.format("Best episode: %d, score: %d/%d", r.episode, r.score, r.maxScore));
    }
  }
}
