package org.siki.cashcounter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode
public class Saving {
  private LocalDate from;
  private LocalDate to;
  private int amount;
  private String comment;
}
