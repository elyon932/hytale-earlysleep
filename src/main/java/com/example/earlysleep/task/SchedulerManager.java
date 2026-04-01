package com.example.earlysleep.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Centralized executor service wrapper representing the background thread environment.
 */
public class SchedulerManager {

   private final ScheduledExecutorService executorService;

   public SchedulerManager() {
      this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
         Thread t = new Thread(r, "EarlySleep-Task-Thread");
         t.setDaemon(true);
         return t;
      });
   }

   public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
      executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
   }

   public void schedule(Runnable command, long delay, TimeUnit unit) {
      executorService.schedule(command, delay, unit);
   }
   
   public void shutdown() {
      executorService.shutdown();
   }
}