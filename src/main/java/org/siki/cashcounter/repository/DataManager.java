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
import java.io.IOException;
import java.util.List;

@Slf4j
public class DataManager {

  @Autowired private final ConfigurationManager configurationManager;

  private DataSource dataSource;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public DataManager(ConfigurationManager configurationManager) {
    this.configurationManager = configurationManager;
    objectMapper.registerModule(new JavaTimeModule());
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

  @Data
  static class DataSource {
    private List<MonthlyBalance> monthlyBalances;
    private List<CategoryMatchingRule> categoryMatchingRules;
  }
}
