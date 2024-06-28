package org.siki.cashcounter.view.chart;

import static java.util.Objects.nonNull;
import static javafx.geometry.Pos.TOP_LEFT;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.CorrectionService;
import org.siki.cashcounter.view.Refreshable;

public class CategoryChartGrid extends GridPane implements Refreshable {
  public static final int DEFAULT_AVERAGE_WEEK_RANGE = 4;

  private final CategoryChart categoryChart;
  private final ChoiceBox<CategoryChartGrid.Range> rangeChoiceBox;
  private final ConfigurationManager configurationManager;
  private final CorrectionService correctionService;

  @Setter private String category;

  public CategoryChartGrid(
      CategoryService categoryService,
      ConfigurationManager configurationManager,
      CorrectionService correctionService) {
    this.categoryChart = new CategoryChart(categoryService);
    this.configurationManager = configurationManager;
    this.correctionService = correctionService;
    this.rangeChoiceBox = rangeChoiceBox();
    loadUI();
  }

  @Override
  public void refresh() {
    loadUI();
    if (nonNull(category)) {
      categoryChart.refreshChart(
          category, rangeChoiceBox.getSelectionModel().getSelectedItem().getWeekRange());
    }
  }

  private void loadUI() {
    setHgap(10);
    setVgap(10);
    var categoriesColumn = new ColumnConstraints(130);
    var chartColumn = new ColumnConstraints();
    chartColumn.setHgrow(Priority.ALWAYS);
    getColumnConstraints().clear();
    getColumnConstraints().addAll(categoriesColumn, chartColumn);
    var contentRow = new RowConstraints();
    contentRow.setVgrow(Priority.ALWAYS);
    getRowConstraints().clear();
    getRowConstraints().addAll(contentRow);

    getChildren().clear();
    getChildren().addAll(categoriesArea(), chartArea());
  }

  private ScrollPane categoriesArea() {
    var vBox = new VBox();
    vBox.setSpacing(10);
    vBox.setPadding(new Insets(10));
    correctionService
        .getAllCorrectionTypes()
        .forEach(
            c -> {
              var btn = new Button(c);
              btn.setPrefWidth(90);
              btn.setOnAction(
                  actionEvent -> {
                    category = c;
                    categoryChart.refreshChart(
                        category,
                        rangeChoiceBox.getSelectionModel().getSelectedItem().getWeekRange());
                  });
              vBox.getChildren().add(btn);
            });

    return new ScrollPane(vBox);
  }

  private StackPane chartArea() {
    var chartArea = new StackPane(categoryChart, rangeChoiceBox);
    GridPane.setColumnIndex(chartArea, 1);

    return chartArea;
  }

  private ChoiceBox<CategoryChartGrid.Range> rangeChoiceBox() {
    var rangePicker =
        new ChoiceBox<>(FXCollections.observableList(List.of(CategoryChartGrid.Range.values())));
    rangePicker.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(CategoryChartGrid.Range range) {
            return range.label;
          }

          @Override
          public CategoryChartGrid.Range fromString(String s) {
            return CategoryChartGrid.Range.valueOf(s);
          }
        });
    var weekRange =
        configurationManager
            .getIntegerProperty(category + " average range")
            .orElse(DEFAULT_AVERAGE_WEEK_RANGE);
    rangePicker.getSelectionModel().select(CategoryChartGrid.Range.fromWeeks(weekRange));
    rangePicker
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observableValue, oldRange, newRange) ->
                categoryChart.refreshChart(category, newRange.getWeekRange()));
    StackPane.setAlignment(rangePicker, TOP_LEFT);
    return rangePicker;
  }

  @Getter
  @RequiredArgsConstructor
  enum Range {
    ONE_MONTH("1 hónap", 4),
    QUARTER("negyed év", 13),
    HALF_YEAR("fél év", 26),
    ONE_YEAR("1 év", 52),
    TWO_YEARS("2 év", 104);

    private final String label;
    private final int weekRange;

    public static CategoryChartGrid.Range fromWeeks(int weekRange) {
      return Stream.of(CategoryChartGrid.Range.values())
          .min(Comparator.comparingInt(o -> Math.abs(weekRange - o.weekRange)))
          .orElseThrow();
    }
  }
}
