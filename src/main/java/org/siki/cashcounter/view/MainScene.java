package org.siki.cashcounter.view;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.service.DataForViewService;
import org.siki.cashcounter.util.StopWatch;
import org.siki.cashcounter.view.chart.CashFlowChart;
import org.springframework.beans.factory.annotation.Autowired;

public class MainScene extends Scene {

  @Autowired private ConfigurationManager configurationManager;
  @Autowired private ViewFactory viewFactory;
  @Autowired private DataForViewService dataForViewService;

  private final VBox dailyBalancesPH = new VBox();
  private final VBox vbCashFlow = new VBox();
  private final VBox vbStatistics = new VBox();

  public MainScene(
      CashFlowChart cashFlowChart,
      ConfigurationManager configurationManager,
      ViewFactory viewFactory,
      DataForViewService dataForViewService) {
    super(new BorderPane(), 640, 480);
    this.configurationManager = configurationManager;
    this.viewFactory = viewFactory;
    this.dataForViewService = dataForViewService;
    draw((BorderPane) getRoot());
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

  private void prepareDailyBalances() {
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

    var refreshMenuItem = new MenuItem("Frissítés");
    refreshMenuItem.setOnAction(this::doRefresh);

    var importMenuItem = new MenuItem("Importálás");
    importMenuItem.setOnAction(this::doImport);

    var categoriesMenuItem = new MenuItem("Kategóriák");
    categoriesMenuItem.setOnAction(this::showCategories);

    var predictedCorrectionsMenuItem = new MenuItem("Betöltés");
    predictedCorrectionsMenuItem.setOnAction(this::loadPredictedCorrections);

    return new MenuBar(
        new Menu("Fájl", null, saveMenuItem),
        new Menu("Adat", null, refreshMenuItem, importMenuItem, categoriesMenuItem),
        new Menu("Korrekciók", null, predictedCorrectionsMenuItem));
  }

  private void doSave(ActionEvent actionEvent) {}

  private void doRefresh(ActionEvent actionEvent) {}

  private void doImport(ActionEvent actionEvent) {}

  private void showCategories(ActionEvent actionEvent) {}

  private void loadPredictedCorrections(ActionEvent actionEvent) {}

  private void scrollChart(ScrollEvent scrollEvent) {}

  private void refreshChart(Event event) {}

  private void refreshStatistics(Event event) {}
}
