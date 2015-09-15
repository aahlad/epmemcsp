package net.francisli.epmemcsp;

public class IntervalEndpoint implements Comparable<IntervalEndpoint> {
  public enum Type {START, END}
  private final int value;
  private final Type type;

  public IntervalEndpoint(int value, Type type) {
    this.value = value;
    this.type = type;
  }

  public int getValue() {
    return value;
  }

  public Type getType() {
    return type;
  }

  @Override
  public int compareTo(IntervalEndpoint e) {
    return value - e.getValue();
  }

  @Override
  public String toString() {
    return "IntervalEndpoint{" +
        "value=" + value +
        ", type=" + type +
        '}';
  }
}
