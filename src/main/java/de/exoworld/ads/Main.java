package de.exoworld.ads;

import de.exoworld.ads.Commands.Commands;
import de.exoworld.ads.Manager.AdManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    private static final Logger log = Logger.getLogger("eXoAds");
    private static Main mainInstance;
    private static Economy econ;
    private Commands commandHandler = new Commands();
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Vault API nicht gefunden!", getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        mainInstance = this;
        new Settings();


        createManager();
        createCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }



    private void createManager() {
        new AdManager();
    }

    private void createCommands() {
        getCommand("ad").setExecutor(commandHandler);
        getCommand("adreload").setExecutor(commandHandler);
        getCommand("adadmin").setExecutor(commandHandler);
        getCommand("adtoggle").setExecutor(commandHandler);
        getCommand("adprice").setExecutor(commandHandler);
    }
    public static Economy getEconomy() {
        return econ;
    }
    public static Main getInstance() {
        return mainInstance;
    }
}
