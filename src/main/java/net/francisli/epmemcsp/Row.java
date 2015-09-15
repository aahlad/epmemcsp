package net.francisli.epmemcsp;

import java.util.Arrays;

public final class Row {
  private final int[] values;
  public Row(int... values) {
    this.values = values;
  }

  public int get(int index) {
    return values[index];
  }

  @Override
  public String toString() {
    return "Row{" +
        "values=" + Arrays.toString(values) +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Row row = (Row) o;
    return Arrays.equals(values, row.values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }
}
