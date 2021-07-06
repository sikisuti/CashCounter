package org.siki.cashcounter.view.dialog;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompareRow {
  String category;
  int predictedAmount;
  int amount;
  boolean bold;

  public int getDifference() {
    return amount - predictedAmount;
  }
}
