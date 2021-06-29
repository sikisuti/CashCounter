package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.siki.cashcounter.model.CategoryMatchingRule;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.siki.cashcounter.repository.DataManager.DataSource;

@Slf4j
class Migrate {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void init() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  @Disabled
  void migrate() throws Exception {
    String destinationPath = "c:\\Project Sources\\CashCounter\\data.json";

    DataSource dataSource = new DataSource();

    readDailyBalances(dataSource);
    readCategoryMatchingRules(dataSource);

    objectMapper.writeValue(Paths.get(destinationPath).toFile(), dataSource);
  }

  private void readDailyBalances(DataSource dataSource) throws Exception {
    String dailyBalancesSourcePath = "C:\\GoogleDrive\\CashCount\\data.jsn";

    List<DailyBalance> dailyBalances = new ArrayList<>();
    List<String> dailyBalanceSources = Files.readAllLines(Paths.get(dailyBalancesSourcePath));
    for (String dailyBalanceSource : dailyBalanceSources) {
      var dailyBalance = objectMapper.readValue(dailyBalanceSource, DailyBalance.class);
      if (dailyBalance.getSavings() == null) {
        dailyBalance.setSavings(FXCollections.observableArrayList());
      }

      if (dailyBalance.getCorrections() == null) {
        dailyBalance.setCorrections(FXCollections.observableArrayList());
      }

      if (dailyBalance.getTransactions() == null) {
        dailyBalance.setTransactions(FXCollections.observableArrayList());
      }

      dailyBalances.add(dailyBalance);
    }

    log.info(dailyBalances.size() + " DailyBalance read");

    Map<YearMonth, List<DailyBalance>> months =
        dailyBalances.stream().collect(Collectors.groupingBy(db -> YearMonth.from(db.getDate())));
    dataSource.setMonthlyBalances(new ArrayList<>());
    months.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(
            entry -> {
              MonthlyBalance monthlyBalance = new MonthlyBalance();
              monthlyBalance.setYearMonth(entry.getKey());
              monthlyBalance.setDailyBalances(entry.getValue());
              dataSource.getMonthlyBalances().add(monthlyBalance);
            });

    log.info(dataSource.getMonthlyBalances().size() + " MonthlyBalances created");
  }

  private void readCategoryMatchingRules(DataSource dataSource) throws Exception {
    String categoryMatchingRulesSourcePath = "c:\\Apps\\CashCount\\matchingRules.jsn";

    List<CategoryMatchingRule> categoryMatchingRules = new ArrayList<>();
    List<String> CategoryMatchingRuleSources =
        Files.readAllLines(Paths.get(categoryMatchingRulesSourcePath));
    for (String categoryMatchingRuleSource : CategoryMatchingRuleSources) {
      categoryMatchingRules.add(
          objectMapper.readValue(categoryMatchingRuleSource, CategoryMatchingRule.class));
    }

    log.info(categoryMatchingRules.size() + " CategoryMatchingRule read");
    dataSource.setCategoryMatchingRules(new ArrayList<>());
    for (CategoryMatchingRule categoryMatchingRule : categoryMatchingRules) {
      dataSource.getCategoryMatchingRules().add(categoryMatchingRule);
    }

    log.info(dataSource.getCategoryMatchingRules().size() + " CategoryMatchingRule created");
  }
}
