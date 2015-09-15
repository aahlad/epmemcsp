package net.francisli.epmemcsp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import au.com.rete.db.*;
import au.com.rete.db.Sqlite3;

public class Utils {
  private static final Pattern WME_PATTERN =
      Pattern.compile("^\\(<([\\w-]+?)>\\s\\^([\\w-]+?)\\s([\\w<>-]+?)\\)$");

  private static String extractVarName(String var) {
      return var.substring(1, var.length() - 1);
  }

  private static class InnerWmeFormatException extends RuntimeException {
    private InnerWmeFormatException(String line) {
      super(line);
    }
  }

  public static class WmeFormatException extends Exception {
    private WmeFormatException(String msg) {
      super(msg);
    }
  }

  private static Wme readWme(String line) {
    Matcher m = WME_PATTERN.matcher(line);
    if (!m.matches()) {
      throw new InnerWmeFormatException(line);
    }
    Id id = new Id(m.group(1));
    String attr = m.group(2);
    String val = m.group(3);
    Wme wm;
    try {
      wm = new Wme(id, attr, Integer.parseInt(val));
    } catch (NumberFormatException e) {
      if (val.charAt(0) == '<' && val.charAt(val.length() - 1) == '>') {
        wm = new Wme(id, attr, new Id(extractVarName(val)));
      } else {
        wm = new Wme(id, attr, val);
      }
    }

    return wm;
  }

  public static Set<Wme> readQuery(String path) throws IOException, WmeFormatException {
    try {
      return Files.lines(Paths.get(path)).
          map(Utils::readWme).collect(Collectors.toSet());
    } catch (InnerWmeFormatException e) {
      throw new WmeFormatException(e.getMessage());
    }
  }
}
