package com.example.earlysleep.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;

public class SleepConfig {
    public double sleepStart = (double)19.5F;
    public double wakeUpTime = (double)5.5F;
    public String sleepThreshold = "50%";
    public long sleepDelay = -1L;
    public boolean loadMessageEnabled = true;
    public boolean sleepEffectsEnabled = true;

    private final File configFile = new File("plugins/EarlySleep/config.json");

    public void load() {
        if (!this.configFile.exists()) {
            this.save((double)19.5F, (double)5.5F);
        } else {
            try {
                String content = new String(Files.readAllBytes(this.configFile.toPath()));
                this.sleepStart = Double.parseDouble(content.split("\"sleepStart\":")[1].split(",")[0].trim());
                this.wakeUpTime = Double.parseDouble(content.split("\"wakeUpTime\":")[1].split(",")[0].trim());
                if (content.contains("\"sleepThreshold\":")) {
                    this.sleepThreshold = content.split("\"sleepThreshold\":")[1].split("\"")[1].trim();
                }

                if (content.contains("\"sleepDelay\":")) {
                    this.sleepDelay = Long.parseLong(content.split("\"sleepDelay\":")[1].split(",")[0].split("}")[0].trim());
                }

                if (content.contains("\"loadMessageEnabled\":")) {
                    this.loadMessageEnabled = Boolean.parseBoolean(content.split("\"loadMessageEnabled\":")[1].split(",")[0].split("}")[0].trim());
                }

                if (content.contains("\"sleepEffectsEnabled\":")) {
                    this.sleepEffectsEnabled = Boolean.parseBoolean(content.split("\"sleepEffectsEnabled\":")[1].split(",")[0].split("}")[0].trim());
                }
            } catch (Exception var2) {
                System.err.println("[EarlySleep] Failed to load config.json, using defaults.");
            }
        }
    }

    public void save(double start, double wake) {
        if (start >= (double)0.0F) {
            this.sleepStart = start;
        }

        if (wake >= (double)0.0F) {
            this.wakeUpTime = wake;
        }

        try {
            if (!this.configFile.getParentFile().exists()) {
                this.configFile.getParentFile().mkdirs();
            }

            String json = "{\n  \"sleepStart\": " + this.sleepStart + ",\n  \"wakeUpTime\": " + this.wakeUpTime + ",\n  \"sleepThreshold\": \"" + this.sleepThreshold + "\",\n  \"sleepDelay\": " + this.sleepDelay + ",\n  \"loadMessageEnabled\": " + this.loadMessageEnabled + ",\n  \"sleepEffectsEnabled\": " + this.sleepEffectsEnabled + "\n}";
            Files.write(this.configFile.toPath(), json.getBytes(), new OpenOption[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}