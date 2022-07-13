package org.siki.cashcounter.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.repository.DataSourceRaw;
import org.siki.cashcounter.service.SavingService;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class LoadDataTask extends Task<DataSourceRaw> {
  private final ConfigurationManager configurationManager;
  private final DataManager dataManager;
  private final ObjectMapper objectMapper;
  private final SavingService savingService;

  @Override
  public DataSourceRaw call() throws Exception {
    var dataSourceRaw = loadDataFromFile();
    savingService.loadSavingsFromFile(
        dataSourceRaw.getMonthlyBalances().stream()
            .flatMap(mb -> mb.getDailyBalances().stream())
            .collect(Collectors.toList()));
    return dataSourceRaw;
  }

  private DataSourceRaw loadDataFromFile() throws IOException {
    var dataPath = configurationManager.getStringProperty("DataPath").orElseThrow();
    log.info("Loading data from " + dataPath);
    try (var inputStream = new FileInputStream(dataPath)) {
      updateTitle("Loading data file");
      var dataSourceRaw = objectMapper.readValue(inputStream, DataSourceRaw.class);
      updateTitle("Wire dependencies");
      wireDependencies(dataSourceRaw);
      updateTitle(null);
      log.info(dataSourceRaw.getMonthlyBalances().size() + " months loaded");
      return dataSourceRaw;
    } catch (IOException e) {
      log.error("Unable to load data file " + dataPath, e);
      throw e;
    }
  }

  private void wireDependencies(DataSourceRaw dataSourceRaw) {
    var allDailyBalances =
        dataSourceRaw.getMonthlyBalances().stream()
            .flatMap(mb -> mb.getDailyBalances().stream())
            .collect(Collectors.toList());
    allDailyBalances.get(0).setDataManager(dataManager);
    for (var i = 1; i < allDailyBalances.size(); i++) {
      updateProgress(i, allDailyBalances.size());
      var actDailyBalance = allDailyBalances.get(i);
      var prevDailyBalance = allDailyBalances.get(i - 1);
      actDailyBalance.setPrevDailyBalance(prevDailyBalance);
      actDailyBalance.setDataManager(dataManager);
      prevDailyBalance
          .balanceProperty()
          .addListener((observable, oldValue, newValue) -> actDailyBalance.updateBalance());
    }
  }
}
