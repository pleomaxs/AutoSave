/**
 * Copyright 2011 Morgan Humes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package sh.cereal.bukkit.plugin.AutoSave;

import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

public class AutoSaveThread extends Thread {

    protected final Logger log = Logger.getLogger("Minecraft");
    private boolean run = true;
    private AutoSave plugin = null;
    private AutoSaveConfig config = null;

    // Constructor to define number of seconds to sleep
    AutoSaveThread(AutoSave plugin, AutoSaveConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    // Allows for the thread to naturally exit if value is false
    public void setRun(boolean run) {
        this.run = run;
    }

    // The code to run...weee
    public void run() {
        if (config == null) {
            return;
        }

        log.info(String.format("[%s] AutoSaveThread Started: Interval is %d seconds, Warn Times are %s", plugin.getDescription().getName(), config.varInterval, Generic.join(",", config.varWarnTimes)));
        while (run) {
            // Do our Sleep stuff!
            for (int i = 0; i < config.varInterval; i++) {
                try {
                    if (!run) {
                        if (config.varDebug) {
                            log.info(String.format("[%s] Graceful quit of AutoSaveThread", plugin.getDescription().getName()));
                        }
                        return;
                    }
                    boolean warn = false;
                    for (int w : config.varWarnTimes) {
                        if (w != 0 && w + i == config.varInterval) {
                            warn = true;
                        }
                    }

                    if (warn) {
                        // Perform warning
                        if (config.varDebug) {
                            log.info(String.format("[%s] Warning Time Reached: %d seconds to go.", plugin.getDescription().getName(), config.varInterval - i));
                        }
                        plugin.getServer().broadcastMessage(config.messageWarning);
                        log.info(String.format("[%s] %s", plugin.getDescription().getName(), config.messageWarning));
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.info("Could not sleep!");
                }
            }

            // getServer().savePlayers() & World.save() are not listed as
            // explicitly threadsafe. Note; getServer().getWorlds() isn't
            // threadsafe either.
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                public void run() {
                    plugin.performSave();
                    plugin.lastSave = new Date();
                }
            });

        }
    }

}
