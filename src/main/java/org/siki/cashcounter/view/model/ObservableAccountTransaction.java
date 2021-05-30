package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.time.LocalDate;

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
}
