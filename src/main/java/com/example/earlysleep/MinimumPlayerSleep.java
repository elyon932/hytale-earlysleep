package com.example.earlysleep;

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSleep;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSlumber;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSomnolence;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.SleepConfig;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class MinimumPlayerSleep extends CommandBase {
   private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
   private static volatile boolean schedulerStarted = false;
   private final Main plugin;

   public MinimumPlayerSleep(Main plugin) {
      super("sm", "Manages sleep requirements using percentage or fixed values", false);
      this.plugin = plugin;
      this.setAllowsExtraArguments(true);
      startScheduler(plugin);
   }

   private static synchronized void startScheduler(Main plugin) {
      if (!schedulerStarted) {
         schedulerStarted = true;
         SCHEDULER.scheduleAtFixedRate(() -> checkSleepCycles(plugin), 1L, 1L, TimeUnit.SECONDS);
      }
   }

   protected void executeSync(@Nonnull CommandContext context) {
      String input = context.getInputString();
      String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
      String[] parts = normalized.split("\\s+");
      String label = parts.length > 0 ? parts[0].replace("/", "") : "sm";
      if (!context.sender().hasPermission("earlysleep.admin")) {
         context.sendMessage(Message.raw("Permission denied.").color(Color.RED));
      } else if (parts.length < 2) {
         this.sendUsage(context, label);
      } else {
         if (parts[1].equals("minset")) {
            this.handleSet(context, parts);
         } else if (parts[1].equals("minget")) {
            this.handleGet(context);
         } else {
            this.sendUsage(context, label);
         }

      }
   }

   private void handleSet(CommandContext context, String[] parts) {
      if (parts.length < 3) {
         this.sendUsage(context, "minset");
      } else {
         String raw = parts[2];
         int online = getGlobalPlayerCount();

         try {
            if (raw.endsWith("%")) {
               int pct = Integer.parseInt(raw.replace("%", ""));
               if (pct < 0 || pct > 100) {
                  context.sendMessage(Message.raw("Percentage must be between 0 and 100.").color(Color.RED));
                  return;
               }

               this.plugin.sleepThreshold = raw;
               this.plugin.saveConfig((double)-1.0F, (double)-1.0F);
               context.sendMessage(Message.raw("[Sleep Manager] Minimum requirement set to: " + raw).color(Color.GREEN));
            } else {
               int abs = Integer.parseInt(raw);
               if (abs < 1 || abs > online) {
                  context.sendMessage(Message.raw("Value must be between 1 and " + online + " players online.").color(Color.RED));
                  return;
               }

               this.plugin.sleepThreshold = raw;
               this.plugin.saveConfig((double)-1.0F, (double)-1.0F);
               context.sendMessage(Message.raw("[Sleep Manager] Minimum requirement set to: " + abs + " players").color(Color.GREEN));
            }
         } catch (NumberFormatException var6) {
            context.sendMessage(Message.raw("Invalid format. Use numbers or %.").color(Color.RED));
         }

      }
   }

   private void handleGet(CommandContext context) {
      int online = getGlobalPlayerCount();
      String threshold = this.plugin.sleepThreshold;
      int displayAbs;
      int displayPct;
      if (threshold.endsWith("%")) {
         displayPct = Integer.parseInt(threshold.replace("%", ""));
         displayAbs = (int)Math.ceil((double)(displayPct * online) / (double)100.0F);
      } else {
         displayAbs = Integer.parseInt(threshold);
         displayPct = online > 0 ? displayAbs * 100 / online : 100;
      }

      String msg = "[Sleep Manager] Current threshold: " + displayPct + "% (" + displayAbs + " players) | Online: " + online;
      context.sendMessage(Message.raw(msg).color(Color.YELLOW));
   }

   private static void checkSleepCycles(Main plugin) {
      Universe universe = Universe.get();
      if (universe != null) {
         for(World world : universe.getWorlds().values()) {
            world.execute(() -> checkWorldSleep(world, plugin));
         }

      }
   }

   private static void checkWorldSleep(World world, Main plugin) {
      try {
         EntityStore entityStore = world.getEntityStore();
         if (entityStore == null) {
            return;
         }

         Store<EntityStore> store = entityStore.getStore();
         if (store == null) {
            return;
         }

         WorldTimeResource timeRes = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
         if (timeRes == null) {
            return;
         }

         SleepConfig sleepConfig = world.getGameplayConfig().getWorldConfig().getSleepConfig();
         LocalDateTime gameTime = LocalDateTime.ofInstant(timeRes.getGameTime(), ZoneOffset.UTC);
         if (sleepConfig != null && !sleepConfig.isWithinSleepHoursRange(gameTime)) {
            return;
         }

         WorldSomnolence worldSom = (WorldSomnolence)store.getResource(WorldSomnolence.getResourceType());
         if (worldSom == null) {
            return;
         }

         WorldSleep worldState = worldSom.getState();
         if (worldState instanceof WorldSlumber) {
            return;
         }

         Collection<PlayerRef> players = world.getPlayerRefs();
         if (players.isEmpty()) {
            return;
         }

         int sleepingCount = 0;

         for(PlayerRef p : players) {
            Ref<EntityStore> ref = p.getReference();
            if (ref != null) {
               PlayerSomnolence som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
               if (som != null) {
                  PlayerSleep state = som.getSleepState();
                  if (state instanceof PlayerSleep.Slumber) {
                     ++sleepingCount;
                  } else if (state instanceof PlayerSleep.NoddingOff) {
                     PlayerSleep.NoddingOff nodding = (PlayerSleep.NoddingOff)state;
                     if (Instant.now().isAfter(nodding.realTimeStart().plusMillis(getGlobalPlayerCount() == 1 ? 3200L : 1000L))) {
                        ++sleepingCount;
                     }
                  }
               }
            }
         }

         if (sleepingCount == 0) {
            return;
         }

         boolean thresholdMet;
         if (plugin.sleepThreshold.endsWith("%")) {
            int pct = Integer.parseInt(plugin.sleepThreshold.replace("%", ""));
            thresholdMet = sleepingCount * 100 / players.size() >= pct;
         } else {
            int abs = Integer.parseInt(plugin.sleepThreshold);
            thresholdMet = sleepingCount >= abs;
         }

         if (thresholdMet) {
            triggerSlumber(store, world, plugin);
         }
      } catch (Exception var17) {
      }

   }

   private static void triggerSlumber(Store<EntityStore> store, World world, Main plugin) {
      WorldSomnolence worldSom = (WorldSomnolence)store.getResource(WorldSomnolence.getResourceType());
      if (worldSom != null) {
         if (!(worldSom.getState() instanceof WorldSlumber)) {
            WorldTimeResource timeRes = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
            if (timeRes != null) {
               float wakeHour = (float)plugin.wakeUpTime;
               Instant now = timeRes.getGameTime();
               LocalDateTime ldt = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
               Instant wakeUpInstant = ldt.toLocalDate().plusDays(1L).atTime((int)wakeHour, (int)(wakeHour % 1.0F * 60.0F)).toInstant(ZoneOffset.UTC);
               timeRes.setGameTime(wakeUpInstant, world, store);

               for(PlayerRef p : world.getPlayerRefs()) {
                  Ref<EntityStore> ref = p.getReference();
                  if (ref != null) {
                     PlayerSomnolence som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
                     if (som != null) {
                        PlayerSleep state = som.getSleepState();
                        if (state instanceof PlayerSleep.NoddingOff || state instanceof PlayerSleep.Slumber) {
                           store.putComponent(ref, PlayerSomnolence.getComponentType(), new PlayerSomnolence(new PlayerSleep.MorningWakeUp(wakeUpInstant)));
                        }
                     }
                  }
               }

            }
         }
      }
   }

   private static int getGlobalPlayerCount() {
      int count = 0;
      Universe universe = Universe.get();
      if (universe != null) {
         for(World w : universe.getWorlds().values()) {
            count += w.getPlayerRefs().size();
         }
      }

      return Math.max(1, count);
   }

   private void sendUsage(CommandContext context, String label) {
      String msg = label.equals("minset") ? "/sm minset <value>" : "/sm minset <value> or /sm minget";
      context.sendMessage(Message.raw("Usage: " + msg).color(Color.RED));
   }
}
