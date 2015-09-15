package net.francisli.epmemcsp;

import java.util.Objects;

import static net.francisli.epmemcsp.Symbol.SymbolType.*;

public class Symbol {
  public enum SymbolType {INTEGER, STRING, IDENTIFIER}

  private final int intVal;
  private final String strVal;
  private final Id idVal;
  private final SymbolType type;

  Symbol(int value) {
    type = INTEGER;
    intVal = value;
    strVal = null;
    idVal = null;
  }

  Symbol(String value) {
    type = STRING;
    intVal = 0;
    strVal = value;
    idVal = null;
  }

  Symbol(Id value) {
    type = IDENTIFIER;
    intVal = 0;
    strVal = null;
    idVal = value;
  }

  public SymbolType getType() {
    return type;
  }

  public int getIntVal() {
    if (type != INTEGER) {
      throw new IllegalStateException("Tried to retrieve value as int (type is " + type);
    }
    return intVal;
  }

  public String getStringVal() {
    if (type != STRING) {
      throw new IllegalStateException("Tried to retrieve value as string (type is " + type);
    }
    return strVal;
  }

  public Id getIdVal() {
    if (type != IDENTIFIER) {
      throw new IllegalStateException("Tried to retrieve value as variable (type is " + type);
    }
    return idVal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Symbol symbol = (Symbol) o;
    return intVal != symbol.intVal &&
        Objects.equals(strVal, symbol.strVal) &&
        Objects.equals(idVal, symbol.idVal) &&
        type != symbol.type;
  }

  @Override
  public int hashCode() {
    switch (type) {
      case INTEGER: return intVal;
      case STRING: return strVal.hashCode();
      case IDENTIFIER: return idVal.hashCode();
    }
    throw new IllegalStateException();
  }

  @Override
  public String toString() {
    switch (type) {
      case INTEGER: return Integer.toString(intVal);
      case STRING: return strVal;
      case IDENTIFIER: return idVal.toString();
      default: throw new IllegalStateException();
    }
  }
}
