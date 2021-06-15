package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DataForViewService {

  @Autowired private final DataManager dataManager;

  private ObservableList<String> categories;
  private ObservableList<String> correctionTypes;
  private ObservableList<ObservableMonthlyBalance> observableMonthlyBalances;

  public ObservableList<ObservableMonthlyBalance> getObservableMonthlyBalances() {
    if (observableMonthlyBalances == null) {
      observableMonthlyBalances =
          dataManager.getMonthlyBalances().stream()
              .filter(mb -> mb.getYearMonth().isAfter(YearMonth.now().minusYears(1)))
              .map(ObservableMonthlyBalance::of)
              .collect(Collectors.toCollection(FXCollections::observableArrayList));
      wireDependencies();
    }

    return observableMonthlyBalances;
  }

  private void wireDependencies() {
    var allDailyBalances =
        observableMonthlyBalances.stream()
            .flatMap(mb -> mb.getObservableDailyBalances().stream())
            .collect(Collectors.toList());
    for (var i = 1; i < allDailyBalances.size(); i++) {
      var actDailyBalance = allDailyBalances.get(i);
      var prevDailyBalance = allDailyBalances.get(i - 1);
      prevDailyBalance
          .balanceProperty()
          .addListener(
              (observable, oldValue, newValue) ->
                  actDailyBalance.setBalance(
                      newValue.intValue() + actDailyBalance.getSumTransactions()));
    }
  }

  public ObservableList<String> getAllCorrectionTypes() {
    if (correctionTypes == null) {
      correctionTypes = FXCollections.observableArrayList();
    }

    if (correctionTypes.isEmpty()) {
      collectCorrectionTypes();
    }

    return correctionTypes;
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

  private void collectCorrectionTypes() {
    correctionTypes.addAll(
        dataManager.getMonthlyBalances().stream()
            .flatMap(
                mb ->
                    mb.getDailyBalances().stream()
                        .flatMap(db -> db.getCorrections().stream().map(Correction::getType)))
            .collect(Collectors.toList()));
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
