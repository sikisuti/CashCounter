package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.YearMonth;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DataForViewService {

  @Autowired private final DataManager dataManager;

  private ObservableList<String> categories;
  private ObservableList<ObservableMonthlyBalance> observableMonthlyBalances;

  public ObservableList<ObservableMonthlyBalance> getObservableMonthlyBalances() {
    if (observableMonthlyBalances == null) {
      observableMonthlyBalances =
          dataManager.getMonthlyBalances().stream()
              .filter(mb -> mb.getYearMonth().isAfter(YearMonth.now().minusYears(1)))
              .map(ObservableMonthlyBalance::of)
              .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    return observableMonthlyBalances;
  }

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
