package org.siki.cashcounter.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.StringProperty;

public class ObservableCorrection {
  private LongProperty id;
  private IntegerProperty amount;
  private StringProperty comment;
  private StringProperty type;
  private ObservableDailyBalance dailyBalance;
  private BooleanProperty paired;
  private ObservableAccountTransaction pairedTransaction;
  private LongProperty pairedTransactionId;
}
