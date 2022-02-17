package org.siki.cashcounter.view.statistics;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.siki.cashcounter.ConfigurationManager;

import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.siki.cashcounter.view.statistics.StatisticsTableProvider.STAT_END_OFFSET_MONTH;
import static org.siki.cashcounter.view.statistics.StatisticsTableProvider.STAT_START_OFFSET_MONTH;

public class StatisticsTableView extends TableView<CategoryRow> {

  private final ConfigurationManager configurationManager;

  public StatisticsTableView(
      ConfigurationManager configurationManager, StatisticsTableProvider statisticsProvider) {
    super();
    this.configurationManager = configurationManager;
    configureColumns();
    setItems(statisticsProvider.getStatistics());
  }

  private void configureColumns() {
    configureCategoryNameColumn();
    configureValueColumns();
  }

  private void configureCategoryNameColumn() {
    var categoryNameCol = new TableColumn<CategoryRow, String>("Kategória");
    categoryNameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    getColumns().add(categoryNameCol);
  }

  private void configureValueColumns() {
    var currencyFormat = NumberFormat.getCurrencyInstance();
    currencyFormat.setMaximumFractionDigits(0);
    for (var yearMonth = YearMonth.now().plusMonths(STAT_START_OFFSET_MONTH);
        yearMonth.isBefore(YearMonth.now().plusMonths(STAT_END_OFFSET_MONTH + 1));
        yearMonth = yearMonth.plusMonths(1)) {
      var valueCol =
          new TableColumn<CategoryRow, Number>(
              yearMonth.format(DateTimeFormatter.ofPattern("yyyy.M.")));
      valueCol.setCellValueFactory(monthlyValueFactory(yearMonth));
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
      monthlyValueFactory(YearMonth yearMonth) {
    return categoryRowStringCellDataFeatures ->
        Optional.ofNullable(
                categoryRowStringCellDataFeatures.getValue().getCategoryValue(yearMonth))
            .map(SimpleIntegerProperty::new)
            .orElse(null);
  }
}
