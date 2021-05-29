package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import lombok.AllArgsConstructor;
import org.siki.cashcounter.model.DailyBalance;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class ChartService {
  @Autowired DataManager dataManager;

  public Series<LocalDate, Number> getBalances() {
    Stream<DailyBalance> dailyBalanceStream =
        dataManager.getMonthlyBalances().stream().flatMap(mb -> mb.getDailyBalances().stream());
    ObservableList<Data<LocalDate, Number>> data =
        dailyBalanceStream
            .map(db -> new Data<LocalDate, Number>(db.getDate(), db.getBalance()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
    return new Series<>(data);
  }
}
