package org.siki.cashcounter.view.dialog.monthlyinfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CorrectionDetailsRow {
  String comment;
  int amount;

  public void addAmount(int value) {
    amount += value;
  }
}
