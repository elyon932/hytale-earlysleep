package com.example.earlysleep.config;

import com.example.earlysleep.util.LoggerFacade;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;

/**
 * Handles persistence and retrieval of the application configuration.
 */
public class ConfigManager {

   private static final String CONFIG_PATH = "plugins/EarlySleep/config.json";
   private final File configFile;
   private final AppConfig config;

   public ConfigManager() {
      this.configFile = new File(CONFIG_PATH);
      this.config = new AppConfig();
   }

   public AppConfig getConfig() {
      return config;
   }

   public void loadConfiguration() {
      if (!configFile.exists()) {
         saveConfiguration();
         return;
      }

      try {
         String content = new String(Files.readAllBytes(configFile.toPath()));
         parseConfigurationString(content);
      } catch (Exception e) {
         LoggerFacade.error("Failed to load config.json, using defaults.", e);
      }
   }

   public void saveConfiguration() {
      try {
         if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
         }

         String json = buildJsonString();
         Files.write(configFile.toPath(), json.getBytes(), new OpenOption[0]);
      } catch (IOException e) {
         LoggerFacade.error("Error writing configuration file.", e);
      }
   }

   private void parseConfigurationString(String content) {
      try {
         if (content.contains("\"sleepStart\":")) {
            config.setSleepStart(Double.parseDouble(extractValue(content, "\"sleepStart\":")));
         }
         if (content.contains("\"wakeUpTime\":")) {
            config.setWakeUpTime(Double.parseDouble(extractValue(content, "\"wakeUpTime\":")));
         }
         if (content.contains("\"sleepThreshold\":")) {
            config.setSleepThreshold(content.split("\"sleepThreshold\":")[1].split("\"")[1].trim());
         }
         if (content.contains("\"sleepDelay\":")) {
            config.setSleepDelay(Long.parseLong(extractValue(content, "\"sleepDelay\":")));
         }
         if (content.contains("\"loadMessageEnabled\":")) {
            config.setLoadMessageEnabled(Boolean.parseBoolean(extractValue(content, "\"loadMessageEnabled\":")));
         }
         if (content.contains("\"sleepEffectsEnabled\":")) {
            config.setSleepEffectsEnabled(Boolean.parseBoolean(extractValue(content, "\"sleepEffectsEnabled\":")));
         }
      } catch (Exception e) {
         LoggerFacade.error("Malformed configuration data. Using safe defaults.", e);
      }
   }

   private String extractValue(String content, String key) {
      return content.split(key)[1].split(",")[0].split("}")[0].trim();
   }

   private String buildJsonString() {
      return "{\n" +
            "  \"sleepStart\": " + config.getSleepStart() + ",\n" +
            "  \"wakeUpTime\": " + config.getWakeUpTime() + ",\n" +
            "  \"sleepThreshold\": \"" + config.getSleepThreshold() + "\",\n" +
            "  \"sleepDelay\": " + config.getSleepDelay() + ",\n" +
            "  \"loadMessageEnabled\": " + config.isLoadMessageEnabled() + ",\n" +
            "  \"sleepEffectsEnabled\": " + config.isSleepEffectsEnabled() + "\n" +
            "}";
   }
}