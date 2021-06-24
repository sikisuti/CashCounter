package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Correction implements Externalizable {

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

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(getId());
    out.writeObject(getType());
    out.writeInt(getAmount());
    out.writeObject(getComment());
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    setId(in.readLong());
    setType((String) in.readObject());
    setAmount(in.readInt());
    setComment((String) in.readObject());
  }
}
