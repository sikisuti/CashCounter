package org.siki.cashcounter.view.statistics;

import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.repository.DataManager;

import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class StatisticsProvider {
  private static final int AVERAGE_OF_MONTHS = 12;

  private final DataManager dataManager;
  private final ConfigurationManager configurationManager;

  private final SortedMap<LocalDate, StatisticsMonthModel> statisticsMonthModels = new TreeMap<>();
  List<String> consideredCategories;

  public SortedMap<LocalDate, StatisticsMonthModel> getStatistics() {
    var dailyBalances = dataManager.getAllDailyBalances();
    initializeModels();
    setEndBalances(dailyBalances);

    getCorrections(dailyBalances);
    setGeneralSpent();

    consideredCategories =
        Arrays.asList(configurationManager.getStringProperty("ConsideredCategories").split(","));
    getConsideredTransactions(dailyBalances);
    getRestTransactions(dailyBalances);

    setCashSpent();

    setBackwardCellReferences();
    fillEmptyStatisticsModels();
    calculateAverages();

    return statisticsMonthModels;
  }

  private void initializeModels() {
    var date = LocalDate.now().withDayOfMonth(1).minusYears(2).minusMonths(1);
    StatisticsMonthModel previousMonthModel = null;

    do {
      var actMonthModel = new StatisticsMonthModel(previousMonthModel);
      statisticsMonthModels.put(date, actMonthModel);
      previousMonthModel = actMonthModel;
      date = date.plusMonths(1);
    } while (date.isBefore(LocalDate.now().withDayOfMonth(1).plusYears(1)));
  }

  private void setEndBalances(List<DailyBalance> dailyBalances) {
    var dateGroupedDailyBalances =
        dailyBalances.stream().collect(Collectors.groupingBy(db -> db.getDate().withDayOfMonth(1)));
    for (var monthDailyBalances : dateGroupedDailyBalances.entrySet()) {
      if (statisticsMonthModels.containsKey(monthDailyBalances.getKey())) {
        var lastMonthBalance =
            monthDailyBalances
                .getValue()
                .get(monthDailyBalances.getValue().size() - 1)
                .getBalance();
        statisticsMonthModels.get(monthDailyBalances.getKey()).setEndBalance(lastMonthBalance);
      }
    }
  }

  private void getCorrections(List<DailyBalance> dailyBalances) {
    var allCorrections =
        dailyBalances.stream()
            .flatMap(db -> db.getCorrections().stream())
            .collect(Collectors.toList());
    var dateGroupedCorrections =
        allCorrections.stream()
            .collect(
                Collectors.groupingBy(c -> c.getParentDailyBalance().getDate().withDayOfMonth(1)));

    var dateAndTypeGroupedCorrections =
        dateGroupedCorrections.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    e ->
                        e.getValue().stream().collect(Collectors.groupingBy(Correction::getType))));

    for (var monthCorrectionEntry : dateAndTypeGroupedCorrections.entrySet()) {
      for (var typeCorrectionEntry : monthCorrectionEntry.getValue().entrySet()) {
        if (statisticsMonthModels.containsKey(monthCorrectionEntry.getKey())) {
          var cellModel = new StatisticsCellModel();
          cellModel.putAllCorrections(typeCorrectionEntry.getValue());
          statisticsMonthModels
              .get(monthCorrectionEntry.getKey())
              .addCellModel(typeCorrectionEntry.getKey(), cellModel);
        }
      }
    }
  }

  private void setGeneralSpent() {
    for (var monthModelEntry : statisticsMonthModels.entrySet()) {
      if (monthModelEntry.getValue().getPreviousMonthModel() == null) {
        continue;
      }

      var allCorrections =
          monthModelEntry.getValue().getCellModels().values().stream()
              .mapToInt(StatisticsCellModel::getAmount)
              .sum();
      var cellModel = new StatisticsCellModel();
      var correctionToPut = new Correction();
      correctionToPut.setAmount(
          monthModelEntry.getValue().getEndBalance()
              - monthModelEntry.getValue().getPreviousMonthModel().getEndBalance()
              - allCorrections);
      correctionToPut.setComment("Máshová nem sorolható elemek");
      cellModel.putCorrection(correctionToPut);
      monthModelEntry.getValue().addCellModel("Általános", cellModel);
    }
  }

  private void getConsideredTransactions(List<DailyBalance> dailyBalances) {
    var allTransactions =
        dailyBalances.stream()
            .flatMap(db -> db.getTransactions().stream())
            .filter(
                t ->
                    consideredCategories.contains(t.getCategory())
                        && (!t.isPaired() || (t.isPaired() && t.getUnpairedAmount() != 0)));
    var dateGroupedTransactions =
        allTransactions.collect(Collectors.groupingBy(t -> t.getDate().withDayOfMonth(1)));

    var dateAndTypeGroupedTransactions =
        dateGroupedTransactions.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    e ->
                        e.getValue().stream()
                            .collect(Collectors.groupingBy(AccountTransaction::getCategory))));

    for (var monthTransactionEntry : dateAndTypeGroupedTransactions.entrySet()) {
      for (var typeTransactionEntry : monthTransactionEntry.getValue().entrySet()) {
        if (statisticsMonthModels.containsKey(monthTransactionEntry.getKey())) {
          var cellModel = new StatisticsCellModel();
          cellModel.putAllTransactions(typeTransactionEntry.getValue());
          statisticsMonthModels
              .get(monthTransactionEntry.getKey())
              .addCellModel("  -- " + typeTransactionEntry.getKey(), cellModel);
        }
      }
    }
  }

  private void getRestTransactions(List<DailyBalance> dailyBalances) {
    Stream<AccountTransaction> allTransactions =
        dailyBalances.stream()
            .map(DailyBalance::getTransactions)
            .flatMap(Collection::stream)
            .filter(
                t ->
                    !consideredCategories.contains(t.getCategory())
                        && !"Készpénzfelvét".equalsIgnoreCase(t.getCategory())
                        && (!t.isPaired() || (t.isPaired() && t.getUnpairedAmount() != 0)));
    Map<LocalDate, List<AccountTransaction>> dateGroupedTransactions =
        allTransactions.collect(Collectors.groupingBy(t -> t.getDate().withDayOfMonth(1)));

    for (Entry<LocalDate, List<AccountTransaction>> monthTransactions :
        dateGroupedTransactions.entrySet()) {
      if (statisticsMonthModels.containsKey(monthTransactions.getKey())) {
        var cellModel = new StatisticsCellModel();
        cellModel.putAllTransactions(monthTransactions.getValue());
        statisticsMonthModels
            .get(monthTransactions.getKey())
            .addCellModel("  -- Maradék", cellModel);
      }
    }
  }

  private void setCashSpent() {
    for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
      if (monthEntry.getValue().getPreviousMonthModel() == null
          || !monthEntry.getKey().isBefore(LocalDate.now().withDayOfMonth(1))) {
        continue;
      }

      int balanceDifference =
          monthEntry.getValue().getEndBalance()
              - monthEntry.getValue().getPreviousMonthModel().getEndBalance();
      int allCorrections =
          monthEntry.getValue().getCellModels().entrySet().stream()
              .filter(c -> !"Általános".equalsIgnoreCase(c.getKey()))
              .flatMap(e -> e.getValue().getCorrections().stream())
              .mapToInt(Correction::getAmount)
              .sum();
      int allTransactions =
          monthEntry.getValue().getCellModels().entrySet().stream()
              .flatMap(e -> e.getValue().getTransactions().stream())
              .mapToInt(AccountTransaction::getUnpairedAmount)
              .sum();
      int cashSpent = balanceDifference - allCorrections - allTransactions;
      var cellModel = new StatisticsCellModel();
      var correctionToPut = new Correction();
      correctionToPut.setAmount(cashSpent);
      correctionToPut.setComment("Készpénzköltés");
      cellModel.putCorrection(correctionToPut);
      monthEntry.getValue().addCellModel("  -- Készpénzköltés", cellModel);
    }
  }

  private void setBackwardCellReferences() {
    for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
      if (!statisticsMonthModels.containsKey(monthEntry.getKey().minusMonths(1))) {
        continue;
      }

      for (Entry<String, StatisticsCellModel> categoryEntry :
          monthEntry.getValue().getCellModels().entrySet()) {
        var previousStatisticsCellModel =
            statisticsMonthModels
                .get(monthEntry.getKey().minusMonths(1))
                .getCellModels()
                .get(categoryEntry.getKey());
        categoryEntry.getValue().setPreviousStatisticsModel(previousStatisticsCellModel);
      }
    }
  }

  private void fillEmptyStatisticsModels() {
    Set<String> allCorrectionTypes =
        statisticsMonthModels.values().stream()
            .map(statisticsMonthModel -> statisticsMonthModel.getCellModels().entrySet())
            .flatMap(Collection::stream)
            .map(Entry::getKey)
            .collect(Collectors.toSet());

    for (String type : allCorrectionTypes) {
      for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
        Map<String, StatisticsCellModel> monthTypes = monthEntry.getValue().getCellModels();
        if (!monthTypes.containsKey(type)) {
          monthTypes.put(type, new StatisticsCellModel());
        }
      }
    }
  }

  private void calculateAverages() {
    Map<LocalDate, StatisticsMonthModel> filteredMonthStatistics =
        statisticsMonthModels.entrySet().stream()
            .filter(
                e ->
                    e.getKey()
                        .plusMonths(AVERAGE_OF_MONTHS)
                        .isAfter(LocalDate.now().withDayOfMonth(1).minusMonths(3)))
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    Entry::getValue,
                    (v1, v2) -> {
                      throw new RuntimeException(
                          String.format("Duplicate key for values %s and %s", v1, v2));
                    },
                    TreeMap::new));
    for (Entry<LocalDate, StatisticsMonthModel> monthStatisticsEntry :
        filteredMonthStatistics.entrySet()) {
      calculateMonthAverages(monthStatisticsEntry);
    }
  }

  private void calculateMonthAverages(Entry<LocalDate, StatisticsMonthModel> monthStatisticsEntry) {
    for (Entry<String, StatisticsCellModel> statisticsEntry :
        monthStatisticsEntry.getValue().getCellModels().entrySet()) {
      long monthCount =
          statisticsMonthModels.entrySet().stream()
              .filter(
                  e ->
                      e.getKey()
                              .plusMonths(AVERAGE_OF_MONTHS)
                              .isAfter(monthStatisticsEntry.getKey())
                          && !e.getKey().isAfter(monthStatisticsEntry.getKey()))
              .count();
      if (monthCount != AVERAGE_OF_MONTHS) {
        return;
      }

      List<Integer> amounts =
          statisticsMonthModels.entrySet().stream()
              .filter(
                  e ->
                      e.getKey()
                              .plusMonths(AVERAGE_OF_MONTHS + 5L)
                              .isAfter(monthStatisticsEntry.getKey())
                          && !e.getKey().isAfter(monthStatisticsEntry.getKey()))
              .map(e -> e.getValue().getCellModels().entrySet())
              .flatMap(Collection::stream)
              .filter(e -> e.getKey().equals(statisticsEntry.getKey()))
              .mapToInt(e -> e.getValue().getAmount())
              .boxed()
              .collect(Collectors.toList());

      // calculate weighted average
      var amountSum = 0d;
      var divider = 0d;
      for (int i = 0; i < amounts.size(); i++) {
        amountSum += amounts.get(i) * (i + 1);
        divider += (i + 1);
      }

      Double averageDbl = divider != 0 ? (amountSum / divider) : 0;
      statisticsEntry.getValue().setAverage(averageDbl.intValue());
    }
  }
}
