package org.siki.cashcounter.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

@Data
@NoArgsConstructor
public class PredictedCorrection {
  String category;
  String subCategory;
  DayOfWeek dayOfWeek;
  Month month;
  Integer monthDay;
  Integer day;
  LocalDate startDate;
  LocalDate endDate;
  Integer amount;

  public void setMonth(int value) {
    month = Month.of(value);
  }
}
