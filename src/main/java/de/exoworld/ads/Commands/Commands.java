package de.exoworld.ads.Commands;

import de.exoworld.ads.Main;
import de.exoworld.ads.Manager.AdManager;
import de.exoworld.ads.Settings;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commands  implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Ad System


        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (command.getName().equalsIgnoreCase("ad")) {
                if(!sender.hasPermission(command.getPermission())) {
                    sender.sendMessage(Settings.getAdMessages("NoPermission"));
                    return false;
                }

                if (args.length == 0) {
                    AdManager.getInstance().sendAdInfo((Player) sender);
                    return true;
                }

                if (!AdManager.getInstance().adsAllowed()) {
                    p.sendMessage(PlaceholderAPI.setPlaceholders(p, Settings.getAdMessages("CurrentlyDisabled")));
                    return false;
                }

                long ts = new Timestamp(new Date().getTime()).getTime() / 1000;
                if ( ts < AdManager.getInstance().getLastAdPlayer().getOrDefault(((Player) sender).getUniqueId(), 0L) + Settings.getAdTime("TimeBetweenSamePlayer.Default"))  {
                    long count = AdManager.getInstance().getLastAdPlayer().getOrDefault(((Player) sender).getUniqueId(), 0L) + Settings.getAdTime("TimeBetweenSamePlayer.Default");
                    count -= ts;

                    String ms = Settings.getAdMessages("CooldownSamePlayer").replaceAll("%timeRemaining%", String.valueOf(count))   ;
                    ms = PlaceholderAPI.setPlaceholders(p, ms);
                    sender.sendMessage(ms);
                    return false;
                }
                if (ts < AdManager.lastAd + Settings.getAdTime("TimeBetween")) {
                    long count = AdManager.lastAd + Settings.getAdTime("TimeBetween");
                    count -= ts;

                    String ms = Settings.getAdMessages("Cooldown").replaceAll("%timeRemaining%", String.valueOf(count));
                    ms = PlaceholderAPI.setPlaceholders(p, ms);
                    sender.sendMessage(ms);
                    return false;
                }

                double price = AdManager.getInstance().calculatePrice(String.join("", args));
                if (Main.getEconomy().getBalance((Player) sender) < price) {

                    String ms = Settings.getAdMessages("NotEnoughMoney").replaceAll("%price%", String.valueOf(price));
                    ms = PlaceholderAPI.setPlaceholders(p, ms);
                    sender.sendMessage(ms);
                    return false;
                }

                Main.getEconomy().withdrawPlayer((Player) sender, price);
                AdManager.getInstance().sendAd((Player) sender, String.join(" ", args));
                AdManager.lastAd = ts;
                AdManager.getInstance().getLastAdPlayer().put(((Player) sender).getUniqueId(), ts);
                return true;
            }

            if (command.getName().equalsIgnoreCase("adreload")) {
                Settings.reloadSettings();
            }

            if (command.getName().equalsIgnoreCase("adadmin") && sender.hasPermission(command.getPermission())) {
                AdManager.getInstance().sendAd((Player) sender, String.join(" ", args));
            }

            if (command.getName().equalsIgnoreCase("adtoggle") && sender.hasPermission(command.getPermission())) {
                AdManager.getInstance().toggleAds(p);
            }

            if (command.getName().equalsIgnoreCase("adprice") && sender.hasPermission(command.getPermission())) {
                p.sendMessage(PlaceholderAPI.setPlaceholders(p, Settings.getAdMessages("CostsText")
                        .replaceAll("%price%", String.valueOf(AdManager.getInstance().calculatePrice(String.join("", args))))));
            }
        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
