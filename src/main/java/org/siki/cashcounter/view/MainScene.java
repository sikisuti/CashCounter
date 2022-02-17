package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.PredictedCorrection;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.util.FilePicker;
import org.siki.cashcounter.util.StopWatch;
import org.siki.cashcounter.view.chart.CashFlowChart;
import org.siki.cashcounter.view.dialog.ExceptionDialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MainScene extends Scene {

  @Autowired private final ConfigurationManager configurationManager;
  @Autowired private final ViewFactory viewFactory;
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final AccountTransactionService transactionService;
  @Autowired private final DataManager dataManager;
  @Autowired private final CashFlowChart cashFlowChart;
  @Autowired private final PredictionService predictionService;

  private final ObservableList<MonthlyBalanceTitledPane> monthlyBalanceTitledPanes =
      FXCollections.observableArrayList();

  private final VBox dailyBalancesPH = new VBox();
  private final VBox vbCashFlow = new VBox();
  private final VBox vbStatistics = new VBox();

  public MainScene(
      ConfigurationManager configurationManager,
      ViewFactory viewFactory,
      DataForViewService dataForViewService,
      AccountTransactionService transactionService,
      DataManager dataManager,
      CashFlowChart cashFlowChart,
      PredictionService predictionService) {
    super(new StackPane(), 640, 480);
    this.cashFlowChart = cashFlowChart;
    this.configurationManager = configurationManager;
    this.viewFactory = viewFactory;
    this.dataForViewService = dataForViewService;
    this.transactionService = transactionService;
    this.dataManager = dataManager;
    this.predictionService = predictionService;

    Bindings.bindContent(dailyBalancesPH.getChildren(), monthlyBalanceTitledPanes);

    var root = (StackPane) getRoot();
    var borderPane = new BorderPane();
    root.getChildren().addAll(borderPane);
    draw(borderPane);
    loadCorrections();
    vbCashFlow.getChildren().add(cashFlowChart);
  }

  // For future use
  private StackPane initBusyVeil() {
    var veil = new Region();
    veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
    var progressIndicator = new ProgressIndicator();
    progressIndicator.setStyle("-fx-min-width: 100; -fx-min-height: 100;");
    VBox.setVgrow(progressIndicator, Priority.ALWAYS);
    var progressMessage = new Label();
    progressMessage.setFont(new Font("System Bold", 18));
    var progressInfo = new VBox(progressIndicator, progressMessage);
    progressInfo.setAlignment(Pos.CENTER);
    StackPane.setAlignment(progressInfo, Pos.CENTER);
    var busyVeil = new StackPane(veil, progressInfo);
    StackPane.setAlignment(busyVeil, Pos.CENTER);
    return busyVeil;
  }

  private void draw(BorderPane pane) {
    pane.setTop(getMenuBar());
    pane.setCenter(getTabPane());
  }

  private Node getTabPane() {
    return new TabPane(getCorrectionsTab(), getCashFlowTab(), getStatisticsTab());
  }

  private Tab getCorrectionsTab() {
    var gridPane = new GridPane();
    var columnConstraints = new ColumnConstraints();
    columnConstraints.setPercentWidth(100);
    gridPane.getColumnConstraints().add(columnConstraints);
    var rowConstraints = new RowConstraints();
    rowConstraints.setVgrow(Priority.ALWAYS);
    gridPane.getRowConstraints().add(rowConstraints);
    gridPane.getChildren().add(new ScrollPane(dailyBalancesPH));
    var correctionsTab = new Tab("Korrekciók", gridPane);
    correctionsTab.setClosable(false);
    return correctionsTab;
  }

  private void loadCorrections() {
    if (configurationManager.getBooleanProperty("LogPerformance"))
      StopWatch.start("prepareDailyBalances");
    dailyBalancesPH.getChildren().clear();
    dataManager.getMonthlyBalances().stream()
        .filter(mb -> mb.getYearMonth().isAfter(YearMonth.now().minusYears(1)))
        .forEach(
            mb -> monthlyBalanceTitledPanes.add(viewFactory.createMonthlyBalanceTitledPane(mb)));
    validate();
    if (configurationManager.getBooleanProperty("LogPerformance"))
      StopWatch.stop("prepareDailyBalances");
  }

  private void validate() {
    dailyBalancesPH
        .getChildren()
        .forEach(
            child -> {
              if (child instanceof MonthlyBalanceTitledPane) {
                ((MonthlyBalanceTitledPane) child).validate();
              }
            });
  }

  private Tab getCashFlowTab() {
    vbCashFlow.setAlignment(Pos.CENTER);
    vbCashFlow.setOnScroll(this::scrollChart);
    var cashFlowTab = new Tab("Flow chart", vbCashFlow);
    cashFlowTab.setClosable(false);
    cashFlowTab.setOnSelectionChanged(this::refreshChart);
    return cashFlowTab;
  }

  private Tab getStatisticsTab() {
    vbStatistics.setAlignment(Pos.CENTER);
    var statisticsTab = new Tab("Statisztikák", new ScrollPane(vbStatistics));
    statisticsTab.setClosable(false);
    statisticsTab.setOnSelectionChanged(this::refreshStatistics);
    return statisticsTab;
  }

  private MenuBar getMenuBar() {
    var saveMenuItem = new MenuItem("Mentés");
    saveMenuItem.setOnAction(this::doSave);

    var importMenuItem = new MenuItem("Importálás");
    importMenuItem.setOnAction(this::importFromFile);

    var categoriesMenuItem = new MenuItem("Kategóriák");
    categoriesMenuItem.setOnAction(this::showCategories);

    var predictedCorrectionsMenuItem = new MenuItem("Betöltés");
    predictedCorrectionsMenuItem.setOnAction(this::loadPredictedCorrections);

    return new MenuBar(
        new Menu("Fájl", null, saveMenuItem),
        new Menu("Adat", null, importMenuItem, categoriesMenuItem),
        new Menu("Korrekciók", null, predictedCorrectionsMenuItem));
  }

  private void doSave(ActionEvent actionEvent) {
    try {
      dataForViewService.save();
    } catch (Exception e) {
      log.error("", e);
      ExceptionDialog.get(e).showAndWait();
    }
  }

  private void importFromFile(ActionEvent actionEvent) {
    selectFileToImport()
        .ifPresent(
            f -> {
              doImport(f);
              validate();
            });
  }

  private Optional<File> selectFileToImport() {
    return FilePicker.builder(this.getWindow())
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
          monthlyBalanceTitledPanes.stream()
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

  private void showCategories(ActionEvent actionEvent) {
    viewFactory.getCategoriesDialog().showAndWait();
  }

  private void loadPredictedCorrections(ActionEvent event) {
    var fileChooser = new FileChooser();
    fileChooser.setTitle("Válaszd ki a korrekciós fájlt");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("json files", "*.jsn"),
            new FileChooser.ExtensionFilter("Minden fájl", "*.*"));
    var selectedFile = fileChooser.showOpenDialog(getWindow());
    if (selectedFile != null) {
      List<PredictedCorrection> pcList;
      try {
        pcList = predictionService.loadPredictedCorrections(selectedFile.getAbsolutePath());
        predictionService.clearPredictedCorrections();
        predictionService.fillPredictedCorrections(pcList);
        predictionService.storePredictions(pcList);
      } catch (IOException ex) {
        log.error("", ex);
      }

      var alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Üzenet");
      alert.setHeaderText("Végrehajtva");
      alert.setContentText("Korrekciók betöltve");
      alert.showAndWait();
    }
  }

  private void scrollChart(ScrollEvent scrollEvent) {
    // To be implemented
  }

  private void refreshChart(Event event) {
    if (((Tab) (event.getSource())).isSelected()) {
      cashFlowChart.refreshChart();
    }
  }

  private void refreshStatistics(Event event) {
    if (((Tab) (event.getSource())).isSelected()) {
      vbStatistics.getChildren().clear();
      vbStatistics.getChildren().add(viewFactory.createStatisticsView());
    }
  }
}
