package org.siki.cashcounter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Component
public class ConfigurationManager {
  private final Properties properties = new Properties();

  public ConfigurationManager(String configurationPath) throws IOException {
    try (var inputStream = new FileInputStream(configurationPath);
        var inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      properties.load(inputStreamReader);
    } catch (IOException e) {
      log.error("Unable to initialize properties", e);
      throw e;
    }
  }

  public Optional<String> getStringProperty(String propertyName) {
    return Optional.ofNullable(properties.getProperty(propertyName));
  }

  public Optional<Boolean> getBooleanProperty(String propertyName) {
    return getStringProperty(propertyName).map(Boolean::parseBoolean);
  }

  public Optional<Double> getDoubleProperty(String propertyName) {
    return getStringProperty(propertyName).map(Double::parseDouble);
  }

  public Optional<Integer> getIntegerProperty(String propertyName) {
    return getStringProperty(propertyName).map(Integer::parseInt);
  }

  public void setProperty(String propertyName, String propertyValue) {
    properties.setProperty(propertyName, propertyValue);
    saveProperties();
  }

  private void saveProperties() {
    try (OutputStream outputStream = new FileOutputStream("./config.properties")) {
      properties.store(outputStream, "CashCount property file");
    } catch (IOException ex) {
      log.error("Unable to save properties", ex);
    }
  }
}
