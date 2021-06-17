package org.siki.cashcounter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.CategoryMatchingRule;
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
