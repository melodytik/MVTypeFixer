package org.bukkit.plugin.java;

import org.bukkit.plugin.Plugin;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.logging.Logger;

public class JavaPlugin implements Plugin {
    public FileConfiguration getConfig() { return null; }
    public void saveDefaultConfig() { }
    public void reloadConfig() { }
    public Server getServer() { return null; }
    public Logger getLogger() { return null; }
    public void onEnable() { }
    public void onDisable() { }
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) { return false; }
}
