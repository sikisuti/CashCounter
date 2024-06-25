package org.siki.cashcounter.view;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.service.TaskExecutorService;
import org.siki.cashcounter.task.TaskFactory;
import org.siki.cashcounter.view.dialog.AlertFactory;
import org.siki.cashcounter.view.dialog.CategoriesDialog;
import org.siki.cashcounter.view.dialog.ExceptionDialog;
import org.siki.cashcounter.view.dialog.FileChooserFactory;
import org.siki.cashcounter.view.search.SearchDialog;

@SuppressWarnings("java:S110")
@Slf4j
public class MainMenuBar extends MenuBar {

  private final PredictionService predictionService;
  private final CategoriesDialog categoriesDialog;
  private final DataManager dataManager;
  private final DataForViewService dataForViewService;
  private final FileChooserFactory fileChooserFactory;
  private final AlertFactory alertFactory;
  private final TaskExecutorService taskExecutorService;
  private final TaskFactory taskFactory;

  public MainMenuBar(
      PredictionService predictionService,
      CategoriesDialog categoriesDialog,
      DataManager dataManager,
      DataForViewService dataForViewService,
      FileChooserFactory fileChooserFactory,
      AlertFactory alertFactory,
      TaskExecutorService taskExecutorService,
      TaskFactory taskFactory) {
    this.predictionService = predictionService;
    this.categoriesDialog = categoriesDialog;
    this.dataManager = dataManager;
    this.dataForViewService = dataForViewService;
    this.fileChooserFactory = fileChooserFactory;
    this.alertFactory = alertFactory;
    this.taskExecutorService = taskExecutorService;
    this.taskFactory = taskFactory;

    this.getMenus().addAll(fileMenu(), dataMenu(), correctionsMenu());
  }

  private Menu fileMenu() {
    return new Menu("Fájl", null, searchMenuItem(), saveMenuItem());
  }

  private Menu dataMenu() {
    return new Menu("Adat", null, importMenuItem(), categoriesMenuItem());
  }

  private Menu correctionsMenu() {
    return new Menu("Korrekciók", null, predictedCorrectionsMenuItem());
  }

  private MenuItem saveMenuItem() {
    var saveMenuItem = new MenuItem("Mentés");
    saveMenuItem.setOnAction(this::doSave);
    return saveMenuItem;
  }

  private MenuItem searchMenuItem() {
    var searchMenuItem = new MenuItem("Keresés");
    searchMenuItem.setOnAction(this::doSearch);
    return searchMenuItem;
  }

  private MenuItem importMenuItem() {
    var importMenuItem = new MenuItem("Importálás");
    importMenuItem.setOnAction(this::importFromFile);
    return importMenuItem;
  }

  private MenuItem categoriesMenuItem() {
    var categoriesMenuItem = new MenuItem("Kategóriák");
    categoriesMenuItem.setOnAction(this::showCategories);
    return categoriesMenuItem;
  }

  private MenuItem predictedCorrectionsMenuItem() {
    var predictedCorrectionsMenuItem = new MenuItem("Betöltés");
    predictedCorrectionsMenuItem.setOnAction(this::loadPredictedCorrections);
    return predictedCorrectionsMenuItem;
  }

  private void loadPredictedCorrections(ActionEvent event) {
    var predictionsFile = fileChooserFactory.forPredictions(this.getScene().getWindow());
    predictionsFile.ifPresent(
        sourceFile -> {
          predictionService.replacePredictedCorrections(sourceFile);
          alertFactory.forPredictionsLoaded();
        });
  }

  private void showCategories(ActionEvent actionEvent) {
    categoriesDialog.showAndWait();
  }

  private void importFromFile(ActionEvent actionEvent) {
    var transactionsFile = fileChooserFactory.forTransactions(this.getScene().getWindow());
    transactionsFile.ifPresent(this::doImport);
  }

  private void doImport(File file) {
    taskExecutorService.runTask(
        taskFactory.importTransactionsTask(file),
        newTransactions -> {
          var noOfAddedTransactions = addTransactions(newTransactions);
          removePredictedFlags(newTransactions);
          alertFactory.forImportResult(noOfAddedTransactions);
        });
  }

  private int addTransactions(List<AccountTransaction> newTransactions) {
    int counter = 0;
    var monthlyGrouppedTransactions =
        newTransactions.stream().collect(Collectors.groupingBy(t -> YearMonth.from(t.getDate())));

    for (var entry : monthlyGrouppedTransactions.entrySet()) {
      counter +=
          dataManager.getMonthlyBalances().stream()
              .filter(mb -> mb.getYearMonth().equals(entry.getKey()))
              .findFirst()
              .map(mb -> mb.addTransactions(entry.getValue()))
              .orElseThrow();
    }

    return counter;
  }

  private void removePredictedFlags(List<AccountTransaction> newTransactions) {
    var lastTransactionDate =
        newTransactions.stream()
            .map(AccountTransaction::getDate)
            .max(LocalDate::compareTo)
            .orElseThrow();

    dataManager.getAllDailyBalances().stream()
        .filter(
            daily ->
                daily.getDate().isBefore(lastTransactionDate.plusDays(1)) && daily.getPredicted())
        .forEach(daily -> daily.setPredicted(false));
  }

  private void doSave(ActionEvent actionEvent) {
    try {
      dataForViewService.save();
    } catch (Exception e) {
      log.error("", e);
      ExceptionDialog.get(e).showAndWait();
    }
  }

  private void doSearch(ActionEvent actionEvent) {
    var searchDialog = new SearchDialog(dataManager);
    searchDialog.showAndWait();
  }
}
