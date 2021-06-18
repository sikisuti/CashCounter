package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.siki.cashcounter.model.Correction;

public class ObservableCorrection {
  private IntegerProperty amountProperty;
  private StringProperty commentProperty;
  private StringProperty typeProperty;
  private ObjectProperty<ObservableDailyBalance> observableDailyBalance;
  private BooleanProperty pairedProperty;
  private ObjectProperty<ObservableAccountTransaction> pairedTransaction;
  //  private LongProperty pairedTransactionIdProperty;

  @Getter private Correction correction;

  public int getAmount() {
    return amountProperty.get();
  }

  public void setAmount(int value) {
    amountProperty.set(value);
  }

  public IntegerProperty amountProperty() {
    return amountProperty;
  }

  public StringProperty commentProperty() {
    return commentProperty;
  }

  public String getType() {
    return typeProperty.get();
  }

  public StringProperty typeProperty() {
    return typeProperty;
  }

  public boolean isPaired() {
    return pairedProperty.get();
  }

  public boolean isNotPaired() {
    return !isPaired();
  }

  public BooleanProperty pairedProperty() {
    return pairedProperty;
  }

  public ObservableAccountTransaction getPairedTransaction() {
    return pairedTransaction.get();
  }

  //  public LongProperty pairedTransactionIdProperty() {
  //    return pairedTransactionIdProperty;
  //  }

  public static ObservableCorrection of(
      Correction correction, ObservableList<ObservableAccountTransaction> transactions) {
    var observableCollection = new ObservableCorrection();
    observableCollection.correction = correction;
    observableCollection.amountProperty = new SimpleIntegerProperty(correction.getAmount());
    observableCollection.commentProperty = new SimpleStringProperty(correction.getComment());
    observableCollection.typeProperty = new SimpleStringProperty(correction.getType());
    //    observableCollection.pairedProperty = new SimpleBooleanProperty(correction.isPaired());

    observableCollection.pairedTransaction =
        new SimpleObjectProperty<>(
            transactions.stream()
                .filter(t -> t.getId() == correction.getPairedTransactionId())
                .findFirst()
                .orElse(null));
    observableCollection.pairedProperty = new SimpleBooleanProperty();
    observableCollection.pairedProperty.bind(observableCollection.pairedTransaction.isNotNull());
    //    observableCollection.pairedTransactionIdProperty =
    //        new SimpleLongProperty(correction.getPairedTransactionId());
    return observableCollection;
  }

  public void setPairedTransaction(ObservableAccountTransaction transaction) {
    pairedTransaction.set(transaction);
    pairedProperty.set(transaction != null);
    //    pairedTransactionIdProperty.set(transaction != null ? transaction.getId() : 0);
  }
}
