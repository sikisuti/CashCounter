package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DataForViewService {

  @Autowired private final DataManager dataManager;

  private ObservableList<String> categories;

  public void save() throws IOException {
    dataManager.save();
  }

  public ObservableList<String> getAllCategories() {
    if (categories == null) {
      categories = FXCollections.observableArrayList();
    }

    if (categories.isEmpty()) {
      collectCategories();
    }

    return categories;
  }

  private void collectCategories() {
    categories.addAll(
        dataManager.getMonthlyBalances().stream()
            .flatMap(
                mb ->
                    mb.getDailyBalances().stream()
                        .flatMap(
                            db ->
                                db.getTransactions().stream().map(AccountTransaction::getCategory)))
            .collect(Collectors.toList()));
  }
}
