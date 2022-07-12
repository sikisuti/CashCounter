package org.siki.cashcounter.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.repository.converter.DataSourceMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Optional.ofNullable;

@Slf4j
public class DataManager {
  private final ConfigurationManager configurationManager;
  private final DataSourceMapper dataSourceMapper;

  private final DataSource dataSource = new DataSource();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public DataManager(ConfigurationManager configurationManager, DataSourceMapper dataSourceMapper) {
    this.configurationManager = configurationManager;
    this.dataSourceMapper = dataSourceMapper;

    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public void loadData(DataSourceRaw dataSourceRaw) {
    dataSourceMapper.fromRaw(dataSourceRaw, dataSource);
  }

  @JsonIgnore
  public ObservableList<MonthlyBalance> getMonthlyBalances() {
    return dataSource.getMonthlyBalances();
  }

  public Map<String, List<String>> getCategoryMatchingRules() {
    return dataSource.getCategoryMatchingRules();
  }

  public void addCategoryMatchingRule(String pattern, String category) {
    ofNullable(dataSource.getCategoryMatchingRules().get(category))
        .ifPresentOrElse(
            patterns -> patterns.add(pattern),
            () -> dataSource.getCategoryMatchingRules().put(category, List.of(pattern)));

    dataSource.getMonthlyBalances().stream()
        .flatMap(
            mb ->
                mb.getDailyBalances().stream()
                    .flatMap(
                        db ->
                            db.getTransactions().stream()
                                .filter(AccountTransaction::hasNoCategory)))
        .forEach(
            t -> {
              if (isCategoryMatch(t, pattern)) {
                t.setCategory(category);
              }
            });
  }

  public long getNumberOfTransactionsBy(String category) {
    return dataSource.getMonthlyBalances().stream()
        .flatMap(
            mb ->
                mb.getDailyBalances().stream()
                    .flatMap(
                        db ->
                            db.getTransactions().stream()
                                .filter(t -> category.equals(t.getCategory()))))
        .count();
  }

  public boolean isCategoryMatch(AccountTransaction transaction, String pattern) {
    return transaction.getComment().toLowerCase().contains(pattern.toLowerCase())
        || transaction.getType().toLowerCase().contains(pattern.toLowerCase())
        || transaction.getOwner().toLowerCase().contains(pattern.toLowerCase());
  }

  public void save() throws IOException {
    if (dataSource.getMonthlyBalances().isEmpty()) {
      return;
    }

    var dataPath = configurationManager.getStringProperty("DataPath").orElseThrow();
    backupIfRequired(dataPath);

    var rawData = dataSourceMapper.toRaw(dataSource);
    try (var outputStream = new FileOutputStream(dataPath)) {
      objectMapper.writeValue(outputStream, rawData);
    }
  }

  public List<DailyBalance> getAllDailyBalances() {
    return dataSource.getMonthlyBalances().stream()
        .flatMap(mb -> mb.getDailyBalances().stream())
        .collect(Collectors.toList());
  }

  private void backupIfRequired(String dataPath) throws IOException {
    FileTime lastModifiedTime = Files.getLastModifiedTime(Paths.get(dataPath));
    var lastModifiedDate =
        LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault()).toLocalDate();
    if (!lastModifiedDate.equals(LocalDate.now())) {
      var backupPath = configurationManager.getStringProperty("BackupPath").orElseThrow();
      if (Files.notExists(Paths.get(backupPath))) {
        Files.createDirectory(Paths.get(backupPath));
      }

      Files.copy(
          Paths.get(dataPath),
          Paths.get(backupPath + "/data_" + lastModifiedDate + ".jsn"),
          REPLACE_EXISTING);
    }
  }
}
