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
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public class CategoryService {
  public static final int RANGE = -730;
  public static final int DEFAULT_AVERAGE_WEEK_RANGE = 4;
  // average is calculated in weeks range (28 days) for the smoother chart and we need to correct
  // the result to be closer to a month (30 days)
  public static final double MONTH_CORRECTION = (30d / 28d);

  @Autowired private final DataManager dataManager;
  @Autowired private final ConfigurationManager configurationManager;

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

  public ObservableList<Series<LocalDate, Number>> getCategoryChartData(String category) {
    var averageWeekRange =
        configurationManager
            .getIntegerProperty(category + " average range")
            .orElse(DEFAULT_AVERAGE_WEEK_RANGE);

    var movingAverage =
        IntStream.range(-730, 0)
            .mapToObj(
                offset ->
                    getDayAverage(
                        dataManager.getAllDailyBalances(), category, -offset, averageWeekRange))
            .toList();

    var averageData =
        movingAverage.stream()
            .map(a -> new Data<LocalDate, Number>(a.getDate(), a.getAvg()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

    return FXCollections.observableArrayList(
        new Series<>("%s (%d hetes átlag)".formatted(category, averageWeekRange), averageData));
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

    var oneMonthAverage =
        (int)
            Math.round(
                (fromCorrections + fromTransactions) / (avgWeekRange / (4 * MONTH_CORRECTION)));
    return new Tuple(endDay, oneMonthAverage);
  }

  @lombok.Data
  @AllArgsConstructor
  private static class Tuple {
    private LocalDate date;
    private int avg;
  }
}
