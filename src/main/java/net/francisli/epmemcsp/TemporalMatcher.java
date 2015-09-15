package net.francisli.epmemcsp;

import java.util.*;
import java.util.stream.Collectors;

import static net.francisli.epmemcsp.Symbol.SymbolType.*;
import static net.francisli.epmemcsp.ColumnName.*;

public class TemporalMatcher {
  private final Collection<Clause> formula;
  private final SoarDb db;

  private final Set<Binding> bindingsDone;

  private static class Binding {
    private final Wme wme;
    private final Integer id;
    private final Integer val;

    public Binding(Wme wme, Integer id, Integer val) {
      this.wme = wme;
      this.id = id;
      this.val = val;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Binding binding = (Binding) o;
      return Objects.equals(wme, binding.wme) &&
          Objects.equals(id, binding.id) &&
          Objects.equals(val, binding.val);
    }

    @Override
    public int hashCode() {
      return Objects.hash(wme, id, val);
    }
  }

  private Clause clauseFromWmeRel(Collection<Map<Id, Integer>> solutions, Wme wme, Relation relation) {
    Collection<Literal> literals = new ArrayList<>();
    solutions.forEach(solution -> {
      List<ColumnName> selectCols = new ArrayList<>(1);
      List<Relation.Value> whereVals = new ArrayList<>(2);
      Integer idBinding = solution.get(wme.getId());
      Integer valBinding = null;
      whereVals.add(new Relation.Value(ColumnName.PARENT_N_ID, idBinding));
      if (wme.getVal().getType() == IDENTIFIER) {
        selectCols.add(WI_ID);
        valBinding = solution.get(wme.getVal().getIdVal());
        whereVals.add(new Relation.Value(ColumnName.CHILD_N_ID, valBinding));
      } else {
        selectCols.add(WC_ID);
      }
      Binding b = new Binding(wme, idBinding, valBinding);
      if (!bindingsDone.contains(b)) {
        bindingsDone.add(b);
        int nodeId = relation.selectWhere(selectCols, whereVals).findFirst().get().get(0);
        Iterator<IntervalEndpoint> iterator = db.getIntervalIterator(wme, nodeId);
        literals.add(new Literal(wme, nodeId, iterator));
      }
    });
    return new Clause(wme, literals, db.getMaxEpisodeNumber());
  }

  public TemporalMatcher(SoarDb db, Collection<Map<Id, Integer>> solutions, Map<Wme, Relation> constraints) {
    this.db = db;
    bindingsDone = new HashSet<>();
    formula = constraints.entrySet().stream().
        map(e -> clauseFromWmeRel(solutions, e.getKey(), e.getValue())).collect(Collectors.toList());
  }

  public Result run() {
//    System.out.println(this);
    int goal = formula.size();
    int count = 0;
    int maxCount = -1;

    int maxEpisode = db.getMaxEpisodeNumber();
    int currentEpisode = maxEpisode;
    int bestEpisode = currentEpisode;

    while (count < goal && currentEpisode > 0) {
      int ep = currentEpisode;

      // for each clause, step to currentepisode and get satisfaction
      //
      count = formula.stream().mapToInt(c -> c.step(ep)).sum();

      if (count > maxCount) {
        maxCount = count;
        bestEpisode = currentEpisode;
      }
      currentEpisode = formula.stream().mapToInt(Clause::nextEpisode).max().getAsInt();
    }
    return new Result(bestEpisode, maxEpisode, maxCount, goal);


  }

  @Override
  public String toString() {
    return formula.stream().map(Clause::toString).collect(Collectors.joining(" AND\n"));
  }


  public static class Clause {
    private final Wme wme;
    private final Collection<Literal> literals;
    private int satisfaction = 0;
    private final PriorityQueue<IntervalEndpoint> endpoints = new PriorityQueue<>((e1, e2) -> e2.compareTo(e1));
    int nextEpisode;
    public Clause(Wme wme, Collection<Literal> literals, int maxEp) {
      this.wme = wme;
      this.literals = literals;
      nextEpisode = maxEp;
    }

    public int step(int episode) {
      literals.forEach(literal -> literal.stepTo(episode).forEach(endpoints::add));
      IntervalEndpoint next = endpoints.peek();
      while (next != null && next.getValue() == episode) {
        endpoints.remove();
        if (next.getType() == IntervalEndpoint.Type.END) {
          satisfaction++;
        } else {
          satisfaction--;
        }
        next = endpoints.peek();
      }
      if (next != null) {
        nextEpisode = next.getValue();
      } else {
        nextEpisode = 0;
      }
      return satisfaction > 0 ? 1 : 0;
    }

    public int nextEpisode() {
      return nextEpisode;
    }

    @Override
    public String toString() {
      return "(" +
        literals.stream().map(Literal::toString).collect(Collectors.joining(" OR ")) +
          ")";

    }
  }


  public static class Literal {
    private final Wme wme;
    private final int nodeId;
    private final Iterator<IntervalEndpoint> iterator;
    private int iteratorPosition = Integer.MAX_VALUE;
    public Literal(Wme wme, int nodeId, Iterator<IntervalEndpoint> iterator) {
      this.wme = wme;
      this.nodeId = nodeId;
      this.iterator = iterator;
    }

    public Collection<IntervalEndpoint> stepTo(int position) {
      Collection<IntervalEndpoint> results = new ArrayDeque<>();
      while (iterator.hasNext() && iteratorPosition >= position) {
        results.add(iterator.next()); // do it twice
        IntervalEndpoint next = iterator.next();
        iteratorPosition = next.getValue();
        results.add(next);
      }
      return results;
    }

    @Override
    public String toString() {
      if (wme.getVal().getType() == IDENTIFIER) {
        return "{" + nodeId + ":" + wme.getAttr() + ":ID}";
      } else {
        return "{" + nodeId + ":" + wme.getAttr() + ":C}";
      }
    }
  }
}
