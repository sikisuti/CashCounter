package org.siki.cashcounter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
@Component
public class ConfigurationManager {
    private final Properties properties = new Properties();

    public ConfigurationManager() throws IOException {
        try (var inputStream = new FileInputStream("./config.properties");
             var inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error("Unable to initialize properties", e);
            throw e;
        }
    }

    public String getStringProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public boolean getBooleanProperty(String propertyName) {
        return Boolean.parseBoolean(getStringProperty(propertyName));
    }

    public double getDoubleProperty(String propertyName) {
        return Double.parseDouble(getStringProperty(propertyName));
    }

    public int getIntegerProperty(String propertyName) {
        return Integer.parseInt(getStringProperty(propertyName));
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
