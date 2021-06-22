package org.siki.cashcounter.model.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ListToObservableConverter<T> extends StdConverter<List<T>, ObservableList<T>> {
  @Override
  public ObservableList<T> convert(List<T> accountTransactions) {
    return FXCollections.observableArrayList(accountTransactions);
  }
}
