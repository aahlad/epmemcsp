package net.francisli.epmemcsp;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import au.com.rete.db.Sqlite3;
import au.com.rete.db.Sqlite3.*;

import static net.francisli.epmemcsp.Symbol.SymbolType.*;
import static net.francisli.epmemcsp.IntervalEndpoint.Type.*;

public class SoarDb implements AutoCloseable {

  private final Sqlite3 db;
  private final Statement beginTransaction;
  private final Statement commit;
  private final Statement idStmt;
  private final Statement constStmt;
  private final Statement strSymStmt;
  private final Statement intSymStmt;
  private final Statement idIntervalNowStmt;
  private final Statement constIntervalNowStmt;
  private final Statement maxEpisodeStmt;

  private final static String identifierPointIntervalsSql =
      "SELECT episode_id FROM epmem_wmes_identifier_point WHERE wi_id = ? ORDER BY episode_id DESC";
  private final static String identifierRangeIntervalsSql =
      "SELECT start_episode_id, end_episode_id FROM epmem_wmes_identifier_range" +
          "WHERE wi_id = ? ORDER BY end_episode_id DESC";
  private final static String constantPointIntervalsSql =
      "SELECT episode_id FROM epmem_wmes_constant_point WHERE wc_id = ? ORDER BY episode_id DESC";
  private final static String constantRangeIntervalsSql =
      "SELECT start_episode_id, end_episode_id FROM epmem_wmes_constant_range" +
          "WHERE wc_id = ? ORDER BY end_episode_id DESC";

  private final Map<Object, Integer> symbolTable = new HashMap<>();
  private boolean CLOSED;
  private int maxEpisodeNumber = -1;
  
  public SoarDb(String filename) {
    db = Sqlite3.loadToMemory(filename);
    beginTransaction = db.prepare("BEGIN EXCLUSIVE");
    commit = db.prepare("COMMIT");
    idStmt = db.prepare("SELECT wi_id, parent_n_id, child_n_id FROM epmem_wmes_identifier " +
        "WHERE attribute_s_id = ?");
    constStmt = db.prepare("SELECT wc_id, parent_n_id FROM epmem_wmes_constant " +
        "WHERE attribute_s_id = ? and value_s_id = ?");
    strSymStmt = db.prepare("SELECT s_id FROM epmem_symbols_string WHERE symbol_value = ?");
    intSymStmt = db.prepare("SELECT s_id FROM epmem_symbols_integer WHERE symbol_value = ?");
    idIntervalNowStmt = db.prepare("SELECT start_episode_id FROM epmem_wmes_identifier_now " +
        "WHERE wi_id = ?");
    constIntervalNowStmt = db.prepare("SELECT start_episode_id FROM epmem_wmes_constant_now " +
        "WHERE wc_id = ?");
    maxEpisodeStmt = db.prepare("SELECT max(episode_id) FROM epmem_episodes");
    stepAndThrow(beginTransaction);
  }

  private boolean stepAndThrow(Statement stmt) {
    try {
      return stmt.step();
    } catch (Sqlite3BusyException e) {
      throw new IllegalStateException();
    }
  }

  public int getMaxEpisodeNumber() {
    if (maxEpisodeNumber == -1) {
      stepAndThrow(maxEpisodeStmt);
      maxEpisodeNumber = maxEpisodeStmt.columnInt(0);
      maxEpisodeStmt.reset();
    }
    return maxEpisodeNumber;
  }

  private int getSymbolId(String sym) throws Sqlite3BusyException {
    Integer symId = symbolTable.get(sym);
    if (symId == null) {
      strSymStmt.bind(1, sym);
      strSymStmt.step();
      symId = strSymStmt.columnInt(0);
      strSymStmt.reset();
      symbolTable.put(sym, symId);
    }
    return symId;
  }

  private int getSymbolId(int sym) throws Sqlite3BusyException {
    Integer symId = symbolTable.get(sym);
    if (symId == null) {
      intSymStmt.bind(1, sym);
      intSymStmt.step();
      symId = intSymStmt.columnInt(0);
      intSymStmt.reset();
      symbolTable.put(sym, symId);
    }
    return symId;
  }

  public Iterator<IntervalEndpoint> getIntervalIterator(Wme wme, int nodeId) {
    return new IntervalEndpointIterator(wme, nodeId);
  }

  public class IntervalEndpointIterator implements Iterator<IntervalEndpoint> {

    private Statement pointIntervals;
    private Statement rangeIntervals;
    private final Queue<IntervalEndpoint> endpoints = new ArrayDeque<>();
    private IntervalEndpoint latestPointEndpoint;
    private IntervalEndpoint latestRangeEndpoint;
    private IntervalEndpoint latestRangeStartpoint;

    public IntervalEndpointIterator(Wme wme, int nodeId) {
      boolean wmeIsIdentifier = wme.getVal().getType() == IDENTIFIER;
      Statement nowIntervals;
      if (wmeIsIdentifier) {
        nowIntervals = idIntervalNowStmt;
      } else {
        nowIntervals = constIntervalNowStmt;
      }
      nowIntervals.bind(1, nodeId);

      if (stepAndThrow(nowIntervals)) {
        int startId = nowIntervals.columnInt(0) - 1;
        endpoints.add(new IntervalEndpoint(getMaxEpisodeNumber(), END));
        endpoints.add(new IntervalEndpoint(startId, START));
        if (startId == 0) {
          pointIntervals = null;
          rangeIntervals = null;
          nowIntervals.reset();
          return;
        }
      }
      nowIntervals.reset();

      if (wmeIsIdentifier) {
        pointIntervals = db.prepare(identifierPointIntervalsSql);
        rangeIntervals = db.prepare(identifierRangeIntervalsSql);
      } else {
        pointIntervals = db.prepare(constantPointIntervalsSql);
        rangeIntervals = db.prepare(constantRangeIntervalsSql);
      }
      pointIntervals.bind(1, nodeId);
      rangeIntervals.bind(1, nodeId);
      stepPoint();
      stepRange();
      if (endpoints.peek() == null && !nextInterval()) {
        throw new IllegalStateException();
      }
    }

    private boolean nextInterval() {

      if (latestRangeEndpoint != null && (latestPointEndpoint == null || latestPointEndpoint.compareTo(latestRangeEndpoint) == -1)) {
        endpoints.add(latestRangeEndpoint);
        endpoints.add(latestRangeStartpoint);
        stepRange();
        return true;
      }

      if (latestPointEndpoint != null && (latestRangeEndpoint == null || latestPointEndpoint.compareTo(latestRangeEndpoint) == 1)) {
        endpoints.add(latestPointEndpoint);
        endpoints.add(new IntervalEndpoint(latestPointEndpoint.getValue() - 1, START));
        stepPoint();
        return true;
      }

      return false;

//      if (latestPointEndpoint == null) {
//        if (latestRangeEndpoint == null) {  // both finished
//          return false;
//        } else {  // just range
//          endpoints.add(latestRangeEndpoint);
//          endpoints.add(latestRangeStartpoint);
//          stepRange();
//        }
//      } else {
//        if (latestRangeEndpoint == null) {  // just point
//          endpoints.add(latestPointEndpoint);
//          endpoints.add(new IntervalEndpoint(latestPointEndpoint.getValue() - 1, START));
//          stepPoint();
//        } else if (latestPointEndpoint.compareTo(latestRangeEndpoint) == 1) { // both still going
//          endpoints.add(latestPointEndpoint);
//          endpoints.add(new IntervalEndpoint(latestPointEndpoint.getValue() - 1, START));
//          stepPoint();
//        } else {
//          endpoints.add(latestRangeEndpoint);
//          endpoints.add(latestRangeStartpoint);
//          stepRange();
//        }
//      }
//      return true;
    }

    private void stepRange() {
      if (stepAndThrow(rangeIntervals)) {
        latestRangeEndpoint = new IntervalEndpoint(rangeIntervals.columnInt(1), END);
        latestRangeStartpoint = new IntervalEndpoint(rangeIntervals.columnInt(0) - 1, START);
      } else {
        rangeIntervals.close();
        latestRangeEndpoint = null;
        latestRangeStartpoint = null;
      }
    }

    private void stepPoint() {
      if (stepAndThrow(pointIntervals)) {
        latestPointEndpoint = new IntervalEndpoint(pointIntervals.columnInt(0), END);
      } else {
        pointIntervals.close();
        latestPointEndpoint = null;
      }
    }

    @Override
    public boolean hasNext() {
      return endpoints.peek() != null || latestPointEndpoint != null || latestRangeEndpoint != null;
    }

    @Override
    public IntervalEndpoint next() {
      if (hasNext()) {
        nextInterval();
        return endpoints.remove();
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  private class IdWmeRowIterator implements Iterator<Row> {

    boolean hasRow;

    private boolean stepAndReset() {
      try {
        if (idStmt.step()) {
          return true;
        } else {
          idStmt.reset();
          return false;
        }
      }catch (Sqlite3BusyException e) {
        throw new IllegalStateException();
      }
    }

    IdWmeRowIterator(Wme wme) {
      try {
        idStmt.bind(1, getSymbolId(wme.getAttr()));
      } catch (Sqlite3BusyException e) {
        throw new IllegalStateException();
      }
      hasRow = stepAndReset();
    }

    @Override
    public boolean hasNext() {
      return hasRow;
    }

    @Override
    public Row next() {
      if (hasRow) {
        Row row = new Row(idStmt.columnInt(0), idStmt.columnInt(1), idStmt.columnInt(2));
        hasRow = stepAndReset();
        return row;
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  private class ConstWmeRowIterator implements Iterator<Row> {

    boolean hasRow;

    private boolean stepAndReset() {
      try {
        if (constStmt.step()) {
          return true;
        } else {
          constStmt.reset();
          return false;
        }
      } catch (Sqlite3BusyException e) {
        throw new IllegalStateException();
      }
    }

    ConstWmeRowIterator(Wme wme) {
      try {
        int valSymId;
        Symbol val = wme.getVal();
        switch (val.getType()) {
          case INTEGER:
            valSymId = getSymbolId(val.getIntVal());
            break;
          case STRING:
            valSymId = getSymbolId(val.getStringVal());
            break;
          default:
            throw new IllegalStateException();
        }
        constStmt.bind(1, getSymbolId(wme.getAttr())).bind(2, valSymId);
        hasRow = stepAndReset();

      } catch (Sqlite3BusyException e) {
        throw new IllegalStateException();
      }
    }

    @Override
    public boolean hasNext() {
      return hasRow;
    }

    @Override
    public Row next() {
      if (hasRow) {
        Row row = new Row(constStmt.columnInt(0), constStmt.columnInt(1));
        hasRow = stepAndReset();
        return row;
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  public Stream<Row> wmeRows(Wme wme) {
    Iterator<Row> iter;
    if (wme.getVal().getType() == IDENTIFIER) {
      iter = new IdWmeRowIterator(wme);
    } else {
      iter = new ConstWmeRowIterator(wme);
    }
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter,
        Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT), false);
  }
  
  @Override
  public void close() {
    if (!CLOSED) {
      try {
        commit.step();
      } catch (Sqlite3BusyException e) {
        throw new IllegalStateException();
      }
      beginTransaction.close();
      commit.close();
      idStmt.close();
      constStmt.close();
      strSymStmt.close();
      intSymStmt.close();
      idIntervalNowStmt.close();
      constIntervalNowStmt.close();
      maxEpisodeStmt.close();
      db.close();
      CLOSED = true;
    }
  }
}
