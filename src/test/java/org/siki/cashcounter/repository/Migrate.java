package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
  @Test
  @Disabled
  void migrate() throws Exception {
    String sourcePath = "C:\\GoogleDrive\\CashCount\\data.jsn";
    String destinationPath = "c:\\Project Sources\\CashCounter\\data.json";

    List<String> dailyBalanceSources = Files.readAllLines(Paths.get(sourcePath));

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<DailyBalance> dailyBalances = new ArrayList<>();

    for (String dailyBalanceSource : dailyBalanceSources) {
      dailyBalances.add(objectMapper.readValue(dailyBalanceSource, DailyBalance.class));
    }

    log.info(dailyBalances.size() + " DailyBalance read");

    Map<YearMonth, List<DailyBalance>> months =
        dailyBalances.stream().collect(Collectors.groupingBy(db -> YearMonth.from(db.getDate())));
    DataSource dataSource = new DataSource();
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

    objectMapper.writeValue(Paths.get(destinationPath).toFile(), dataSource);
  }
}
