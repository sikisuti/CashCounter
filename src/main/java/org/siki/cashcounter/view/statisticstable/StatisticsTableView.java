package org.siki.cashcounter.view.statisticstable;

import static org.siki.cashcounter.view.statisticstable.StatisticsTableProvider.STAT_END_OFFSET_MONTH;
import static org.siki.cashcounter.view.statisticstable.StatisticsTableProvider.STAT_START_OFFSET_MONTH;

import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.view.Refreshable;
import org.siki.cashcounter.view.ViewFactory;

@SuppressWarnings("java:S110")
@Slf4j
public class StatisticsTableView extends TableView<CategoryRow> implements Refreshable {
  private final ViewFactory viewFactory;
  private final StatisticsTableProvider statisticsProvider;

  public StatisticsTableView(ViewFactory viewFactory, StatisticsTableProvider statisticsProvider) {
    super();
    this.viewFactory = viewFactory;
    this.statisticsProvider = statisticsProvider;

    configureColumns();
  }

  @Override
  public void refresh() {
    setItems(statisticsProvider.getStatistics());
  }

  private void configureColumns() {
    configureCategoryNameColumn();
    configureValueColumns();
  }

  private void configureCategoryNameColumn() {
    var categoryNameCol = new TableColumn<CategoryRow, CategoryRow>();
    categoryNameCol.setPrefWidth(120);
    categoryNameCol.setCellFactory(
        categoryRowStringTableColumn ->
            new TableCell<>() {
              @Override
              protected void updateItem(CategoryRow categoryRow, boolean empty) {
                super.updateItem(categoryRow, empty);
                if (!empty && categoryRow != null) {
                  var button = new Button(categoryRow.getCategoryName());
                  button.setPrefWidth(200);
                  button.setOnAction(
                      actionEvent ->
                          viewFactory
                              .createCategoryChartDialog(categoryRow.getCategoryName())
                              .showChart());
                  setGraphic(button);
                }
              }
            });
    categoryNameCol.setCellValueFactory(
        categoryRowStringCellDataFeatures ->
            new SimpleObjectProperty<>(categoryRowStringCellDataFeatures.getValue()));
    getColumns().add(categoryNameCol);
  }

  private void configureValueColumns() {
    var currencyFormat = NumberFormat.getCurrencyInstance();
    currencyFormat.setMaximumFractionDigits(0);
    for (var yearMonth = YearMonth.now().plusMonths(STAT_START_OFFSET_MONTH);
        yearMonth.isBefore(YearMonth.now().plusMonths(STAT_END_OFFSET_MONTH + 1L));
        yearMonth = yearMonth.plusMonths(1)) {
      var valueCol =
          new TableColumn<CategoryRow, Number>(
              yearMonth.format(DateTimeFormatter.ofPattern("yyyy.M.")));
      valueCol.setCellValueFactory(monthlyCellValueFactory(yearMonth));
      valueCol.setCellFactory(monthlyCellFactory(currencyFormat));
      getColumns().add(valueCol);
    }
  }

  private Callback<TableColumn<CategoryRow, Number>, TableCell<CategoryRow, Number>>
      monthlyCellFactory(NumberFormat currencyFormat) {
    return tc ->
        new TableCell<>() {
          @Override
          protected void updateItem(Number value, boolean empty) {
            super.updateItem(value, empty);
            if (empty || value == null) {
              setText(null);
            } else {
              setText(currencyFormat.format(value));
            }
          }
        };
  }

  private Callback<TableColumn.CellDataFeatures<CategoryRow, Number>, ObservableValue<Number>>
      monthlyCellValueFactory(YearMonth yearMonth) {
    return categoryRowStringCellDataFeatures ->
        categoryRowStringCellDataFeatures
            .getValue()
            .getCategoryCell(yearMonth)
            .map(cell -> new SimpleIntegerProperty(cell.getAmount()))
            .orElse(null);
  }
}
