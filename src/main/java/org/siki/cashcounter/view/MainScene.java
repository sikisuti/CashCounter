package org.siki.cashcounter.view;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.DailyBalanceService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.util.StopWatch;
import org.siki.cashcounter.view.chart.CashFlowChart;
import org.siki.cashcounter.view.dialog.ExceptionDialog;
import org.siki.cashcounter.view.model.ObservableAccountTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class MainScene extends Scene {

  @Autowired private final ConfigurationManager configurationManager;
  @Autowired private final ViewFactory viewFactory;
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final AccountTransactionService transactionService;
  @Autowired private final DailyBalanceService dailyBalanceService;

  private final VBox dailyBalancesPH = new VBox();
  private final VBox vbCashFlow = new VBox();
  private final VBox vbStatistics = new VBox();

  public MainScene(
      CashFlowChart cashFlowChart,
      ConfigurationManager configurationManager,
      ViewFactory viewFactory,
      DataForViewService dataForViewService,
      AccountTransactionService transactionService,
      DailyBalanceService dailyBalanceService) {
    super(new BorderPane(), 640, 480);
    this.configurationManager = configurationManager;
    this.viewFactory = viewFactory;
    this.dataForViewService = dataForViewService;
    this.transactionService = transactionService;
    this.dailyBalanceService = dailyBalanceService;
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
    dataForViewService
        .getObservableMonthlyBalances()
        .forEach(
            omb ->
                dailyBalancesPH.getChildren().add(viewFactory.createMonthlyBalanceTitledPane(omb)));
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

  private void doSave(ActionEvent actionEvent) {}

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
        List<ObservableAccountTransaction> newTransactions = new ArrayList<>();

        while ((line = br.readLine()) != null) {
          newTransactions.add(transactionService.createObservableTransactionsFromCSV(line));
        }

        newTransactions.sort(Comparator.comparing(t -> t.dateProperty().get()));
        TreeMap<LocalDate, List<ObservableAccountTransaction>> groupped =
            newTransactions.stream()
                .collect(
                    Collectors.groupingBy(
                        t -> t.dateProperty().get(), TreeMap::new, Collectors.toList()));

        for (Map.Entry<LocalDate, List<ObservableAccountTransaction>> entry : groupped.entrySet()) {
          var db = dailyBalanceService.findDailyBalanceByDate(entry.getKey());
          // TODO: DataManager line 233
          transactionService.storeObservableTransactions(entry.getValue(), db);
        }

        Integer importedRows;
        var force = false;
        do {
          try {
            importedRows = DataManager.getInstance().saveTransactions(newTransactions, force);
            force = false;
          } catch (TransactionGapException ex) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Adathiány");
            alert.setHeaderText("Adat hiányzik");
            StringBuilder content = new StringBuilder();
            ex.missingDates.stream()
                .forEach(
                    (d) -> {
                      content
                          .append(d.toString())
                          .append(" (")
                          .append(d.getDayOfWeek().name())
                          .append(")\n");
                    });
            content.append("\nMégis betöltsem a fájlt?");
            alert.setContentText(content.toString());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) force = true;
          }
        } while (force);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Üzenet");
        alert.setHeaderText("Importálás kész");
        alert.setContentText(importedRows + " új tranzakció importálva");
        alert.showAndWait();
        validate();
      } catch (Exception e) {
        log.error("", e);
        ExceptionDialog.get(e).showAndWait();
      }
    }
  }

  private void showCategories(ActionEvent actionEvent) {}

  private void loadPredictedCorrections(ActionEvent actionEvent) {}

  private void scrollChart(ScrollEvent scrollEvent) {}

  private void refreshChart(Event event) {}

  private void refreshStatistics(Event event) {}
}
