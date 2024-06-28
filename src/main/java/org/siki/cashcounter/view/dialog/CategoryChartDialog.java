package org.siki.cashcounter.view.dialog;

import static javafx.geometry.Pos.TOP_LEFT;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.siki.cashcounter.ConfigurationManager;
import org.siki.cashcounter.service.CategoryService;
import org.siki.cashcounter.service.CorrectionService;
import org.siki.cashcounter.view.chart.CategoryChart;

public class CategoryChartDialog extends Stage {
  public static final int DEFAULT_AVERAGE_WEEK_RANGE = 4;

  private final CorrectionService correctionService;
  private final ConfigurationManager configurationManager;
  private final CategoryChart categoryChart;
  private final ChoiceBox<Range> rangeChoiceBox;
  private String category;

  public CategoryChartDialog(
      CategoryService categoryService,
      String category,
      ConfigurationManager configurationManager,
      CorrectionService correctionService) {
    categoryChart = new CategoryChart(categoryService);
    this.category = category;
    this.configurationManager = configurationManager;
    this.correctionService = correctionService;
    this.rangeChoiceBox = rangeChoiceBox();
    loadUI();
  }

  public void showChart() {
    categoryChart.refreshChart(
        category, rangeChoiceBox.getSelectionModel().getSelectedItem().getWeekRange());
    showAndWait();
  }

  private void loadUI() {
    var root = rootGrid();
    root.getChildren().addAll(categoriesArea(), chartArea());
    setScene(new Scene(root));
    this.initStyle(StageStyle.DECORATED);
    setWidth(1300);
    setHeight(700);
  }

  private GridPane rootGrid() {
    var root = new GridPane(10, 10);
    var categoriesColumn = new ColumnConstraints(130);
    var chartColumn = new ColumnConstraints();
    chartColumn.setHgrow(Priority.ALWAYS);
    root.getColumnConstraints().addAll(categoriesColumn, chartColumn);
    var contentRow = new RowConstraints();
    contentRow.setVgrow(Priority.ALWAYS);
    root.getRowConstraints().addAll(contentRow);

    return root;
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

  private ChoiceBox<Range> rangeChoiceBox() {
    var rangePicker = new ChoiceBox<>(FXCollections.observableList(List.of(Range.values())));
    rangePicker.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(Range range) {
            return range.label;
          }

          @Override
          public Range fromString(String s) {
            return Range.valueOf(s);
          }
        });
    var weekRange =
        configurationManager
            .getIntegerProperty(category + " average range")
            .orElse(DEFAULT_AVERAGE_WEEK_RANGE);
    rangePicker.getSelectionModel().select(Range.fromWeeks(weekRange));
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

    public static Range fromWeeks(int weekRange) {
      return Stream.of(Range.values())
          .min(Comparator.comparingInt(o -> Math.abs(weekRange - o.weekRange)))
          .orElseThrow();
    }
  }
}
