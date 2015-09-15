package net.francisli.epmemcsp;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class CspTest {
  
  private static final int numRuns = 1000;

  @Test
  public void DomainTest2048() {
    Main.main(new String[]{"tanksoardb/tanksoar101.db", "tanksoar-8.cue", "20000"});
  }

//  @Test
  public void DomainTest() {
    Main.main(new String[]{"tanksoardb/tanksoar101.db", "tanksoar-8.cue", "1"});
    Main.main(new String[]{"tanksoardb/tanksoar101.db", "tanksoar-12.cue", "1"});
    Main.main(new String[]{"tanksoardb/tanksoar101.db", "tanksoar-13.cue", "1"});
    Main.main(new String[]{"tanksoardb/tanksoar101.db", "tanksoar-15.cue", "1"});
  }

//  @Test
  public void CellEpTest() {
    Main.main(new String[]{"../epmemtest/10ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/100ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/200ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/300ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/400ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/500ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/600ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/700ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/800ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/900ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../epmemtest/1000ep.db", "2cell.cue", "10000"});
  }

//  @Test
  public void CellTest() {
    Main.main(new String[]{"../10ep.db", "1cell.cue", "10000"});
    Main.main(new String[]{"../10ep.db", "2cell.cue", "10000"});
    Main.main(new String[]{"../10ep.db", "3cell.cue", "10000"});
    Main.main(new String[]{"../10ep.db", "4cell.cue", "10000"});
  }
  
//  @Test
  public void Test() {
    try {
//      Main.main(new String[]{"tanksoar-355142.db", "tanksoar-1.cue", "1"});
//      Main.main(new String[]{"big.db", "2048.cue", "1"});
//      Main.main(new String[]{"big.db", "small.cue", "1"});
//      Main.main(new String[]{"165.db", "2048.cue", "1"});
//      Main.main(new String[]{"165.db", "small.cue", "1"});
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

//  @Test
  public void Results2() {
    Main.main(new String[]{"tanksoar-355142.db", "tanksoar-15.cue", "5000"});
    for (int i = 0; i <= 100; i+=10) {
      String dbFile = "tanksoardb/tanksoar" + i + ".db";
      Main.main(new String[]{dbFile, "tanksoar-15.cue", "2000"});
    }
  }

//  @Test
  public void Results() {
    Main.main(new String[]{"tanksoar-355142.db", "tanksoar-15.cue", "30000"});
    for (int i = 0; i <= 101; i++) {
      String dbFile = "tanksoardb/tanksoar" + i + ".db";
      Main.main(new String[]{dbFile, "tanksoar-15.cue", "2000"});
    }
  }

//  @Test
  public void TankSoar12() {
    String runs = Integer.toString(numRuns);
    Main.main(new String[]{"tanksoar-355142.db", "tanksoar-15.cue", "30000"});
    try {
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-1.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-2.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-3.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-4.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-5.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-6.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-7.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-8.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-9.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-10.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-11.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-12.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-13.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-14.cue", runs});
      Main.main(new String[]{"tanksoardb/tanksoar0.db", "tanksoar-15.cue", runs});
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

//  @Test
//  public void JoinTest() {
//    Set<WmeRow> leftRows = new LinkedHashSet<>(Arrays.asList(
//        new WmeRow(0, 1, 2),
//        new WmeRow(1, 3, 4),
//        new WmeRow(2, 5, 6),
//        new WmeRow(3, 7, 8)
//    ));
//    Set<WmeRow> rightRows = new LinkedHashSet<>(Arrays.asList(
//        new WmeRow(4, 0, 0),
//        new WmeRow(5, 3, 3),
//        new WmeRow(6, 4, 4),
//        new WmeRow(7, 7, 8)
//    ));
//
//    Set<WmeRow> leftRowsAfter = new LinkedHashSet<>(Arrays.asList(
//        new WmeRow(1, 3, 4),
//        new WmeRow(3, 7, 8)
//    ));
//    Set<WmeRow> rightRowsAfter = new LinkedHashSet<>(Arrays.asList(
//        new WmeRow(5, 3, 3),
//        new WmeRow(7, 7, 8)
//    ));
//
//
//    Csp.joinAndPrune(leftRows, WmeField.ID, rightRows, WmeField.ID);
//
//    assertEquals(leftRows, leftRowsAfter);
//    assertEquals(rightRows, rightRowsAfter);
//
//  }
//
//  @Test
//  public void intersectionTest() {
//    Set<Integer> a = new HashSet<>(Arrays.asList(1,2,3));
//    Set<Integer> b = new HashSet<>(Arrays.asList(2,3,4));
//    assertEquals(Csp.intersect(a, b), new HashSet<>(Arrays.asList(2,3)));
//  }
}
