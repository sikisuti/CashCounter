package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.siki.cashcounter.model.Correction;

public class ObservableCorrection {
  private LongProperty idProperty;
  private IntegerProperty amountProperty;
  private StringProperty commentProperty;
  private StringProperty typeProperty;
  private ObservableDailyBalance observableDailyBalance;
  private BooleanProperty pairedProperty;
  private ObservableAccountTransaction pairedTransaction;
  private LongProperty pairedTransactionIdProperty;

  private Correction correction;

  public IntegerProperty amountProperty() {
    return amountProperty;
  }

  public StringProperty commentProperty() {
    return commentProperty;
  }

  public StringProperty typeProperty() {
    return typeProperty;
  }

  public BooleanProperty pairedProperty() {
    return pairedProperty;
  }

  public ObservableAccountTransaction getPairedTransaction() {
    return pairedTransaction;
  }

  public LongProperty pairedTransactionIdProperty() {
    return pairedTransactionIdProperty;
  }

  public Correction getCorrection() {
    return correction;
  }

  public static ObservableCorrection of(Correction correction) {
    var observableCollection = new ObservableCorrection();
    observableCollection.correction = correction;
    observableCollection.idProperty = new SimpleLongProperty(correction.getId());
    observableCollection.amountProperty = new SimpleIntegerProperty(correction.getAmount());
    observableCollection.commentProperty = new SimpleStringProperty(correction.getComment());
    observableCollection.typeProperty = new SimpleStringProperty(correction.getType());
    observableCollection.pairedProperty = new SimpleBooleanProperty(correction.isPaired());
    observableCollection.pairedTransactionIdProperty =
        new SimpleLongProperty(correction.getPairedTransactionId());
    return observableCollection;
  }

  public void setPairedTransaction(ObservableAccountTransaction transaction) {
    pairedTransaction = transaction;
    pairedProperty.set(transaction != null);
    pairedTransactionIdProperty.set(transaction != null ? transaction.idProperty().get() : 0);
  }
}
