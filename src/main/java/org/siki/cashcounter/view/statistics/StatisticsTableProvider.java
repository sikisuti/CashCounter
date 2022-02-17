package org.siki.cashcounter.view.statistics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.repository.DataManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class StatisticsTableProvider {
  private final DataManager dataManager;
  private final ConfigurationManager configurationManager;
  public static final int STAT_START_OFFSET_MONTH = -12;
  public static final int STAT_END_OFFSET_MONTH = 12;

  public ObservableList<CategoryRow> getStatistics() {
    var data = new ArrayList<CategoryRow>();
    var categoryMap = collectSpentFromCorrections();

    collectSpentFromTransactions()
        .forEach(
            (category, transactionMap) ->
                ofNullable(categoryMap.get(category))
                    .ifPresentOrElse(
                        addTransactionsToCategory(transactionMap),
                        () -> categoryMap.put(category, transactionMap)));

    categoryMap.forEach(((category, valueMap) -> data.add(createCategoryRow(category, valueMap))));

    return FXCollections.observableArrayList(data);
  }

  private Consumer<Map<YearMonth, Integer>> addTransactionsToCategory(
      Map<YearMonth, Integer> transactionMap) {
    return categoryValueMap -> transactionMap.forEach(addTransactionToCategory(categoryValueMap));
  }

  private BiConsumer<YearMonth, Integer> addTransactionToCategory(
      Map<YearMonth, Integer> categoryValueMap) {
    return (transactionYearMonth, transactionAmount) ->
        categoryValueMap.put(
            transactionYearMonth,
            categoryValueMap.getOrDefault(transactionYearMonth, 0) + transactionAmount);
  }

  private Map<String, Map<YearMonth, Integer>> collectSpentFromTransactions() {
    return dataManager.getAllDailyBalances().stream()
        .filter(
            db ->
                db.getDate().isAfter(LocalDate.now().plusMonths(STAT_START_OFFSET_MONTH - 1L))
                    && db.getDate()
                        .isBefore(LocalDate.now().plusMonths(STAT_END_OFFSET_MONTH + 1L)))
        .flatMap(db -> db.getTransactions().stream())
        .collect(
            Collectors.groupingBy(
                AccountTransaction::getCategory,
                Collectors.groupingBy(
                    t -> YearMonth.from(t.getDate()),
                    Collectors.summingInt(AccountTransaction::getUnpairedAmount))));
  }

  private Map<String, Map<YearMonth, Integer>> collectSpentFromCorrections() {
    return dataManager.getAllDailyBalances().stream()
        .filter(
            db ->
                db.getDate().isAfter(LocalDate.now().plusMonths(STAT_START_OFFSET_MONTH - 1L))
                    && db.getDate()
                        .isBefore(LocalDate.now().plusMonths(STAT_END_OFFSET_MONTH + 1L)))
        .flatMap(db -> db.getCorrections().stream())
        .collect(
            Collectors.groupingBy(
                Correction::getType,
                Collectors.groupingBy(
                    c -> YearMonth.from(c.getParentDailyBalance().getDate()),
                    Collectors.summingInt(Correction::getAmount))));
  }

  private CategoryRow createCategoryRow(String categoryName, Map<YearMonth, Integer> valueMap) {
    var row = new CategoryRow();
    row.setCategoryName(categoryName);
    valueMap.forEach(row::putCategoryValue);

    return row;
  }

  private Map<String, String> createCategoryMap() {
    var row = new LinkedHashMap<String, String>();
    row.put("categoryName", "category1");
    row.put("2022.1.", "100 000 Ft");

    return row;
  }
}
