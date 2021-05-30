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
  private LongProperty id;
  private StringProperty type;
  private ObjectProperty<LocalDate> date;
  private IntegerProperty amount;
  private IntegerProperty balance;
  private StringProperty accountNumber;
  private StringProperty owner;
  private StringProperty comment;
  private StringProperty counter;
  private StringProperty category;
  private BooleanProperty paired;
  private ObservableList<ObservableCorrection> pairedCorrections;
  private BooleanProperty possibleDuplicate;
  private ObservableDailyBalance dailyBalance;

  private AccountTransaction accountTransaction;

  public static ObservableAccountTransaction of(AccountTransaction accountTransaction) {
    var observableAccountTransaction = new ObservableAccountTransaction();
    observableAccountTransaction.accountTransaction = accountTransaction;
    observableAccountTransaction.id = new SimpleLongProperty(accountTransaction.getId());
    observableAccountTransaction.type = new SimpleStringProperty(accountTransaction.getType());
    observableAccountTransaction.date = new SimpleObjectProperty<>(accountTransaction.getDate());
    observableAccountTransaction.amount = new SimpleIntegerProperty(accountTransaction.getAmount());
    observableAccountTransaction.balance =
        new SimpleIntegerProperty(accountTransaction.getBalance());
    observableAccountTransaction.accountNumber =
        new SimpleStringProperty(accountTransaction.getAccountNumber());
    observableAccountTransaction.owner = new SimpleStringProperty(accountTransaction.getOwner());
    observableAccountTransaction.comment =
        new SimpleStringProperty(accountTransaction.getComment());
    observableAccountTransaction.counter =
        new SimpleStringProperty(accountTransaction.getCounter());
    observableAccountTransaction.category =
        new SimpleStringProperty(accountTransaction.getCategory());
    observableAccountTransaction.paired = new SimpleBooleanProperty(accountTransaction.isPaired());
    observableAccountTransaction.possibleDuplicate =
        new SimpleBooleanProperty(accountTransaction.isPossibleDuplicate());
    return observableAccountTransaction;
  }
}
