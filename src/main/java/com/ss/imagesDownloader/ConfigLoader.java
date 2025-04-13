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
  private final String path;
  private static final String CONFIG_FILE = "conf.txt";

  private final static String PATH_PROPERTY = "folder";
  private static final Logger logger = LogManager.getLogger();

  public ConfigLoader() {
    this.path = loadPath();
  }

  public String getPath() {
    return path;
  }

  private String loadPath() {
    Properties properties = new Properties();
    InputStream is;
    String path;
    try {
      is = new FileInputStream(CONFIG_FILE);
    } catch (FileNotFoundException exception) {
      logger.error("config file is not found: " + CONFIG_FILE);
      System.exit(1);
      return null;
    }
    try {
      properties.load(is);
      path = properties.getProperty(PATH_PROPERTY);
      if (path == null) {
        logger.error("property " + PATH_PROPERTY
            + "is not found in config file " + CONFIG_FILE);
        System.exit(1);
        return null;
      }
    } catch (IOException exception) {
      logger.error("cannot read config file: " + CONFIG_FILE);
      System.exit(1);
      return null;
    }
    try {
      is.close();
    } catch (IOException exception) {
    }
    if (!Files.exists(Paths.get(path))) {
      logger.error("base folder doesn't exist: " + path);
      System.exit(1);
      return null;
    }
    return path;
  }
}
