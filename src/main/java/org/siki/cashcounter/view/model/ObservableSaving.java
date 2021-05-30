package org.siki.cashcounter.view.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.NoArgsConstructor;
import org.siki.cashcounter.model.Saving;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ObservableSaving {
  private IntegerProperty amount;
  private StringProperty comment;

  private Saving saving;

  public static ObservableSaving of(Saving saving) {
    var observableSaving = new ObservableSaving();
    observableSaving.saving = saving;
    observableSaving.amount = new SimpleIntegerProperty(saving.getAmount());
    observableSaving.comment = new SimpleStringProperty(saving.getComment());
    return observableSaving;
  }
}
