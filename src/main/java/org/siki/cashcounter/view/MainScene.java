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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.model.AccountTransaction;
import org.siki.cashcounter.model.PredictedCorrection;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.DailyBalanceService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.util.StopWatch;
import org.siki.cashcounter.view.chart.CashFlowChart;
import org.siki.cashcounter.view.dialog.ExceptionDialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class MainScene extends Scene {

  @Autowired private final ConfigurationManager configurationManager;
  @Autowired private final ViewFactory viewFactory;
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final AccountTransactionService transactionService;
  @Autowired private final DailyBalanceService dailyBalanceService;
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
      DailyBalanceService dailyBalanceService,
      DataManager dataManager,
      CashFlowChart cashFlowChart,
      PredictionService predictionService) {
    super(new BorderPane(), 640, 480);
    this.cashFlowChart = cashFlowChart;
    this.configurationManager = configurationManager;
    this.viewFactory = viewFactory;
    this.dataForViewService = dataForViewService;
    this.transactionService = transactionService;
    this.dailyBalanceService = dailyBalanceService;
    this.dataManager = dataManager;
    this.predictionService = predictionService;

    Bindings.bindContent(dailyBalancesPH.getChildren(), monthlyBalanceTitledPanes);

    draw((BorderPane) getRoot());
    loadCorrections();
    vbCashFlow.getChildren().add(cashFlowChart);
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
    importMenuItem.setOnAction(this::doImport);

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

  private void doImport(ActionEvent actionEvent) {
    var fileChooser = new FileChooser();
    fileChooser.setTitle("Válaszd ki a fájlt");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("csv files", "*.csv"),
            new FileChooser.ExtensionFilter("Minden fájl", "*.*"));
    var selectedFile = fileChooser.showOpenDialog(this.getWindow());
    if (selectedFile != null) {
      try (var br =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(selectedFile), StandardCharsets.UTF_8))) {
        String line;
        List<AccountTransaction> newTransactions = new ArrayList<>();

        while ((line = br.readLine()) != null) {
          transactionService.createObservableTransactionsFromCSV(line, newTransactions);
        }

        newTransactions.sort(Comparator.comparing(AccountTransaction::getDate));
        TreeMap<LocalDate, List<AccountTransaction>> groupped =
            newTransactions.stream()
                .collect(
                    Collectors.groupingBy(
                        AccountTransaction::getDate, TreeMap::new, Collectors.toList()));

        for (Map.Entry<LocalDate, List<AccountTransaction>> entry : groupped.entrySet()) {
          var db =
              monthlyBalanceTitledPanes.stream()
                  .filter(
                      mb ->
                          mb.getMonthlyBalance()
                              .getYearMonth()
                              .equals(YearMonth.from(entry.getKey())))
                  .findFirst()
                  .orElseThrow()
                  .getDailyBalanceControls()
                  .stream()
                  .filter(dbc -> dbc.getDailyBalance().getDate().isEqual(entry.getKey()))
                  .findFirst()
                  .orElseThrow();
          transactionService.storeObservableTransactions(entry.getValue(), db.getDailyBalance());
        }

        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Üzenet");
        alert.setHeaderText("Importálás kész");
        alert.setContentText(newTransactions.size() + " új tranzakció importálva");
        alert.showAndWait();
        validate();
      } catch (Exception e) {
        log.error("", e);
        ExceptionDialog.get(e).showAndWait();
      }
    }
  }

  private void showCategories(ActionEvent actionEvent) {}

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

  private void scrollChart(ScrollEvent scrollEvent) {}

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
