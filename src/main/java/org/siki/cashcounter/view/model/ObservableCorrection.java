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
  private LongProperty id;
  private IntegerProperty amount;
  private StringProperty comment;
  private StringProperty type;
  private ObservableDailyBalance dailyBalance;
  private BooleanProperty paired;
  private ObservableAccountTransaction pairedTransaction;
  private LongProperty pairedTransactionId;

  private Correction correction;

  public IntegerProperty amountProperty() {
    return amount;
  }

  public StringProperty commentProperty() {
    return comment;
  }

  public StringProperty typeProperty() {
    return type;
  }

  public BooleanProperty pairedProperty() {
    return paired;
  }

  public Correction getCorrection() {
    return correction;
  }

  public static ObservableCorrection of(Correction correction) {
    var observableCollection = new ObservableCorrection();
    observableCollection.correction = correction;
    observableCollection.id = new SimpleLongProperty(correction.getId());
    observableCollection.amount = new SimpleIntegerProperty(correction.getAmount());
    observableCollection.comment = new SimpleStringProperty(correction.getComment());
    observableCollection.type = new SimpleStringProperty(correction.getType());
    observableCollection.paired = new SimpleBooleanProperty(correction.isPaired());
    observableCollection.pairedTransactionId =
        new SimpleLongProperty(correction.getPairedTransactionId());
    return observableCollection;
  }
}
