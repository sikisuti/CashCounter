package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.time.LocalDate;

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
}
