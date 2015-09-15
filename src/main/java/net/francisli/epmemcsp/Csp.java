package net.francisli.epmemcsp;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.francisli.epmemcsp.Symbol.SymbolType.*;
import static net.francisli.epmemcsp.ColumnName.*;

/**
 * Algorithm works as follows:
 *
 * Retrieve all relations from database. Each wme corresponds to one relation.
 * Generate a constraint graph, where the nodes are the unique identifiers in the query
 * There are binary constraints from identifier wmes, and unary constraints from constant wmes
 * We can assume the query is acyclic, so the constraint graph should be a join tree
 * So each variable should have some number of unary/binary constraints related to it
 * The cost of which is the product of the cardinality of the relations (not including the smallest)
 * The constraints related to a specific variable can be thought of as the sub-problem it needs to
 * solve.
 * Proceeding in reverse of the ordering chosen, each variable must solve its sub-problem
 *
 */
final class Csp implements AutoCloseable {

  private final SoarDb db;

  Csp(String filename) {
    db = new SoarDb(filename);
  }

  private Id[] generateOrdering(Collection<Wme> query) {
    Map<Id, Set<Id>> graph = new LinkedHashMap<>();
    query.forEach(wme -> {
      if (wme.getVal().getType() == IDENTIFIER) {
        Id id = wme.getId();
        Id val = wme.getVal().getIdVal();
        Set<Id> edges = graph.computeIfAbsent(id, x -> new LinkedHashSet<>());
        edges.add(val);
        edges = graph.computeIfAbsent(val, x -> new LinkedHashSet<>());
        edges.add(id);
      } else {
        if (!graph.containsKey(wme.getId())) {
          graph.put(wme.getId(), new LinkedHashSet<>());
        }
      }
    });

    Id[] ordering = new Id[graph.size()];

    for (int i = graph.size(); i > 0; i--) {
      Id smallest = graph.entrySet().stream().
          min((e1, e2) -> e1.getValue().size() - e2.getValue().size()).get().getKey();
      ordering[i - 1] = smallest;
      graph.get(smallest).forEach(id -> graph.get(id).remove(smallest));
      graph.remove(smallest);
    }
    return ordering;
  }

  public Map<Wme, Relation> getConstraints(Collection<Wme> query) {
    return query.stream().collect(Collectors.toMap(Function.identity(),
        wme -> {
          if (wme.getVal().getType() == IDENTIFIER) {
            return new Relation(db.wmeRows(wme), WI_ID, PARENT_N_ID, CHILD_N_ID);
          } else {
            return new Relation(db.wmeRows(wme), WC_ID, PARENT_N_ID);
          }}));
  }

  public void processUnary(Map<Wme, Relation> constraints, Map<Id, Set<Integer>> domains) {
    constraints.forEach((wme, relation) -> {
      if (wme.getVal().getType() != IDENTIFIER) {
        Set<Integer> domain = domains.get(wme.getId());
        if (domain == null) {
          domains.put(wme.getId(), new HashSet<>(relation.projectColumn(PARENT_N_ID)));
        } else {
          domain.retainAll(relation.projectColumn(PARENT_N_ID));
        }
      }
    });
  }

  public static Map<Id, Map<Id, Wme>> buildConstraintGraph(Collection<Wme> query) {
    Map<Id, Map<Id, Wme>> constraintGraph = new LinkedHashMap<>();
    query.forEach(wme -> {
      if (wme.getVal().getType() == IDENTIFIER) {
        Id id = wme.getId();
        Id val = wme.getVal().getIdVal();
        Map<Id, Wme> edges = constraintGraph.computeIfAbsent(id, x -> new LinkedHashMap<>());
        edges.put(val, wme);
        edges = constraintGraph.computeIfAbsent(val, x -> new LinkedHashMap<>());
        edges.put(id, wme);
      }
    });
    return constraintGraph;
  }

  public static void directionalArcConsistency(Map<Wme, Relation> constraints,
                                               Map<Id, Set<Integer>> domains,
                                               Map<Id, Map<Id, Wme>> constraintGraph,
                                               Id[] ordering) {
    for (int i = ordering.length - 1; i >= 0; i--) {
      Id varI = ordering[i];
      for (int j = 0; j < i; j++) {
        Id varJ = ordering[j];
        Map<Id, Wme> valMap = constraintGraph.get(varI);
        if (valMap != null) {
          Wme wme = valMap.get(varJ);
          if (wme != null) {
            Set<Integer> domainI = domains.get(varI);
            Set<Integer> domainJ = domains.get(varJ);
            ColumnName iField;
            ColumnName jField;
            if (wme.getId().equals(varI)) {
              iField = PARENT_N_ID;
              jField = CHILD_N_ID;
            } else {
              iField = CHILD_N_ID;
              jField = PARENT_N_ID;
            }
            if (domainI == null) {
              domainI = new HashSet<>(constraints.get(wme).projectColumn(iField));
              domains.put(varI, domainI);
            }
            if (domainJ == null) {
              domains.put(varJ, new HashSet<>(constraints.get(wme).joinThenProject(iField, domainI, jField)));
            } else {
              domainJ.retainAll(constraints.get(wme).joinThenProject(iField, domainI, jField));
            }
          }
        }
      }
    }
  }

  private static Integer selectValue(Set<Integer> domain) {
    for (Iterator<Integer> it = domain.iterator(); it.hasNext();) {
      int element = it.next();
      it.remove();
      return element;
    }
    return null;
  }

  public static Collection<Map<Id, Integer>> generateSolutions(Map<Id, Set<Integer>> domains,
                                                               Id[] ordering) {
    Set<Integer> domainCopy = new LinkedHashSet<>(domains.get(ordering[0]));
    Collection<Map<Id, Integer>> solutions = new ArrayDeque<>();
    Map<Id, Integer> instantiation = new HashMap<>(ordering.length + 1, 1);

    for (int i = 0; i < ordering.length && i >= 0;) {
      Integer value = selectValue(domainCopy);
      if (value == null) {
        i--;
      } else {
        instantiation.put(ordering[i], value);
        i++;
        if (i < ordering.length) {
          domainCopy = new LinkedHashSet<>(domains.get(ordering[i]));
        } else {
          solutions.add(new HashMap<>(instantiation));
          i--;
        }
      }
    }
    return solutions;
  }

  public Result go(Collection<Wme> query) {
    Map<Wme, Relation> constraints = getConstraints(query);
    Map<Id, Set<Integer>> domains = new HashMap<>();
    processUnary(constraints, domains);
    Map<Id, Map<Id, Wme>> constraintGraph = buildConstraintGraph(query);
    Id[] ordering = generateOrdering(query);
    Id[] reverse = new Id[ordering.length];
    for (int i = 0; i < ordering.length; i++) {
      reverse[i] = ordering[ordering.length-i-1];
    }
    directionalArcConsistency(constraints, domains, constraintGraph, ordering);
    directionalArcConsistency(constraints, domains, constraintGraph, reverse);

    Collection<Map<Id, Integer>> solutions = generateSolutions(domains, ordering);

    TemporalMatcher matcher = new TemporalMatcher(db, solutions, constraints);

    return matcher.run();

  }
  @Override
  public void close() {
    db.close();
  }
}