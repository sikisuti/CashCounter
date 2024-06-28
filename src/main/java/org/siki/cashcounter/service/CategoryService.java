package org.siki.cashcounter.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.repository.DataManager;

@RequiredArgsConstructor
public class CategoryService {
  public static final int RANGE = -730;

  private final DataManager dataManager;

  private final StringProperty selectedCategory = new SimpleStringProperty();

  public String getSelectedCategory() {
    return selectedCategory.get();
  }

  public void setSelectedCategory(String selectedCategory) {
    this.selectedCategory.set(selectedCategory);
  }

  public StringProperty selectedCategoryProperty() {
    return selectedCategory;
  }

  public void setCategory(AccountTransaction accountTransaction) {
    dataManager.getCategoryMatchingRules().keySet().stream()
        .filter(
            c ->
                dataManager.getCategoryMatchingRules().get(c).stream()
                    .anyMatch(p -> dataManager.isCategoryMatch(accountTransaction, p)))
        .findFirst()
        .ifPresent(accountTransaction::setCategory);
  }

  public ObservableList<Series<LocalDate, Number>> getCategoryChartData(
      String category, int weekRange) {

    var movingAverage =
        IntStream.range(-730, 0)
            .mapToObj(
                offset ->
                    getDayAverage(dataManager.getAllDailyBalances(), category, -offset, weekRange))
            .toList();

    var averageData =
        movingAverage.stream()
            .map(a -> new Data<LocalDate, Number>(a.getDate(), a.getAvg()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

    return FXCollections.observableArrayList(
        new Series<>("%s (%d hetes átlag)".formatted(category, weekRange), averageData));
  }

  private Tuple getDayAverage(
      List<DailyBalance> dailyBalances, String category, int dayOffset, int avgWeekRange) {
    var startDay = LocalDate.now().minusWeeks(avgWeekRange).minusDays(dayOffset + 1L);
    var endDay = LocalDate.now().minusDays(dayOffset);
    var dbRange =
        dailyBalances.stream()
            .filter(db -> db.getDate().isAfter(startDay))
            .filter(db -> db.getDate().isBefore(endDay))
            .toList();

    var fromCorrections =
        dbRange.stream()
            .flatMap(db -> db.getCorrections().stream())
            .filter(c -> category.equalsIgnoreCase(c.getType()))
            .mapToInt(Correction::getAmount)
            .sum();

    var fromTransactions =
        dbRange.stream()
            .flatMap(db -> db.getTransactions().stream())
            .filter(t -> category.equalsIgnoreCase(t.getCategory()))
            .mapToInt(AccountTransaction::getUnpairedAmount)
            .sum();

    var oneMonthAverage = (fromCorrections + fromTransactions) / (avgWeekRange / 4);
    return new Tuple(endDay, oneMonthAverage);
  }

  @lombok.Data
  @AllArgsConstructor
  private static class Tuple {
    private LocalDate date;
    private int avg;
  }
}
