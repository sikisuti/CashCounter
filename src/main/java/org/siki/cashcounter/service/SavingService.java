package org.siki.cashcounter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.model.Saving;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
public class SavingService {
  private final ConfigurationManager configurationManager;
  private final ObjectMapper objectMapper;

  public void loadSavingsFromFile(List<DailyBalance> dailyBalances) {
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
        dailyBalances.stream()
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
