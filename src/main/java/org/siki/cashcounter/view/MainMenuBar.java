package org.siki.cashcounter.view;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.util.FilePicker;
import org.siki.cashcounter.view.dialog.CategoriesDialog;
import org.siki.cashcounter.view.dialog.ExceptionDialog;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MainMenuBar extends MenuBar {

  private final PredictionService predictionService;
  private final CategoriesDialog categoriesDialog;
  private final AccountTransactionService transactionService;
  private final DataManager dataManager;
  private final DataForViewService dataForViewService;

  public MainMenuBar(
      PredictionService predictionService,
      CategoriesDialog categoriesDialog,
      AccountTransactionService transactionService,
      DataManager dataManager,
      DataForViewService dataForViewService) {
    this.predictionService = predictionService;
    this.categoriesDialog = categoriesDialog;
    this.transactionService = transactionService;
    this.dataManager = dataManager;
    this.dataForViewService = dataForViewService;
    this.getMenus().addAll(fileMenu(), dataMenu(), correctionsMenu());
  }

  private Menu fileMenu() {
    return new Menu("Fájl", null, saveMenuItem());
  }

  private Menu dataMenu() {
    return new Menu("Adat", null, importMenuItem(), categoriesMenuItem());
  }

  private Menu correctionsMenu() {
    return new Menu("Korrekciók", null, predictedCorrectionsMenuItem());
  }

  private MenuItem predictedCorrectionsMenuItem() {
    var predictedCorrectionsMenuItem = new MenuItem("Betöltés");
    predictedCorrectionsMenuItem.setOnAction(this::loadPredictedCorrections);
    return predictedCorrectionsMenuItem;
  }

  private MenuItem categoriesMenuItem() {
    var categoriesMenuItem = new MenuItem("Kategóriák");
    categoriesMenuItem.setOnAction(this::showCategories);
    return categoriesMenuItem;
  }

  private MenuItem importMenuItem() {
    var importMenuItem = new MenuItem("Importálás");
    importMenuItem.setOnAction(this::importFromFile);
    return importMenuItem;
  }

  private MenuItem saveMenuItem() {
    var saveMenuItem = new MenuItem("Mentés");
    saveMenuItem.setOnAction(this::doSave);
    return saveMenuItem;
  }

  private void loadPredictedCorrections(ActionEvent event) {
    var fileChooser = new FileChooser();
    fileChooser.setTitle("Válaszd ki a korrekciós fájlt");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("json files", "*.jsn"),
            new FileChooser.ExtensionFilter("Minden fájl", "*.*"));
    var selectedFile = fileChooser.showOpenDialog(getMainScene().getWindow());
    Optional.ofNullable(selectedFile)
        .ifPresent(
            sourceFile -> {
              predictionService.replacePredictedCorrections(sourceFile);

              var alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("Üzenet");
              alert.setHeaderText("Végrehajtva");
              alert.setContentText("Korrekciók betöltve");
              alert.showAndWait();
            });
  }

  private void showCategories(ActionEvent actionEvent) {
    categoriesDialog.showAndWait();
  }

  private void importFromFile(ActionEvent actionEvent) {
    selectFileToImport()
        .ifPresent(
            f -> {
              doImport(f);
              getMainScene().validate();
            });
  }

  private Optional<File> selectFileToImport() {
    return FilePicker.builder(getMainScene().getWindow())
        .title("Válaszd ki a fájlt")
        .extensionFilter("excel", new String[] {"*.xlsx"})
        .extensionFilter("csv files", new String[] {"*.csv"})
        .extensionFilter("Minden fájl", new String[] {"*.*"})
        .build()
        .showDialog();
  }

  private void doImport(File file) {
    int counter = 0;
    var newTransactions = transactionService.importTransactionsFrom(file);

    var monthlyGrouppedTransactions =
        newTransactions.stream().collect(Collectors.groupingBy(t -> YearMonth.from(t.getDate())));

    for (var entry : monthlyGrouppedTransactions.entrySet()) {
      counter +=
          getMainScene().monthlyBalanceTitledPanes.stream()
              .filter(mb -> mb.getMonthlyBalance().getYearMonth().equals(entry.getKey()))
              .findFirst()
              .map(mb -> mb.addTransactions(entry.getValue()))
              .orElseThrow();
    }

    removePredictedFlags(newTransactions);
    showImportResult(counter);
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

  private void showImportResult(int counter) {
    var alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Üzenet");
    alert.setHeaderText("Importálás kész");
    alert.setContentText(counter + " új tranzakció importálva");
    alert.showAndWait();
  }

  private void doSave(ActionEvent actionEvent) {
    try {
      dataForViewService.save();
    } catch (Exception e) {
      log.error("", e);
      ExceptionDialog.get(e).showAndWait();
    }
  }

  private MainScene getMainScene() {
    return (MainScene) this.getParent().getScene();
  }
}
