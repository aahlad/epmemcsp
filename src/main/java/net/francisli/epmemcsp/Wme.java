package net.francisli.epmemcsp;

import java.util.Objects;

class Wme {
  private final Id id;
  private final String attr;
  private final Symbol value;
  private final int HASH_CODE;

  Wme(Id id, String attr, int value) {
    this.id = id;
    this.attr = attr;
    this.value = new Symbol(value);
    HASH_CODE = Objects.hash(id, attr, value);
  }

  Wme(Id id, String attr, String value) {
    this.id = id;
    this.attr = attr;
    this.value = new Symbol(value);
    HASH_CODE = Objects.hash(id, attr, value);
  }

  Wme(Id id, String attr, Id value) {
    this.id = id;
    this.attr = attr;
    this.value = new Symbol(value);
    HASH_CODE = Objects.hash(id, attr, value);
  }

  public Id getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Wme wme = (Wme) o;
    return Objects.equals(id, wme.id) &&
        Objects.equals(attr, wme.attr) &&
        Objects.equals(value, wme.value);
  }

  @Override
  public int hashCode() {
    return HASH_CODE;
  }

  public String getAttr() {
    return attr;
  }

  public Symbol getVal() {
    return value;
  }

  public String toString() {
    return String.format("(%s ^%s %s)", id, attr, value);
  }
}