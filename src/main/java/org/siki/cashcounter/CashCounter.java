package org.siki.cashcounter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.view.MainScene;
import org.siki.cashcounter.view.chart.DateAxis;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class CashCounter extends Application {
  public static void main(String[] args) {
    launch();
  }

  ApplicationContext context;

  @Override
  public void start(Stage stage) throws Exception {
    context = new AnnotationConfigApplicationContext(Config.class);
    var mainScene = context.getBean(MainScene.class);
    log.info(mainScene.getData().get(0).getName());

    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

    XYChart.Series<LocalDate, Integer> series = new XYChart.Series<>();
    series.setName("Dátum");
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int value = random.nextInt(0, 100);
    for (LocalDate date = LocalDate.now().minusYears(1); date.isBefore(LocalDate.now().plusYears(1)); date = date.plusDays(1)) {
      value += random.nextInt(-10, 10);
      series.getData().add(new XYChart.Data<>(date, value));
    }
    LineChart<LocalDate, Integer> chart = new LineChart(new DateAxis(series.getData().get(0).getXValue(), series.getData().get(series.getData().size() - 1).getXValue()), new NumberAxis());
    chart.getData().addAll(series);
    chart.setCreateSymbols(false);
    Scene scene = new Scene(new StackPane(l, chart), 640, 480);
    stage.setScene(scene);
    stage.show();
  }
}
