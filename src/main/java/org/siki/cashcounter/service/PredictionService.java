package org.siki.cashcounter.service;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.model.Correction;
import org.siki.cashcounter.model.MonthlyBalance;
import org.siki.cashcounter.model.PredictedCorrection;
import org.siki.cashcounter.repository.DataManager;

@AllArgsConstructor
@Slf4j
public class PredictionService {
  private final DataManager dataManager;
  private final DailyBalanceService dailyBalanceService;
  private final CorrectionService correctionService;
  private final SavingService savingService;

  public void replacePredictedCorrections(File sourceFile) {
    var futureMonthlyBalances = clearFutureMonthlyBalances();
    addPredictions(sourceFile, futureMonthlyBalances);
    bindDailyBalances(futureMonthlyBalances);
    savingService.loadSavingsFromFile(
        futureMonthlyBalances.stream()
            .flatMap(mb -> mb.getDailyBalances().stream())
            .collect(Collectors.toList()));

    dataManager.getMonthlyBalances().addAll(futureMonthlyBalances);
    futureMonthlyBalances.get(0).getDailyBalances().get(0).updateBalance();
  }

  private List<MonthlyBalance> clearFutureMonthlyBalances() {
    removeFutureMonthlyBalances();
    return dailyBalanceService.createMissingMonthlyBalancesUntil(YearMonth.now().plusYears(1));
  }

  private void removeFutureMonthlyBalances() {
    var monthlyBalancesToRemove =
        dataManager.getMonthlyBalances().stream()
            .filter(mb -> mb.getYearMonth().isAfter(YearMonth.now().plusMonths(1)))
            .collect(Collectors.toList());
    dataManager.getMonthlyBalances().removeAll(monthlyBalancesToRemove);
  }

  private void addPredictions(File sourceFile, List<MonthlyBalance> futureMonthlyBalances) {
    List<PredictedCorrection> pcList;
    pcList = loadPredictedCorrections(sourceFile.getAbsolutePath());
    fillPredictedCorrections(futureMonthlyBalances, pcList);
    storePredictions(futureMonthlyBalances, pcList);
  }

  private void bindDailyBalances(List<MonthlyBalance> futureMonthlyBalances) {
    var dailyBalances =
        futureMonthlyBalances.stream()
            .flatMap(mb -> mb.getDailyBalances().stream())
            .collect(Collectors.toList());
    for (int i = 1; i < dailyBalances.size(); i++) {
      var actDailyBalance = dailyBalances.get(i);
      var previousDailyBalance = dailyBalances.get(i - 1);
      previousDailyBalance
          .balanceProperty()
          .addListener(observable -> actDailyBalance.updateBalance());
    }

    var lastDailyBalance = dailyBalanceService.getLastDailyBalance();
    var firstNextDailyBalance = dailyBalances.get(0);
    lastDailyBalance
        .balanceProperty()
        .addListener(observable -> firstNextDailyBalance.updateBalance());
  }

  private List<PredictedCorrection> loadPredictedCorrections(String path) {
    List<PredictedCorrection> pcList = new ArrayList<>();
    var lineCnt = 0;

    try (var fileInputStream = new FileInputStream(path);
        var inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        var br = new BufferedReader(inputStreamReader)) {
      String line;
      while ((line = br.readLine()) != null) {
        lineCnt++;
        if (line.startsWith("#") || line.trim().isEmpty()) {
          continue;
        }

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        pcList.add(objectMapper.readValue(line, PredictedCorrection.class));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error reading line: " + lineCnt, e);
    }

    return pcList;
  }

  private void fillPredictedCorrections(
      List<MonthlyBalance> futureMonthlyBalances, List<PredictedCorrection> predictedCorrections) {
    predictedCorrections.forEach(
        pc -> {
          var dbSchedList =
              futureMonthlyBalances.stream()
                  .flatMap(mb -> mb.getDailyBalances().stream())
                  .filter(
                      db ->
                          db.getDate().isAfter(pc.getStartDate())
                              && db.getDate().isBefore(pc.getEndDate()))
                  .collect(Collectors.toList());

          if (pc.getDayOfWeek() != null) {
            dbSchedList.stream()
                .filter(db -> db.getDate().getDayOfWeek().equals(pc.getDayOfWeek()))
                .forEach(
                    db -> {
                      var correctionToAdd = new Correction();
                      correctionToAdd.setParentDailyBalance(db);
                      correctionToAdd.setId(correctionService.getNextCorrectionId());
                      correctionToAdd.setType(pc.getCategory());
                      correctionToAdd.setComment(pc.getSubCategory());
                      correctionToAdd.setAmount(pc.getAmount());
                      db.addCorrection(correctionToAdd);
                      db.setReviewed(false);
                    });
          } else if (pc.getDay() != null) {
            var found = false;
            for (int i = dbSchedList.size() - 1; i >= 0; i--) {
              if (dbSchedList.get(i).getDate().getDayOfMonth() == pc.getDay()) {
                found = true;
              }
              if (dbSchedList.get(i).getDate().getDayOfWeek().getValue() >= 1
                  && dbSchedList.get(i).getDate().getDayOfWeek().getValue() <= 5
                  && found) {
                var correctionToAdd = new Correction();
                correctionToAdd.setParentDailyBalance(dbSchedList.get(i));
                correctionToAdd.setId(correctionService.getNextCorrectionId());
                correctionToAdd.setType(pc.getCategory());
                correctionToAdd.setComment(pc.getSubCategory());
                correctionToAdd.setAmount(pc.getAmount());
                dbSchedList.get(i).addCorrection(correctionToAdd);
                dbSchedList.get(i).setReviewed(false);
                found = false;
              }
            }
          } else if (pc.getMonth() != null && pc.getMonthDay() != null) {
            var found = false;
            for (int i = dbSchedList.size() - 1; i >= 0; i--) {
              if (dbSchedList.get(i).getDate().getMonth().equals(pc.getMonth())
                  && dbSchedList.get(i).getDate().getDayOfMonth() == pc.getMonthDay()) {
                found = true;
              }
              if (dbSchedList.get(i).getDate().getDayOfWeek().getValue() >= 1
                  && dbSchedList.get(i).getDate().getDayOfWeek().getValue() <= 5
                  && found) {
                var correctionToAdd = new Correction();
                correctionToAdd.setParentDailyBalance(dbSchedList.get(i));
                correctionToAdd.setId(correctionService.getNextCorrectionId());
                correctionToAdd.setType(pc.getCategory());
                correctionToAdd.setComment(pc.getSubCategory());
                correctionToAdd.setAmount(pc.getAmount());
                dbSchedList.get(i).addCorrection(correctionToAdd);
                dbSchedList.get(i).setReviewed(false);
                found = false;
              }
            }
          }
        });
  }

  private void storePredictions(
      List<MonthlyBalance> futureMonthlyBalances, List<PredictedCorrection> predictedCorrections) {
    futureMonthlyBalances.forEach(
        mb -> {
          for (var prediction : predictedCorrections) {
            if (prediction.getMonth() == null
                || prediction.getMonth().equals(mb.getYearMonth().getMonth())) {
              if (prediction.getDayOfWeek() != null) {
                var count = mb.getDailyBalances().stream()
                        .filter(db -> db.getDate().isEqual(prediction.getStartDate()) || db.getDate().isAfter(prediction.getStartDate()))
                        .filter(db -> db.getDate().isBefore(prediction.getEndDate()))
                        .filter(db -> db.getDate().getDayOfWeek().equals(prediction.getDayOfWeek()))
                        .count();
                mb.addPrediction(
                        prediction.getCategory(),
                        prediction.getSubCategory(),
                        prediction.getAmount() * (int) count);
              } else {
                var date =
                    mb.getYearMonth()
                        .atDay(ofNullable(prediction.getDay()).orElse(prediction.getMonthDay()));
                if (date.isAfter(prediction.getStartDate())
                    && date.isBefore(prediction.getEndDate())) {
                  mb.addPrediction(
                      prediction.getCategory(),
                      prediction.getSubCategory(),
                      prediction.getAmount());
                }
              }
            }
          }
        });
  }

  public static int countDayOccurenceInMonth(DayOfWeek dow, YearMonth month) {
    LocalDate start = month.atDay(1).with(TemporalAdjusters.nextOrSame(dow));
    return (int) ChronoUnit.WEEKS.between(start, month.atEndOfMonth()) + 1;
  }
}
