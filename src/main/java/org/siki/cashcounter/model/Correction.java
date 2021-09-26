package org.siki.cashcounter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Data
@EqualsAndHashCode
public class Correction implements Externalizable {

  private long id;
  private final transient IntegerProperty amount;
  private final transient StringProperty comment;
  private final transient StringProperty type;
  private final transient LongProperty pairedTransactionId;
  private final transient BooleanProperty onlyMove;
  @JsonIgnore private transient AccountTransaction pairedTransaction;
  @JsonIgnore public final transient BooleanBinding paired;
  @JsonIgnore private DailyBalance parentDailyBalance;

  public int getAmount() {
    return amount.get();
  }

  public void setAmount(int value) {
    amount.set(value);
  }

  public IntegerProperty amountProperty() {
    return amount;
  }

  public String getComment() {
    return comment.get();
  }

  public void setComment(String value) {
    comment.set(value);
  }

  public StringProperty commentProperty() {
    return comment;
  }

  public String getType() {
    return type.get();
  }

  public void setType(String value) {
    type.set(value);
  }

  public StringProperty typeProperty() {
    return type;
  }

  public long getPairedTransactionId() {
    return pairedTransactionId.get();
  }

  public void setPairedTransactionId(long value) {
    pairedTransactionId.set(value);
  }

  public LongProperty pairedTransactionIdProperty() {
    return pairedTransactionId;
  }

  public boolean getOnlyMove() {
    return onlyMove.get();
  }

  public void setOnlyMove(boolean value) {
    onlyMove.set(value);
  }

  public BooleanProperty onlyMoveProperty() {
    return onlyMove;
  }

  public Correction() {
    amount = new SimpleIntegerProperty();
    comment = new SimpleStringProperty();
    type = new SimpleStringProperty();
    pairedTransactionId = new SimpleLongProperty();
    onlyMove = new SimpleBooleanProperty();
    paired =
        Bindings.createBooleanBinding(() -> getPairedTransactionId() != 0, pairedTransactionId);
  }

  @JsonIgnore
  public int getUnpairedAmount() {
    return pairedTransaction.getAmount() - getAmount();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(getId());
    out.writeObject(getType());
    out.writeInt(amount.get());
    out.writeObject(getComment());
    out.writeBoolean(getOnlyMove());
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    setId(in.readLong());
    setType((String) in.readObject());
    amount.set(in.readInt());
    setComment((String) in.readObject());
    setOnlyMove(in.readBoolean());
  }
}
