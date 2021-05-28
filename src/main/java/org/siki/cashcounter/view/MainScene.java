package org.siki.cashcounter.view;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.siki.cashcounter.repository.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

public class MainScene extends Scene {

  @Autowired private DataManager dataManager;

  public MainScene() {
    super(new BorderPane(), 640, 480);
    draw((BorderPane) getRoot());
  }

  private void draw(BorderPane pane) {
    pane.setTop(getMenuBar());
    pane.setCenter(getTabPane());
  }

  private Node getTabPane() {
    return new TabPane(getCorrectionsTab());
  }

  private Tab getCorrectionsTab() {
    var gridPane = new GridPane();
    var correctionsTab = new Tab("Korrekciók", gridPane);
    correctionsTab.setClosable(false);
    return correctionsTab;
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
}
