package org.siki.cashcounter.view.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ObservableTransaction {
  //  private LongProperty idProperty;
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
  private ObservableList<Correction> pairedCorrections;
  private BooleanProperty possibleDuplicateProperty;
  private ObjectProperty<ObservableDailyBalance> observableDailyBalance;

  @Getter private AccountTransaction accountTransaction;

  public long getId() {
    return accountTransaction.getId();
  }

  public String getType() {
    return typeProperty.get();
  }

  public StringProperty typeProperty() {
    return typeProperty;
  }

  public LocalDate getDate() {
    return dateProperty.get();
  }

  public ObjectProperty<LocalDate> dateProperty() {
    return dateProperty;
  }

  public int getAmount() {
    return amountProperty.get();
  }

  public IntegerProperty amountProperty() {
    return amountProperty;
  }

  public String getOwner() {
    return ownerProperty.get();
  }

  public StringProperty ownerProperty() {
    return ownerProperty;
  }

  public String getComment() {
    return commentProperty.get();
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

  public boolean isPossibleDuplicate() {
    return possibleDuplicateProperty.get();
  }

  public void setPossibleDuplicate(boolean value) {
    possibleDuplicateProperty.set(value);
    accountTransaction.setPossibleDuplicate(value);
  }

  public BooleanProperty possibleDuplicateProperty() {
    return possibleDuplicateProperty;
  }

  public static ObservableTransaction of(AccountTransaction accountTransaction) {
    var observableAccountTransaction = new ObservableTransaction();
    observableAccountTransaction.accountTransaction = accountTransaction;
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
    //    observableAccountTransaction.counterProperty =
    //        new SimpleStringProperty(accountTransaction.getCounter());
    observableAccountTransaction.categoryProperty =
        new SimpleStringProperty(accountTransaction.getCategory());
    observableAccountTransaction.pairedCorrections = FXCollections.observableArrayList();
    observableAccountTransaction.pairedProperty = new SimpleBooleanProperty();
    observableAccountTransaction.pairedProperty.bind(
        Bindings.createBooleanBinding(
            () -> !observableAccountTransaction.pairedCorrections.isEmpty(),
            observableAccountTransaction.pairedCorrections));
    observableAccountTransaction.possibleDuplicateProperty =
        new SimpleBooleanProperty(accountTransaction.isPossibleDuplicate());
    return observableAccountTransaction;
  }

  public Integer getNotPairedAmount() {
    return getAmount() - pairedCorrections.stream().mapToInt(Correction::getAmount).sum();
  }

  public void addPairedCorrection(Correction correction) {
    if (!pairedCorrections.contains(correction)) {
      pairedCorrections.add(correction);
    }
  }

  public void removePairedCorrection(ObservableCorrection observableCorrection) {
    pairedCorrections.remove(observableCorrection);
  }

  public boolean isValid() {
    return accountTransaction.isValid();
  }

  public boolean similar(ObservableTransaction other) {
    return accountTransaction.similar(other.accountTransaction);
  }
}
