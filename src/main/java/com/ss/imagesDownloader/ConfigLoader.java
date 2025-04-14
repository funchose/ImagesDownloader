package com.ss.imagesDownloader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ConfigLoader {
  private String path = null;
  private Integer threadsNumber = null;
  private static final String CONFIG_FILE = "conf.txt";
  private final static String PATH_PROPERTY = "folder";
  private final static String THREADS_NUMBER_PROPERTY = "threads";
  private static final Logger logger = LogManager.getLogger();

  public ConfigLoader() {
    loadProperties();
  }

  public String getPath() {
    return path;
  }

  public int getThreadsNumber() {
    return threadsNumber;
  }

  private void loadProperties() {
    Properties properties = new Properties();
    InputStream is = null;
    String path = null;
    int threadsNumber = 0;
    try {
      is = new FileInputStream(CONFIG_FILE);
    } catch (FileNotFoundException exception) {
      logger.error("config file is not found: " + CONFIG_FILE);
      System.exit(1);
    }
    try {
      properties.load(is);
      if (properties.getProperty(PATH_PROPERTY).isEmpty()) {
        logger.error("property " + PATH_PROPERTY
            + " is not found in config file " + CONFIG_FILE);
        System.exit(1);
      }
      if (properties.getProperty(THREADS_NUMBER_PROPERTY).isEmpty()) {
        logger.error("property " + THREADS_NUMBER_PROPERTY
            + " is not found in config file " + CONFIG_FILE);
        System.exit(1);
      }
      path = properties.getProperty(PATH_PROPERTY);
      threadsNumber = Integer.parseInt(properties.getProperty(THREADS_NUMBER_PROPERTY));
    } catch (IOException exception) {
      logger.error("cannot read config file: " + CONFIG_FILE);
      System.exit(1);
    }
    try {
      is.close();
    } catch (IOException exception) {
    }
    if (!Files.exists(Paths.get(path))) {
      logger.error("base folder doesn't exist: " + path);
      System.exit(1);
    }
    this.path = path;
    this.threadsNumber = threadsNumber;
  }
}
