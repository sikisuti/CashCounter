package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.view.model.ObservableDailyBalance;
import org.siki.cashcounter.view.model.ObservableMonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
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
              (observable, oldValue, newValue) -> {
                int newBalance = newValue.intValue() + actDailyBalance.calculateDailySpent();
                if (actDailyBalance.isNotReviewed()) {
                  newBalance += getDayAverage(actDailyBalance.getDate());
                }

                actDailyBalance.setBalance(newBalance);
              });
    }
  }

  public int getDayAverage(LocalDate date) {
    var averageSum = 0;
    // Consider the last 6 months
    for (int i = -6; i < 0; i++) {
      var weeklyAverage = 0;

      List<ObservableDailyBalance> allDailyBalances =
          getObservableMonthlyBalances().stream()
              .flatMap(mb -> mb.getObservableDailyBalances().stream())
              .collect(Collectors.toList());
      // Check if data exists in the past
      if (allDailyBalances.get(0).getDate().compareTo(date.plusMonths(i).minusDays(4)) <= 0) {
        int index = -1;
        // Search the day in the past
        while (!allDailyBalances.get(++index).getDate().equals(date.plusMonths(i)))
          ;
        var correctionSum = 0;

        // Summarize the corredtions of the week
        for (int j = -3; j <= 3; j++) {
          correctionSum += allDailyBalances.get(index + j).getTotalCorrections();
        }

        weeklyAverage =
            Math.round(
                (allDailyBalances.get(index + 3).getBalance()
                        - correctionSum
                        - allDailyBalances.get(index - 4).getBalance())
                    / 7f);
      } else {
        throw new RuntimeException("Not enough past data");
      }
      averageSum += weeklyAverage;
    }

    return Math.round(averageSum / 6f);
  }

  public void save() throws IOException {
    dataManager.save();
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
            .distinct()
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
