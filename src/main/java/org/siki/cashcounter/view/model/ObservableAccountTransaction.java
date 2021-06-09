package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ObservableAccountTransaction {
  private LongProperty idProperty;
  private StringProperty typeProperty;
  private ObjectProperty<LocalDate> dateProperty;
  private IntegerProperty amountProperty;
  private IntegerProperty balanceProperty;
  private StringProperty accountNumberProperty;
  private StringProperty ownerProperty;
  private StringProperty commentProperty;
  private StringProperty counterProperty;
  private StringProperty categoryProperty;
  private BooleanProperty pairedProperty;
  private ObservableList<ObservableCorrection> observablePairedCorrections;
  private BooleanProperty possibleDuplicateProperty;
  private ObservableDailyBalance observableDailyBalance;

  private AccountTransaction accountTransaction;

  public LongProperty idProperty() {
    return idProperty;
  }

  public StringProperty typeProperty() {
    return typeProperty;
  }

  public IntegerProperty amountProperty() {
    return amountProperty;
  }

  public StringProperty ownerProperty() {
    return ownerProperty;
  }

  public StringProperty commentProperty() {
    return commentProperty;
  }

  public BooleanProperty pairedProperty() {
    return pairedProperty;
  }

  public StringProperty categoryProperty() {
    return categoryProperty;
  }

  public BooleanProperty possibleDuplicateProperty() {
    return possibleDuplicateProperty;
  }

  public static ObservableAccountTransaction of(AccountTransaction accountTransaction) {
    var observableAccountTransaction = new ObservableAccountTransaction();
    observableAccountTransaction.accountTransaction = accountTransaction;
    observableAccountTransaction.idProperty = new SimpleLongProperty(accountTransaction.getId());
    observableAccountTransaction.typeProperty =
        new SimpleStringProperty(accountTransaction.getType());
    observableAccountTransaction.dateProperty =
        new SimpleObjectProperty<>(accountTransaction.getDate());
    observableAccountTransaction.amountProperty =
        new SimpleIntegerProperty(accountTransaction.getAmount());
    observableAccountTransaction.accountNumberProperty =
        new SimpleStringProperty(accountTransaction.getAccountNumber());
    observableAccountTransaction.ownerProperty =
        new SimpleStringProperty(accountTransaction.getOwner());
    observableAccountTransaction.commentProperty =
        new SimpleStringProperty(accountTransaction.getComment());
    observableAccountTransaction.counterProperty =
        new SimpleStringProperty(accountTransaction.getCounter());
    observableAccountTransaction.categoryProperty =
        new SimpleStringProperty(accountTransaction.getCategory());
    observableAccountTransaction.pairedProperty =
        new SimpleBooleanProperty(accountTransaction.isPaired());
    observableAccountTransaction.possibleDuplicateProperty =
        new SimpleBooleanProperty(accountTransaction.isPossibleDuplicate());
    return observableAccountTransaction;
  }

  public Integer getNotPairedAmount() {
    return amountProperty.get()
        - observablePairedCorrections.stream().mapToInt(c -> c.amountProperty().get()).sum();
  }

  public void addPairedCorrection(ObservableCorrection observableCorrection) {
    accountTransaction.addPairedCorrection(observableCorrection.getCorrection());
    if (!observablePairedCorrections.contains(observableCorrection)) {
      observablePairedCorrections.add(observableCorrection);
    }

    pairedProperty.set(true);
  }

  public void removePairedCorrection(ObservableCorrection observableCorrection) {
    accountTransaction.removePairedCorrection(observableCorrection.getCorrection());
    observablePairedCorrections.remove(observableCorrection);
    if (observablePairedCorrections.isEmpty()) {
      pairedProperty.set(false);
    }
  }

  public boolean isValid() {
    return accountTransaction.isValid();
  }
}
