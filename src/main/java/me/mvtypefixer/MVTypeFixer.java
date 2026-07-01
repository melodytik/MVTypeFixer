package me.mvtypefixer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * MVTypeFixer - Automatically adds a world type parameter to
 * Multiverse-Core "mv create" commands.
 *
 * Intercepts both player commands (PlayerCommandPreprocessEvent) and
 * console/command-block commands (ServerCommandEvent).
 */
public class MVTypeFixer extends JavaPlugin implements Listener {

    private String worldType;
    private boolean forceType;
    private boolean debug;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MVTypeFixer enabled! Default world type: " + worldType);
    }

    @Override
    public void onDisable() {
        getLogger().info("MVTypeFixer disabled!");
    }

    private void loadConfigValues() {
        reloadConfig();
        worldType = getConfig().getString("world-type", "FLAT").toUpperCase();
        forceType = getConfig().getBoolean("force-type", false);
        debug = getConfig().getBoolean("debug", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            loadConfigValues();
            sender.sendMessage(ChatColor.GREEN + "[MVTypeFixer] Configuration reloaded!");
            sender.sendMessage(ChatColor.GREEN + "  World type: " + worldType);
            sender.sendMessage(ChatColor.GREEN + "  Force type: " + forceType);
            sender.sendMessage(ChatColor.GREEN + "  Debug: " + debug);
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(ChatColor.GREEN + "[MVTypeFixer] Current settings:");
            sender.sendMessage(ChatColor.GREEN + "  World type: " + worldType);
            sender.sendMessage(ChatColor.GREEN + "  Force type: " + forceType);
            sender.sendMessage(ChatColor.GREEN + "  Debug: " + debug);
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "[MVTypeFixer] Usage: /mvtypefixer <reload|info>");
        return true;
    }

    // ------------------------------------------------------------------
    //  Event listeners
    // ------------------------------------------------------------------

    /**
     * Intercept commands typed by players.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String result = processCommand(event.getMessage());
        if (result != null) {
            if (debug) {
                getLogger().info("[Player] " + event.getMessage() + "  ->  " + result);
            }
            event.setMessage(result);
        }
    }

    /**
     * Intercept commands from console / command blocks / other non-player senders.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        String result = processCommand(event.getCommand());
        if (result != null) {
            if (debug) {
                getLogger().info("[Server] " + event.getCommand() + "  ->  " + result);
            }
            event.setCommand(result);
        }
    }

    // ------------------------------------------------------------------
    //  Core logic
    // ------------------------------------------------------------------

    /**
     * Inspect a raw command string. If it is a Multiverse-Core "create"
     * command that lacks (or, when force-type is on, has) a -t flag,
     * return the modified command; otherwise return null.
     *
     * @param command raw command (player commands include leading '/')
     * @return modified command or null
     */
    private String processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return null;
        }

        String trimmed = command.trim();
        boolean hasSlash = trimmed.startsWith("/");
        String withoutSlash = hasSlash ? trimmed.substring(1) : trimmed;

        // Split on whitespace
        String[] parts = withoutSlash.split("\\s+");
        if (parts.length < 3) {
            return null;  // need at least "mv create <world>"
        }

        // --- Identify whether this is a Multiverse-Core command ---

        String baseCmd = parts[0].toLowerCase();

        // Strip namespace prefix (e.g. "multiverse-core:mv" -> "mv")
        String actualCmd = baseCmd;
        if (baseCmd.contains(":")) {
            actualCmd = baseCmd.substring(baseCmd.indexOf(":") + 1);
        }

        if (!actualCmd.equals("mv") && !actualCmd.equals("mvp") && !actualCmd.equals("multiverse-core")) {
            return null;
        }

        // --- Check the sub-command is "create" ---
        String subCmd = parts[1].toLowerCase();
        if (!subCmd.equals("create")) {
            return null;
        }

        // --- Search for existing -t / --type flag ---
        int typeFlagIndex = -1;
        for (int i = 2; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("-t") || parts[i].equalsIgnoreCase("--type")) {
                typeFlagIndex = i;
                break;
            }
        }

        if (typeFlagIndex != -1 && !forceType) {
            // A type flag is already present and we are not forcing — leave it alone.
            if (debug) {
                getLogger().info("Command already has -t flag, leaving unchanged: " + trimmed);
            }
            return null;
        }

        if (typeFlagIndex != -1 && forceType) {
            // Remove the existing -t flag and its value, then append our own.
            List<String> newParts = new ArrayList<>();
            for (int i = 0; i < parts.length; i++) {
                if (i == typeFlagIndex) {
                    i++;  // skip the flag ...
                    continue; // ... and its value
                }
                newParts.add(parts[i]);
            }
            newParts.add("-t");
            newParts.add(worldType);
            String result = String.join(" ", newParts);
            return hasSlash ? "/" + result : result;
        }

        // No type flag found — simply append "-t <worldType>"
        String result = withoutSlash + " -t " + worldType;
        return hasSlash ? "/" + result : result;
    }
}
