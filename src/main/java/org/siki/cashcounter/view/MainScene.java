package org.siki.cashcounter.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.repository.DataManager;
import org.siki.cashcounter.service.AccountTransactionService;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.service.PredictionService;
import org.siki.cashcounter.util.StopWatch;
import org.siki.cashcounter.view.chart.CashFlowChart;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.YearMonth;

@Slf4j
public class MainScene extends Scene {

  @Autowired private final MainMenuBar mainMenuBar;
  @Autowired private final ConfigurationManager configurationManager;
  @Autowired private final ViewFactory viewFactory;
  @Autowired private final DataForViewService dataForViewService;
  @Autowired private final AccountTransactionService transactionService;
  @Autowired private final DataManager dataManager;
  @Autowired private final CashFlowChart cashFlowChart;
  @Autowired private final PredictionService predictionService;

  public final ObservableList<MonthlyBalanceTitledPane> monthlyBalanceTitledPanes =
      FXCollections.observableArrayList();

  private final VBox dailyBalancesPH = new VBox();
  private final VBox vbCashFlow = new VBox();
  private final VBox vbStatistics = new VBox();
  private final VBox vbStatisticsTable = new VBox();

  public MainScene(
      MainMenuBar mainMenuBar,
      ConfigurationManager configurationManager,
      ViewFactory viewFactory,
      DataForViewService dataForViewService,
      AccountTransactionService transactionService,
      DataManager dataManager,
      CashFlowChart cashFlowChart,
      PredictionService predictionService) {
    super(new StackPane(), 640, 480);
    this.mainMenuBar = mainMenuBar;
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
    pane.setTop(mainMenuBar);
    pane.setCenter(getTabPane());
  }

  private Node getTabPane() {
    return new TabPane(
        getCorrectionsTab(), getCashFlowTab(), getStatisticsTab(), getStatisticsTableTab());
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
    if (configurationManager.getBooleanProperty("LogPerformance").orElse(false))
      StopWatch.start("prepareDailyBalances");
    dailyBalancesPH.getChildren().clear();
    dataManager.getMonthlyBalances().stream()
        .filter(mb -> mb.getYearMonth().isAfter(YearMonth.now().minusYears(1)))
        .forEach(
            mb -> monthlyBalanceTitledPanes.add(viewFactory.createMonthlyBalanceTitledPane(mb)));
    validate();
    if (configurationManager.getBooleanProperty("LogPerformance").orElse(false))
      StopWatch.stop("prepareDailyBalances");
  }

  public void validate() {
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

  private Tab getStatisticsTableTab() {
    vbStatisticsTable.setAlignment(Pos.CENTER);
    var statisticsTab = new Tab("Statisztikák", vbStatisticsTable);
    statisticsTab.setClosable(false);
    statisticsTab.setOnSelectionChanged(this::refreshStatisticsTable);
    return statisticsTab;
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

  private void refreshStatisticsTable(Event event) {
    if (((Tab) (event.getSource())).isSelected()) {
      vbStatisticsTable.getChildren().clear();
      vbStatisticsTable.getChildren().add(viewFactory.createStatisticsTableView());
    }
  }
}
