package com.example.earlysleep.config;

/**
 * Data Transfer Object representing the configuration state of the application.
 */
public class AppConfig {
   private double sleepStart = 19.5;
   private double wakeUpTime = 5.5;
   private String sleepThreshold = "50%";
   private long sleepDelay = -1L;
   private boolean loadMessageEnabled = true;
   private boolean sleepEffectsEnabled = true;

   public double getSleepStart() {
      return sleepStart;
   }

   public void setSleepStart(double sleepStart) {
      if (sleepStart >= 0.0) this.sleepStart = sleepStart;
   }

   public double getWakeUpTime() {
      return wakeUpTime;
   }

   public void setWakeUpTime(double wakeUpTime) {
      if (wakeUpTime >= 0.0) this.wakeUpTime = wakeUpTime;
   }

   public String getSleepThreshold() {
      return sleepThreshold;
   }

   public void setSleepThreshold(String sleepThreshold) {
      this.sleepThreshold = sleepThreshold;
   }

   public long getSleepDelay() {
      return sleepDelay;
   }

   public void setSleepDelay(long sleepDelay) {
      this.sleepDelay = sleepDelay;
   }

   public boolean isLoadMessageEnabled() {
      return loadMessageEnabled;
   }

   public void setLoadMessageEnabled(boolean loadMessageEnabled) {
      this.loadMessageEnabled = loadMessageEnabled;
   }

   public boolean isSleepEffectsEnabled() {
      return sleepEffectsEnabled;
   }

   public void setSleepEffectsEnabled(boolean sleepEffectsEnabled) {
      this.sleepEffectsEnabled = sleepEffectsEnabled;
   }
}