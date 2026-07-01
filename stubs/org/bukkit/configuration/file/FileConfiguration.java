package org.bukkit.configuration.file;

import org.bukkit.configuration.Configuration;

public class FileConfiguration implements Configuration {
    public String getString(String path, String def) { return def; }
    public String getString(String path) { return null; }
    public boolean getBoolean(String path, boolean def) { return def; }
    public boolean getBoolean(String path) { return false; }
}
