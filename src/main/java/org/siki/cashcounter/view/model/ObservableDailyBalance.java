package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.DailyBalance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ObservableDailyBalance {
  private ObservableDailyBalance prevDailyBalance;
  private ObjectProperty<LocalDate> date;
  private IntegerProperty balance;
  private BooleanProperty predicted;
  private BooleanProperty reviewed;
  private IntegerProperty dailySpend;

  private ObservableList<ObservableSaving> savings;
  private ObservableList<ObservableCorrection> corrections;
  private ObservableList<ObservableAccountTransaction> transactions;

  private DailyBalance dailyBalance;

  public String getDateString() {
    return date.get().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
  }

  public BooleanProperty predictedProperty() {
    return predictedProperty()
  }

  public static ObservableDailyBalance of(DailyBalance dailyBalance) {
    var observableDailyBalance = new ObservableDailyBalance();
    observableDailyBalance.dailyBalance = dailyBalance;
    observableDailyBalance.date = new SimpleObjectProperty<>(dailyBalance.getDate());
    observableDailyBalance.balance = new SimpleIntegerProperty(dailyBalance.getBalance());
    observableDailyBalance.predicted = new SimpleBooleanProperty(dailyBalance.isPredicted());
    observableDailyBalance.reviewed = new SimpleBooleanProperty(dailyBalance.isReviewed());
    observableDailyBalance.dailySpend = new SimpleIntegerProperty(dailyBalance.getDailySpend());
    observableDailyBalance.savings =
        dailyBalance.getSavings().stream()
            .map(ObservableSaving::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    observableDailyBalance.corrections =
        dailyBalance.getCorrections().stream()
            .map(ObservableCorrection::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    observableDailyBalance.transactions =
        dailyBalance.getTransactions().stream()
            .map(ObservableAccountTransaction::of)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    return observableDailyBalance;
  }
}
