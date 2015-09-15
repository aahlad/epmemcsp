package net.francisli.epmemcsp;

import java.util.Objects;

final class Id {
  private final String name;
  public Id(String name) {
    this.name = name;
  }
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "<" + name + ">";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Id id = (Id) o;
    return Objects.equals(name, id.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}