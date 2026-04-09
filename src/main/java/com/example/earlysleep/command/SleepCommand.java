package com.example.earlysleep.command;

import com.example.earlysleep.config.SleepConfig;
import com.example.earlysleep.service.SleepManager;
import com.example.earlysleep.util.ChatUtil;
import com.example.earlysleep.util.TimeParser;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import java.awt.Color;
import java.util.Locale;
import javax.annotation.Nonnull;

public class SleepCommand extends CommandBase {
    private final SleepManager manager;
    private final SleepConfig config;

    public SleepCommand(SleepManager manager, SleepConfig config) {
        super("sm", "Manages sleep schedule and requirements", false);
        this.manager = manager;
        this.config = config;
        this.setAllowsExtraArguments(true);
    }

    protected void executeSync(@Nonnull CommandContext context) {
        String input = context.getInputString();
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
        String[] parts = normalized.split("\\s+");
        if (!context.sender().hasPermission("earlysleep.admin")) {
            context.sendMessage(ChatUtil.createMessage("[EarlySleep] Permission denied.", Color.RED));
        } else if (parts.length < 2) {
            this.sendUsage(context, "sm");
        } else {
            switch (parts[1]) {
                case "sleep":
                    if (parts.length >= 3) {
                        this.handleTimeChange(context, parts[2], true);
                    } else {
                        this.sendUsage(context, "sm");
                    }
                    break;
                case "wake":
                    if (parts.length >= 3) {
                        this.handleTimeChange(context, parts[2], false);
                    } else {
                        this.sendUsage(context, "sm");
                    }
                    break;
                case "delay":
                    this.handleDelay(context, parts);
                    break;
                case "player":
                    this.handleSet(context, parts);
                    break;
                case "loadmsg":
                    this.handleToggle(context, parts, "loadMessage");
                    break;
                case "effects":
                    this.handleToggle(context, parts, "effects");
                    break;
                case "status":
                    this.handleGet(context);
                    break;
                case "help":
                    this.handleHelp(context);
                    break;
                default:
                    this.sendUsage(context, "sm");
            }
        }
    }

    private void handleToggle(CommandContext context, String[] parts, String type) {
        if (parts.length < 3) {
            this.sendUsage(context, parts[1]);
        } else {
            String input = parts[2].toLowerCase();
            if (!input.equals("on") && !input.equals("off") && !input.equals("true") && !input.equals("false")) {
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] Use 'on' or 'off'.", Color.RED));
            } else {
                boolean val = input.equals("on") || input.equals("true");
                if (type.equals("loadMessage")) {
                    this.config.loadMessageEnabled = val;
                } else {
                    this.config.sleepEffectsEnabled = val;
                }

                this.config.save((double)-1.0F, (double)-1.0F);
                String state = val ? "ENABLED" : "DISABLED";
                String var10001 = parts[1].toUpperCase();
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] " + var10001 + " is now: " + state, val ? Color.GREEN : Color.RED));
            }
        }
    }

    private void handleDelay(CommandContext context, String[] parts) {
        if (parts.length >= 3 && parts[2].equalsIgnoreCase("status")) {
            long current = this.config.sleepDelay == -1L ? (this.manager.getGlobalPlayerCount() == 1 ? 4000L : 0L) : this.config.sleepDelay;
            String mode = this.config.sleepDelay == -1L ? " (Auto)" : " (Manual)";
            context.sendMessage(ChatUtil.createMessage("[EarlySleep] Current sleep delay: " + current + "ms" + mode, Color.YELLOW));
        } else if (parts.length < 3) {
            this.sendUsage(context, "delay");
        } else {
            try {
                long value = Long.parseLong(parts[2]);
                if (value < 0L || value > 4000L) {
                    context.sendMessage(ChatUtil.createMessage("[EarlySleep] Delay must be between 0 and 4000ms.", Color.RED));
                    return;
                }

                this.config.sleepDelay = value;
                this.config.save((double)-1.0F, (double)-1.0F);
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] Sleep delay set to: " + value + "ms", Color.GREEN));
            } catch (NumberFormatException var6) {
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] Invalid number. Use a value between 0 and 4000.", Color.RED));
            }
        }
    }

    private void handleTimeChange(CommandContext context, String timeStr, boolean isSleep) {
        double hours = TimeParser.parseTimeToDouble(timeStr);
        if (timeStr.equalsIgnoreCase("status")) {
            double val = isSleep ? this.config.sleepStart : this.config.wakeUpTime;
            String time = TimeParser.formatTime(val);
            String type = isSleep ? "sleep start" : "wake up";
            context.sendMessage(ChatUtil.createMessage("[EarlySleep] Current " + type + " time: " + time, Color.YELLOW));
        } else if (hours < (double)0.0F) {
            context.sendMessage(ChatUtil.createMessage("[EarlySleep] Invalid format. Use HH:mm (e.g. 14:30) or Hour (0-23).", Color.RED));
        } else {
            String formattedTime = TimeParser.formatTime(hours);
            if (isSleep) {
                this.config.save(hours, (double)-1.0F);
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] Sleep time set to: " + formattedTime, Color.GREEN));
            } else {
                this.config.save((double)-1.0F, hours);
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] Wake time set to: " + formattedTime, Color.GREEN));
            }

            this.manager.modifyActiveWorldSleepConfigs();
        }
    }

    private void handleSet(CommandContext context, String[] parts) {
        if (parts.length < 3) {
            this.sendUsage(context, "player");
        } else {
            String raw = parts[2];
            int online = this.manager.getGlobalPlayerCount();

            try {
                if (raw.endsWith("%")) {
                    int pct = Integer.parseInt(raw.replace("%", ""));
                    if (pct < 0 || pct > 100) {
                        context.sendMessage(ChatUtil.createMessage("[EarlySleep] Percentage must be between 0 and 100%.", Color.RED));
                        return;
                    }
                } else {
                    int abs = Integer.parseInt(raw);
                    if (abs < 1 || abs > online) {
                        context.sendMessage(ChatUtil.createMessage("[EarlySleep] Value must be between 1 and " + online + " (players online).", Color.RED));
                        return;
                    }
                }

                this.config.sleepThreshold = raw;
                this.config.save((double)-1.0F, (double)-1.0F);
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] Minimum requirement set to: " + raw, Color.GREEN));
            } catch (NumberFormatException var6) {
                context.sendMessage(ChatUtil.createMessage("[EarlySleep] Invalid format. Use numbers (e.g. 2) or percentage (e.g. 50%).", Color.RED));
            }
        }
    }

    private void handleGet(CommandContext context) {
        int online = this.manager.getGlobalPlayerCount();
        String threshold = this.config.sleepThreshold;
        int displayAbs;
        int displayPct;
        if (threshold.endsWith("%")) {
            displayPct = Integer.parseInt(threshold.replace("%", ""));
            displayAbs = (int)Math.ceil((double)(displayPct * online) / (double)100.0F);
        } else {
            displayAbs = Integer.parseInt(threshold);
            displayPct = online > 0 ? displayAbs * 100 / online : 100;
        }

        String msg = "[Early Sleep] Current threshold: " + displayPct + "% (" + displayAbs + " players). Effects: " + (this.config.sleepEffectsEnabled ? "ON" : "OFF");
        context.sendMessage(ChatUtil.createMessage(msg, Color.YELLOW));
    }

    private void handleHelp(CommandContext context) {
        context.sendMessage(ChatUtil.createMessage("--- EarlySleep Command List ---", Color.CYAN));
        context.sendMessage(ChatUtil.createMessage("/sm sleep <HH:mm> - Set start sleep time.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm wake <HH:mm> - Set wake up time.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm player <val/%> - Set sleep requirements.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm delay <ms> - Set transition delay (0-4000).", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm loadmsg <on/off> - Toggle mod load message.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm effects <on/off> - Toggle heal/stamina on wake up.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm sleep status - Show current sleep start time.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm wake status - Show current wake up time.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm delay status - Show current transition delay.", Color.YELLOW));
        context.sendMessage(ChatUtil.createMessage("/sm status - Show current settings of minimum players.", Color.YELLOW));
    }

    private void sendUsage(CommandContext context, String label) {
        String msg = label.equals("player") ? "/sm player <value>" : (label.equals("loadmsg") ? "/sm loadmsg <on/off>" : (label.equals("effects") ? "/sm effects <on/off>" : "/sm help"));
        context.sendMessage(ChatUtil.createMessage("[EarlySleep] Usage: " + msg, Color.RED));
    }
}