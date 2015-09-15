package net.francisli.epmemcsp;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A relation is a set of tuples. Each tuple is addressable by column
 *
 */
public class Relation {
  private final List<Row> rows;
  private final ColumnName[] columns;
  private final Map<ColumnName, Integer> columnPositionMap;
  private final Index[] indexes;

  public Relation(Stream<Row> rows, ColumnName... columns) {
    this.rows = rows.collect(Collectors.toList());
    this.columns = columns;
    columnPositionMap = new HashMap<>(columns.length + 1, 1);
    for (int i = 0; i < columns.length; i++) {
      columnPositionMap.put(columns[i], i);
    }
    indexes = new Index[columns.length];
  }

  public static class Value {
    private final ColumnName column;
    private final int value;
    public Value(ColumnName column, int value) {
      this.column = column;
      this.value = value;
    }

    @Override
    public String toString() {
      return "Value{" +
          "column=" + column +
          ", value=" + value +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Value value1 = (Value) o;
      return value != value1.value &&
          column != value1.column;
    }

    @Override
    public int hashCode() {
      return Objects.hash(column, value);
    }
  }

  private int getColumnIndex(ColumnName column) {
    return columnPositionMap.get(column);
  }

  public Stream<List<Integer>> selectWhere(List<ColumnName> selectCols, List<Value> whereVals) {
    return rows.stream().filter(row -> {
      for (Value v : whereVals) {
        if (row.get(getColumnIndex(v.column)) != v.value) {
          return false;
        }
      }
      return true;
    }).map(row -> selectCols.stream().map(col -> row.get(getColumnIndex(col))).collect(Collectors.toList()));
  }

  public <T> Collection<Integer> projectColumn(ColumnName column,
                                           Supplier<Collection<Integer>> collectionFactory) {
    return rows.stream().map(row -> row.get(getColumnIndex(column))).
        collect(Collectors.toCollection(collectionFactory));
  }

  public Collection<Integer> projectColumn(ColumnName column) {
    return projectColumn(column, ArrayDeque::new);
  }

  public Collection<Integer> joinThenProject(ColumnName joinColumn, Set<Integer> domain,
                                             ColumnName projectColumn,
                                             Supplier<Collection<Integer>> collectionFactory) {
    return rows.stream().
        filter(row -> domain.contains(row.get(getColumnIndex(joinColumn)))).
        map(row -> row.get(getColumnIndex(projectColumn))).
        collect(Collectors.toCollection(collectionFactory));
  }

  public Collection<Integer> joinThenProject(ColumnName joinColumn, Set<Integer> domain,
                                             ColumnName projectColumn) {
    return joinThenProject(joinColumn, domain, projectColumn, ArrayDeque::new);
  }



  @Override
  public String toString() {
    return "Relation{" +
        "rows=" + rows +
        ", columns=" + Arrays.toString(columns) +
        '}';
  }

  private static class Index {
//    private final Map<Integer, Collection<Row>> index;
    private Index(int columnIndex) {

    }
  }
}
