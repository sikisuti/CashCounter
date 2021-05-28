package org.siki.cashcounter;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.siki.cashcounter.view.MainScene;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

    /*XYChart.Series<LocalDate, Integer> series = new XYChart.Series<>();
    series.setName("Dátum");
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int value = random.nextInt(0, 100);
    for (LocalDate date = LocalDate.now().minusYears(1); date.isBefore(LocalDate.now().plusYears(1)); date = date.plusDays(1)) {
      value += random.nextInt(-10, 10);
      series.getData().add(new XYChart.Data<>(date, value));
    }
    LineChart<LocalDate, Integer> chart = new LineChart(new DateAxis(series.getData().get(0).getXValue(), series.getData().get(series.getData().size() - 1).getXValue()), new NumberAxis());
    chart.getData().addAll(series);
    chart.setCreateSymbols(false);*/

    stage.setScene(mainScene);
    stage.show();
  }
}
