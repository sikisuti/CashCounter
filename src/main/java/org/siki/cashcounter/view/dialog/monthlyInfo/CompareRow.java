package org.siki.cashcounter.view.dialog.monthlyInfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompareRow {
  String type;
  int predictedAmount;
  int amount;
  boolean bold;

  public int getDifference() {
    return amount - predictedAmount;
  }
}
