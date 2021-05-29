package org.siki.cashcounter.view.chart;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.chart.Axis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class DateAxis extends Axis<LocalDate> {
  private final ObjectProperty<LocalDate> lowerBound =
      new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
          return DateAxis.this;
        }

        @Override
        public String getName() {
          return "lowerBound";
        }
      };

  public final LocalDate getLowerBound() {
    return lowerBound.get();
  }

  public final void setLowerBound(LocalDate value) {
    lowerBound.set(value);
  }

  public final ObjectProperty<LocalDate> lowerBoundProperty() {
    return lowerBound;
  }

  private final ObjectProperty<LocalDate> upperBound =
      new ObjectPropertyBase<LocalDate>() {
        @Override
        public Object getBean() {
          return DateAxis.this;
        }

        @Override
        public String getName() {
          return "upperBound";
        }
      };

  public final LocalDate getUpperBound() {
    return upperBound.get();
  }

  public final void setUpperBound(LocalDate value) {
    upperBound.set(value);
  }

  public final ObjectProperty<LocalDate> upperBoundProperty() {
    return upperBound;
  }

  public DateAxis(LocalDate lowerBound, LocalDate upperBound) {
    setLowerBound(lowerBound);
    setUpperBound(upperBound);
  }

  @Override
  protected Object autoRange(double v) {
    return getRange();
  }

  @Override
  protected void setRange(Object o, boolean b) {}

  @Override
  protected Object getRange() {
    return new Object[] {getLowerBound(), getUpperBound()};
  }

  @Override
  public double getZeroPosition() {
    return 0;
  }

  @Override
  public double getDisplayPosition(LocalDate localDate) {
    double length = getWidth();
    long days = DAYS.between(getLowerBound(), getUpperBound());
    double unit = length / days;
    long offset = DAYS.between(getLowerBound(), localDate);
    return offset * unit;
  }

  @Override
  public LocalDate getValueForDisplay(double v) {
    return null;
  }

  @Override
  public boolean isValueOnAxis(LocalDate localDate) {
    return false;
  }

  @Override
  public double toNumericValue(LocalDate localDate) {
    return 0;
  }

  @Override
  public LocalDate toRealValue(double v) {
    return null;
  }

  @Override
  protected List<LocalDate> calculateTickValues(double v, Object o) {
    List<LocalDate> values = new ArrayList<>();
    Object[] range = (Object[]) o;
    LocalDate startDate = (LocalDate) range[0];
    LocalDate endDate = (LocalDate) range[1];
    for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
      if (date.getDayOfMonth() == 1) {
        values.add(date);
      }
    }

    return values;
  }

  @Override
  protected String getTickMarkLabel(LocalDate localDate) {
    return localDate.format(DateTimeFormatter.ofPattern("YY.MM"));
  }
}
