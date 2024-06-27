package org.siki.cashcounter.view.dialog;

import static javafx.stage.StageStyle.DECORATED;

import java.text.Collator;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.siki.cashcounter.repository.DataManager;

public class CategoriesDialog extends Stage {
  private final ListView<String> patternsView = new ListView<>();
  private final DataManager dataManager;

  private final TextField patternField = new TextField();
  private final ComboBox<String> categoryPicker = new ComboBox<>();

  public CategoriesDialog(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  @Override
  public void showAndWait() {
    if (isNotInitiated()) {
      loadUI();
    }

    super.showAndWait();
  }

  private boolean isNotInitiated() {
    return getScene() == null;
  }

  private void loadUI() {
    var root = new VBox(listViews(), addView());
    setScene(new Scene(root));

    this.initStyle(DECORATED);
  }

  private Node addView() {
    categoryPicker.setEditable(true);
    categoryPicker.setItems(
        FXCollections.observableArrayList(dataManager.getCategoryMatchingRules().keySet())
            .sorted((o1, o2) -> Collator.getInstance(new Locale("hu", "HU")).compare(o1, o2)));
    var addButton = new Button("Hozzáad");
    addButton.setOnAction(this::addPattern);
    addButton
        .disableProperty()
        .bind(
            Bindings.isEmpty(patternField.textProperty())
                .or(Bindings.isNull(categoryPicker.valueProperty())));
    var control = new HBox(patternField, categoryPicker, addButton);
    control.setSpacing(20);
    control.setPadding(new Insets(20));
    return control;
  }

  private void addPattern(ActionEvent event) {
    dataManager.addCategoryMatchingRule(patternField.getText(), categoryPicker.getValue());
    patternField.clear();
  }

  private Node listViews() {
    var control = new HBox(categoriesListView(), patternsView);
    control.setSpacing(20);
    control.setPadding(new Insets(20));
    return control;
  }

  private Node categoriesListView() {
    var categories =
        new ListView<>(
            FXCollections.observableArrayList(
                    dataManager.getCategoryMatchingRules().keySet().stream()
                        .map(k -> k + " (" + dataManager.getNumberOfTransactionsBy(k) + ")")
                        .collect(Collectors.toList()))
                .sorted((o1, o2) -> Collator.getInstance(new Locale("hu", "HU")).compare(o1, o2)));

    categories
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observableValue, oldValue, newValue) ->
                patternsView.setItems(
                    FXCollections.observableList(
                        dataManager
                            .getCategoryMatchingRules()
                            .get(newValue.replaceAll(" \\([0-9]+\\)", "")))));

    return categories;
  }
}
