package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Correction {

  private long id;
  private int amount;
  private String comment;
  private String type;
  private long pairedTransactionId;

  @JsonIgnore
  public boolean isPaired() {
    return pairedTransactionId != 0;
  }

  @JsonIgnore
  public boolean isNotPaired() {
    return !isPaired();
  }
}
