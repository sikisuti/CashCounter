package org.siki.cashcounter.service;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class CategoryService {
  private static int RANGE = -730;

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

  public ObservableList<Series<LocalDate, Number>> getCategoryChart(String category) {
    return FXCollections.observableArrayList(
        new Series<>(
            category,
            IntStream.range(RANGE, 0)
                .mapToObj(dayOffset -> getDayData(category, dayOffset, sum()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList))));
  }

  private Data<LocalDate, Number> getDayData(
      String category, int dayOffset, ToDoubleFunction<IntStream> calculator) {
    return new Data<>(
        LocalDate.now().plusDays(dayOffset), getDayAverage(category, dayOffset, calculator));
  }

  private double getDayAverage(
      String category, int dayOffset, ToDoubleFunction<IntStream> calculator) {
    long averageRange =
        configurationManager.getIntegerProperty(category + " average range").orElse(30);

    var dateRange =
        dataManager.getAllDailyBalances().stream()
            .filter(
                db ->
                    db.getDate().isAfter(LocalDate.now().plusDays(dayOffset - averageRange))
                        && db.getDate().isBefore(LocalDate.now().plusDays(dayOffset)))
            .collect(Collectors.toList());

    var fromCorrections =
        dateRange.stream()
            .flatMap(db -> db.getCorrections().stream())
            .filter(c -> category.equalsIgnoreCase(c.getType()))
            .mapToInt(Correction::getAmount);

    var fromTransactions =
        dateRange.stream()
            .flatMap(db -> db.getTransactions().stream())
            .filter(t -> category.equalsIgnoreCase(t.getCategory()))
            .mapToInt(AccountTransaction::getUnpairedAmount);

    var dayAmounts = IntStream.concat(fromCorrections, fromTransactions);

    var temp =
        dateRange.stream()
            .collect(
                Collectors.toMap(
                    dailyBalance -> dailyBalance.getDate(),
                    dailyBalance ->
                        dailyBalance.getTransactions().stream()
                            .mapToInt(t -> t.getUnpairedAmount())
                            .average()));

    return calculator.applyAsDouble(dayAmounts);
  }

  private ToDoubleFunction<IntStream> sum() {
    return dayAmounts -> (double) dayAmounts.sum();
  }
}
