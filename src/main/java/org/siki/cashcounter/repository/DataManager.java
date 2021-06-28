package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.CategoryMatchingRule;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Slf4j
public class DataManager {

  @Autowired private final ConfigurationManager configurationManager;

  private DataSource dataSource;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public DataManager(ConfigurationManager configurationManager) {
    this.configurationManager = configurationManager;
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    loadDataFromFile();
  }

  public List<MonthlyBalance> getMonthlyBalances() {
    return dataSource.monthlyBalances;
  }

  private void loadDataFromFile() {
    var dataPath = configurationManager.getStringProperty("DataPath");
    try (var inputStream = new FileInputStream(dataPath)) {
      dataSource = objectMapper.readValue(inputStream, DataSource.class);
      wireDependencies();
    } catch (IOException e) {
      log.error("Unable to load data file " + dataPath, e);
    }
  }

  public List<CategoryMatchingRule> getCategoryMatchingRules() {
    return dataSource.categoryMatchingRules;
  }

  public void save() throws IOException {
    var dataPath = configurationManager.getStringProperty("DataPath");
    backupIfRequired(dataPath);

    try (var outputStream = new FileOutputStream(dataPath)) {
      objectMapper.writeValue(outputStream, dataSource);
    }
  }

  private List<DailyBalance> getAllDailyBalances() {
    return dataSource.monthlyBalances.stream()
        .flatMap(mb -> mb.getDailyBalances().stream())
        .collect(Collectors.toList());
  }

  private void wireDependencies() {
    var allDailyBalances = getAllDailyBalances();
    allDailyBalances.get(0).setDataManager(this);
    for (var i = 1; i < allDailyBalances.size(); i++) {
      var actDailyBalance = allDailyBalances.get(i);
      var prevDailyBalance = allDailyBalances.get(i - 1);
      actDailyBalance.setPrevDailyBalance(prevDailyBalance);
      actDailyBalance.setDataManager(this);
      prevDailyBalance
          .balanceProperty()
          .addListener((observable, oldValue, newValue) -> actDailyBalance.updateBalance());
    }
  }

  public int getDayAverage(LocalDate date) {
    var allDailyBalances = getAllDailyBalances();
    var averageSum = 0;
    // Consider the last 6 months
    for (int i = -6; i < 0; i++) {
      var weeklyAverage = 0;

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

  private void backupIfRequired(String dataPath) throws IOException {
    FileTime lastModifiedTime = Files.getLastModifiedTime(Paths.get(dataPath));
    var lastModifiedDate =
        LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault()).toLocalDate();
    if (!lastModifiedDate.equals(LocalDate.now())) {
      var backupPath = configurationManager.getStringProperty("BackupPath");
      if (Files.notExists(Paths.get(backupPath))) {
        Files.createDirectory(Paths.get(backupPath));
      }

      Files.copy(Paths.get(dataPath), Paths.get(backupPath + "/data_" + lastModifiedDate + ".jsn"));
    }
  }

  @Data
  static class DataSource {
    private List<MonthlyBalance> monthlyBalances;
    private List<CategoryMatchingRule> categoryMatchingRules;
  }
}
