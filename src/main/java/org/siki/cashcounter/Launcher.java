package org.siki.cashcounter;

import java.util.Locale;

public class Launcher {
  public static void main(String[] args) {
    Locale.setDefault(new Locale("hu", "HU"));
    CashCounter.main(args);
  }
}
