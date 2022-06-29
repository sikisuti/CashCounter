package org.siki.cashcounter.view;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.Map;

@SuppressWarnings("java:S110")
public class MainTabPaneContent extends TabPane {

  public MainTabPaneContent(Map<String, Node> tabs) {
    tabs.forEach(
        (label, content) -> {
          var tab = new Tab(label, content);
          tab.setClosable(false);
          if (content instanceof Refreshable) {
            setRefreshBehaviour((Refreshable) content, tab);
          }

          this.getTabs().add(tab);
        });
  }

  private void setRefreshBehaviour(Refreshable value, Tab tab) {
    tab.setOnSelectionChanged(
        event -> {
          if (((Tab) (event.getSource())).isSelected()) {
            value.refresh();
          }
        });
  }
}
