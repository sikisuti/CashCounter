package org.siki.cashcounter.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.Saving;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.repository.DataSourceRaw;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Slf4j
public class LoadDataTask extends Task<DataSourceRaw> {
  private final ConfigurationManager configurationManager;
  private final DataManager dataManager;
  private final ObjectMapper objectMapper;

  @Override
  public DataSourceRaw call() throws Exception {
    var dataSourceRaw = loadDataFromFile();
    loadSavingsFromFile(dataSourceRaw);
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

  private void loadSavingsFromFile(DataSourceRaw dataSourceRaw) {
    var savingsPath = configurationManager.getStringProperty("SavingStorePath").orElseThrow();
    try (var fileInputStream = new FileInputStream(savingsPath);
        var inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        var br = new BufferedReader(inputStreamReader)) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || line.trim().isEmpty()) {
          continue;
        }
        var saving = objectMapper.readValue(line, Saving.class);
        dataSourceRaw.getMonthlyBalances().stream()
            .flatMap(mb -> mb.getDailyBalances().stream())
            .filter(
                db ->
                    db.getDate().isAfter(saving.getFrom().minusDays(1))
                        && db.getDate().isBefore(ofNullable(saving.getTo()).orElse(LocalDate.MAX)))
            .forEach(db -> db.addSaving(saving));
      }
    } catch (IOException e) {
      log.error("Unable to load data file " + savingsPath, e);
    }
  }
}
