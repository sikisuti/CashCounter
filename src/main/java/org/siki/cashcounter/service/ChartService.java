package org.siki.cashcounter.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import lombok.AllArgsConstructor;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ChartService {
  @Autowired DataManager dataManager;

  public ObservableList<Series<LocalDate, Number>> getSeries() {
    ObservableList<Series<LocalDate, Number>> seriesList = FXCollections.observableArrayList();
    seriesList.add(
        new Series<>(
            "Lekötések",
            dataManager.getMonthlyBalances().stream()
                .flatMap(mb -> mb.getDailyBalances().stream())
                .map(db -> new Data<LocalDate, Number>(db.getDate(), db.getTotalSavings()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList))));
    seriesList.add(
        new Series<>(
            "Egyenleg",
            dataManager.getMonthlyBalances().stream()
                .flatMap(mb -> mb.getDailyBalances().stream())
                .map(db -> new Data<LocalDate, Number>(db.getDate(), db.balanceWithSaving.get()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList))));
    return seriesList;
  }
}
