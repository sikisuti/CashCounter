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
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class CategoryService {
  public static final int RANGE = -730;
  public static final int DEFAULT_AVERAGE_RANGE = 30;

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
    var count =
        getAmountsOfCategory(category, 0, -RANGE).values().stream().mapToLong(List::size).sum();

    ToIntFunction<List<Integer>> calculator;
    String calculatorName;
    if (count < Math.floor(-RANGE / 2d)) {
      calculator = avg();
      calculatorName = "avg";
    } else {
      calculator = sum();
      calculatorName = "sum";
    }

    long averageRange =
        configurationManager
            .getIntegerProperty(category + " average range")
            .orElse(DEFAULT_AVERAGE_RANGE);

    Map<String, Map<LocalDate, Number>> data = new HashMap<>();

    IntStream.range(RANGE, 1)
        .forEach(
            dayOffset -> {
              var dayData = getDayData(category, dayOffset, calculator, averageRange);
              dayData.forEach(
                  (subCategory, value) ->
                      ofNullable(data.get(subCategory))
                          .ifPresentOrElse(
                              c ->
                                  data.get(subCategory)
                                      .put(LocalDate.now().plusDays(dayOffset), value),
                              () -> {
                                var map = new HashMap<LocalDate, Number>();
                                map.put(LocalDate.now().plusDays(dayOffset), value);
                                data.put(subCategory, map);
                              }));
            });

    var categorySum = data.get(category).values().stream().mapToInt(Number::intValue).sum();

    var orderedData =
        data.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().values().stream().mapToInt(Number::intValue).sum()))
            .entrySet()
            .stream()
            .sorted(
                categorySum < 0
                    ? Map.Entry.comparingByValue()
                    : Collections.reverseOrder(Map.Entry.comparingByValue()))
            .limit(8)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

    var seriesList = FXCollections.observableArrayList(new ArrayList<Series<LocalDate, Number>>());

    data.forEach(
        (subCategory, seriesMap) -> {
          if (orderedData.contains(subCategory)) {
            seriesList.add(
                new Series<>(
                    subCategory
                        + (subCategory.equals(category)
                            ? " (" + calculatorName + "/" + averageRange + "days)"
                            : ""),
                    seriesMap.entrySet().stream()
                        .map(
                            seriesEntry -> new Data<>(seriesEntry.getKey(), seriesEntry.getValue()))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList))));
          }
        });

    return seriesList;
  }

  private Map<String, Integer> getDayData(
      String category, int dayOffset, ToIntFunction<List<Integer>> calculator, long averageRange) {

    var dayAmounts = getAmountsOfCategory(category, dayOffset, averageRange);
    var calculatedMap = new HashMap<String, Integer>();
    dayAmounts.forEach((key, value) -> calculatedMap.put(key, calculator.applyAsInt(value)));

    return calculatedMap;
  }

  private Map<String, List<Integer>> getAmountsOfCategory(
      String category, int dayOffset, long averageRange) {
    var dateRange =
        dataManager.getAllDailyBalances().stream()
            .filter(
                db ->
                    db.getDate().isAfter(LocalDate.now().plusDays(dayOffset - averageRange))
                        && db.getDate().isBefore(LocalDate.now().plusDays(dayOffset + 1L)))
            .collect(Collectors.toList());

    var fromCorrections =
        dateRange.stream()
            .flatMap(db -> db.getCorrections().stream())
            .filter(c -> category.equalsIgnoreCase(c.getType()))
            .collect(
                Collectors.groupingBy(
                    Correction::getComment,
                    Collectors.mapping(Correction::getAmount, Collectors.toList())));

    var fromTransactions =
        dateRange.stream()
            .flatMap(db -> db.getTransactions().stream())
            .filter(t -> category.equalsIgnoreCase(t.getCategory()))
            .collect(
                Collectors.groupingBy(
                    AccountTransaction::getOwner,
                    Collectors.mapping(
                        AccountTransaction::getUnpairedAmount, Collectors.toList())));

    return merge(fromCorrections, fromTransactions, category);
  }

  private Map<String, List<Integer>> merge(
      Map<String, List<Integer>> map1, Map<String, List<Integer>> map2, String category) {
    var mergedMap = new HashMap<String, List<Integer>>();
    Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
        .forEach(
            entry -> {
              ofNullable(mergedMap.get(category))
                  .ifPresentOrElse(
                      value -> value.addAll(entry.getValue()),
                      () -> mergedMap.put(category, new ArrayList<>(entry.getValue())));
              ofNullable(mergedMap.get(entry.getKey()))
                  .ifPresentOrElse(
                      value -> value.addAll(entry.getValue()),
                      () -> mergedMap.put(entry.getKey(), new ArrayList<>(entry.getValue())));
            });

    return mergedMap;
  }

  private ToIntFunction<List<Integer>> sum() {
    return dayAmounts -> dayAmounts.stream().mapToInt(Integer::intValue).sum();
  }

  private ToIntFunction<List<Integer>> avg() {
    return dayAmounts -> (int) dayAmounts.stream().mapToInt(Integer::intValue).average().orElse(0);
  }
}
