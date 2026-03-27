package com.example.earlysleep;

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep.MorningWakeUp;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep.NoddingOff;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep.Slumber;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class SleepManagerCommand extends CommandBase {
   private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
   private static volatile boolean schedulerStarted = false;
   private final Main plugin;

   public SleepManagerCommand(Main plugin) {
      super("sm", "Manages sleep schedule and requirements", false);
      this.plugin = plugin;
      this.setAllowsExtraArguments(true);
      startScheduler(plugin);
   }

   private static synchronized void startScheduler(Main plugin) {
      if (!schedulerStarted) {
         schedulerStarted = true;
         SCHEDULER.scheduleAtFixedRate(() -> {
            checkSleepCycles(plugin);
         }, 1L, 1L, TimeUnit.SECONDS);
      }
   }

   protected void executeSync(@Nonnull CommandContext context) {
      String input = context.getInputString();
      String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
      String[] parts = normalized.split("\\s+");
      String label = "sm";
      if (!context.sender().hasPermission("earlysleep.admin")) {
         context.sendMessage(Message.raw("Permission denied.").color(Color.RED));
      } else if (parts.length < 2) {
         this.sendUsage(context, label);
      } else {
         String var6 = parts[1];
         byte var7 = -1;
         switch(var6.hashCode()) {
         case -985752863:
            if (var6.equals("player")) {
               var7 = 3;
            }
            break;
         case -892481550:
            if (var6.equals("status")) {
               var7 = 4;
            }
            break;
         case 3198785:
            if (var6.equals("help")) {
               var7 = 5;
            }
            break;
         case 3641764:
            if (var6.equals("wake")) {
               var7 = 1;
            }
            break;
         case 95467907:
            if (var6.equals("delay")) {
               var7 = 2;
            }
            break;
         case 109522647:
            if (var6.equals("sleep")) {
               var7 = 0;
            }
         }

         switch(var7) {
         case 0:
            if (parts.length >= 3) {
               this.handleTimeChange(context, parts[2], true);
            } else {
               this.sendUsage(context, label);
            }
            break;
         case 1:
            if (parts.length >= 3) {
               this.handleTimeChange(context, parts[2], false);
            } else {
               this.sendUsage(context, label);
            }
            break;
         case 2:
            this.handleDelay(context, parts);
            break;
         case 3:
            this.handleSet(context, parts);
            break;
         case 4:
            this.handleGet(context);
            break;
         case 5:
            this.handleHelp(context);
            break;
         default:
            this.sendUsage(context, label);
         }

      }
   }

   private void handleDelay(CommandContext context, String[] parts) {
      if (parts.length < 3) {
         this.sendUsage(context, "delay");
      } else {
         try {
            long value = Long.parseLong(parts[2]);
            if (value < 1000L || value > 4000L) {
               context.sendMessage(Message.raw("Delay must be between 1000 and 4000ms.").color(Color.RED));
               return;
            }

            this.plugin.sleepDelay = value;
            this.plugin.saveConfig(-1.0D, -1.0D);
            context.sendMessage(Message.raw("[Sleep Manager] Sleep delay set to: " + value + "ms").color(Color.GREEN));
         } catch (NumberFormatException var5) {
            context.sendMessage(Message.raw("Invalid number format.").color(Color.RED));
         }

      }
   }

   private void handleHelp(CommandContext context) {
      context.sendMessage(Message.raw("--- Sleep Manager Command List ---").color(Color.CYAN));
      context.sendMessage(Message.raw("/sm sleep <HH:mm> - Set the time players can start sleeping.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm wake <HH:mm> - Set the time players wake up.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm delay <ms> - Set custom sleep transition delay (1000-4000).").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm player <val/%> - Set minimum player requirement to skip night.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm status - Show current sleep requirement settings.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm help - Display this professional help menu.").color(Color.YELLOW));
   }

   private void handleTimeChange(CommandContext context, String timeStr, boolean isSleep) {
      double hours = this.parseTimeToDouble(timeStr);
      if (hours < 0.0D) {
         context.sendMessage(Message.raw("Invalid format (HH:mm).").color(Color.RED));
      } else {
         if (isSleep) {
            this.plugin.saveConfig(hours, -1.0D);
            context.sendMessage(Message.raw("Sleep time permanently set to: " + timeStr).color(Color.GREEN));
         } else {
            this.plugin.saveConfig(-1.0D, hours);
            context.sendMessage(Message.raw("Wake time permanently set to: " + timeStr).color(Color.GREEN));
         }

         this.plugin.modifyActiveWorldSleepConfigs();
      }

   }

   private double parseTimeToDouble(String timeStr) {
      try {
         String[] parts = timeStr.split(":");
         int h = Integer.parseInt(parts[0]);
         int m = Integer.parseInt(parts[1]);
         return h >= 0 && h <= 23 && m >= 0 && m <= 59 ? (double)h + (double)m / 60.0D : -1.0D;
      } catch (Exception var5) {
         return -1.0D;
      }
   }

   private void handleSet(CommandContext context, String[] parts) {
      if (parts.length < 3) {
         this.sendUsage(context, "player");
      } else {
         String raw = parts[2];
         int online = getGlobalPlayerCount();

         try {
            int abs;
            if (raw.endsWith("%")) {
               abs = Integer.parseInt(raw.replace("%", ""));
               if (abs < 0 || abs > 100) {
                  context.sendMessage(Message.raw("Percentage must be between 0 and 100.").color(Color.RED));
                  return;
               }

               this.plugin.sleepThreshold = raw;
               this.plugin.saveConfig(-1.0D, -1.0D);
               context.sendMessage(Message.raw("[Sleep Manager] Minimum requirement set to: " + raw).color(Color.GREEN));
            } else {
               abs = Integer.parseInt(raw);
               if (abs < 1 || abs > online) {
                  context.sendMessage(Message.raw("Value must be between 1 and " + online + " players online.").color(Color.RED));
                  return;
               }

               this.plugin.sleepThreshold = raw;
               this.plugin.saveConfig(-1.0D, -1.0D);
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
         displayAbs = (int)Math.ceil((double)(displayPct * online) / 100.0D);
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
         Iterator var2 = universe.getWorlds().values().iterator();

         while(var2.hasNext()) {
            World world = (World)var2.next();
            world.execute(() -> {
               checkWorldSleep(world, plugin);
            });
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

         if (worldSom.getState() instanceof WorldSlumber) {
            return;
         }

         Collection<PlayerRef> players = world.getPlayerRefs();
         if (players.isEmpty()) {
            return;
         }

         int sleepingCount = 0;
         long currentDelay = plugin.sleepDelay != -1L ? plugin.sleepDelay : (getGlobalPlayerCount() == 1 ? 3200L : 1000L);
         Iterator var12 = players.iterator();

         while(var12.hasNext()) {
            PlayerRef p = (PlayerRef)var12.next();
            Ref<EntityStore> ref = p.getReference();
            if (ref != null) {
               PlayerSomnolence som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
               if (som != null) {
                  PlayerSleep state = som.getSleepState();
                  if (state instanceof Slumber) {
                     ++sleepingCount;
                  } else if (state instanceof NoddingOff) {
                     NoddingOff nodding = (NoddingOff)state;
                     if (Instant.now().isAfter(nodding.realTimeStart().plusMillis(currentDelay))) {
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
         int pct;
         if (plugin.sleepThreshold.endsWith("%")) {
            pct = Integer.parseInt(plugin.sleepThreshold.replace("%", ""));
            thresholdMet = sleepingCount * 100 / players.size() >= pct;
         } else {
            pct = Integer.parseInt(plugin.sleepThreshold);
            thresholdMet = sleepingCount >= pct;
         }

         if (thresholdMet) {
            triggerSlumber(store, world, plugin);
         }
      } catch (Exception var18) {
      }

   }

   private static void triggerSlumber(Store<EntityStore> store, World world, Main plugin) {
      WorldSomnolence worldSom = (WorldSomnolence)store.getResource(WorldSomnolence.getResourceType());
      if (worldSom != null && !(worldSom.getState() instanceof WorldSlumber)) {
         WorldTimeResource timeRes = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
         if (timeRes != null) {
            float wakeHour = (float)plugin.wakeUpTime;
            Instant now = timeRes.getGameTime();
            LocalDateTime ldt = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
            Instant wakeUpInstant = ldt.toLocalDate().plusDays(1L).atTime((int)wakeHour, (int)(wakeHour % 1.0F * 60.0F)).toInstant(ZoneOffset.UTC);
            timeRes.setGameTime(wakeUpInstant, world, store);
            Iterator var9 = world.getPlayerRefs().iterator();

            while(true) {
               Ref ref;
               PlayerSleep state;
               do {
                  PlayerSomnolence som;
                  do {
                     do {
                        if (!var9.hasNext()) {
                           return;
                        }

                        PlayerRef p = (PlayerRef)var9.next();
                        ref = p.getReference();
                     } while(ref == null);

                     som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
                  } while(som == null);

                  state = som.getSleepState();
               } while(!(state instanceof NoddingOff) && !(state instanceof Slumber));

               store.putComponent(ref, PlayerSomnolence.getComponentType(), new PlayerSomnolence(new MorningWakeUp(wakeUpInstant)));
            }
         }
      }
   }

   private static int getGlobalPlayerCount() {
      int count = 0;
      Universe universe = Universe.get();
      World w;
      if (universe != null) {
         for(Iterator var2 = universe.getWorlds().values().iterator(); var2.hasNext(); count += w.getPlayerRefs().size()) {
            w = (World)var2.next();
         }
      }

      return Math.max(1, count);
   }

   private void sendUsage(CommandContext context, String label) {
      String msg;
      if (label.equals("player")) {
         msg = "/sm player <value>";
      } else if (label.equals("delay")) {
         msg = "/sm delay <1000-4000>";
      } else {
         msg = "/sm help to see available commands.";
      }

      context.sendMessage(Message.raw("Usage: " + msg).color(Color.RED));
   }
}