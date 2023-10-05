package de.exoworld.ads.Manager;

import de.exoworld.ads.Settings;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;


//TODO     # At first, it checks for a value in the permission "Ad.Cooldown.SamePlayer"
//    # if it finds one it will use this value otherwise it will use the default value
public class AdManager {
    public static AdManager instance;
    public static long lastAd = 0;
    public boolean allowed = true;
    public Map<UUID, Long> lastAdPlayer = new HashMap<>();
    public AdManager() {
        instance = this;
    }

    public void sendAd(Player from, String text) {
        String tempText = formatAd(text);
        for (String s : Settings.getAdStringList("Text")) {
            s = PlaceholderAPI.setPlaceholders(from, s);
            String temp = s.replaceAll("%username%", from.getName()).replace("%adText%", tempText);
            Bukkit.broadcast(Component.text(temp));
        }
    }

    public void sendAdInfo(Player to) {

        for (String s : Settings.getAdStringList("Info")) {
            s = PlaceholderAPI.setPlaceholders(to, s);
            if (s.contentEquals("%allowedColors%")) {
                for (String cs : getPriceFromAllColorsAsText(to)) {
                    to.sendMessage(Component.text(cs));
                }
                continue;
            }
            to.sendMessage(Component.text(s));
        }
    }

    public List<String> getPriceFromAllColorsAsText(Player p) {
        List<String> temp = new ArrayList<>();
        String text = Settings.getAdMessages("ColorPricesText");
        for (Map.Entry<String, Boolean> e : Settings.getAllowedColors().entrySet()) {
            if (e.getValue()) {
                char colorChar = ChatColor.valueOf(e.getKey().toUpperCase()).getChar();
                temp.add(PlaceholderAPI.setPlaceholders(p,
                        text.replaceAll("%colorDisplayName%", Settings.getColorDisplayName(e.getKey(), false))
                                .replaceAll("%colorCode%", "&" + colorChar)
                                .replaceAll("%colorName%", e.getKey())
                                .replaceAll("%color%", ChatColor.getByChar(colorChar).toString())
                                .replaceAll("%price%", String.valueOf(Settings.getAdPrice(e.getKey(), true)))));
            }
        }
        return temp;
    }

    public double getPriceFromColor(String color, boolean convert) {
        String tempColor = color;
        if (convert) {
            tempColor = Settings.colorsAndDecos.get(color);
        }

        return Settings.getAdPrice(tempColor, true);
    }

    public boolean isColorAllowed(String color, boolean convert) {
        String tempColor = color;

        if (convert) {
            tempColor = Settings.colorsAndDecos.get(color.charAt(0) == '&' ? color : "&" + color);
        }

        return Settings.getAllowedColors().getOrDefault(tempColor, false);
    }


    public String formatAd(String text) {
        StringBuilder newText = new StringBuilder();

        String[] splitText = text.split("&");

        if (!text.contains("&")) {
            return text;
        }

        for (String s : splitText) {
            if (s.length() == 0) {
                continue;
            }
            if (s.charAt(0) == 'r') {
                newText.append(ChatColor.translateAlternateColorCodes('&', "&" + s));
            } else if (Settings.colors.get("&" + s.charAt(0)) != null) {
                if (isColorAllowed("&" + s.charAt(0), true)) {
                    newText.append(ChatColor.translateAlternateColorCodes('&', "&" + s));

                    continue;
                }
                newText.append("&" + s);

            } else if (Settings.decorations.get("&" + s.charAt(0)) != null) {
                if (isColorAllowed("&" + s.charAt(0), true)) {
                    newText.append(ChatColor.translateAlternateColorCodes('&', "&" + s));

                    continue;
                }
                newText.append("&" + s);

            } else {
                newText.append("&" + s);
            }

        }
        return newText.toString();
    }

    public double calculatePrice(String text) {
        double price = Settings.getAdPrice("BasePrice", false);
        double multiplicator = Settings.getAdPrice("PricePerChar", false);
        String lastColor = "";
        String lastType = "";

        if (!text.contains("&")) {
            return price + text.replace(" ", "").length() * Settings.getAdPrice("PricePerChar", false);
        }

        List<String> decos = new ArrayList<>();
        String[] splitText = text.split("&");
        for (String s : splitText) {
            if (s.length() == 0) {
                continue;
            }
            int length = s.replace(" ", "").length() + 1;
            if (s.charAt(0) == 'r') {
                multiplicator = Settings.getAdPrice("PricePerChar", false);
                decos.clear();
                lastColor = "";
            } else if (Settings.colors.get("&" + s.charAt(0)) != null) {
                double colorPrice = getPriceFromColor("&" + s.charAt(0), true);

                if (isColorAllowed(String.valueOf(s.charAt(0)), true)) {
                    if (!lastColor.equals("")) {
                        multiplicator -= Settings.getColorPrice("&" + lastColor, true);
                    }

                    if (!lastType.equals("") && lastType.equalsIgnoreCase("deco")) {
                        for (String d : decos) {
                            multiplicator -= Settings.getColorPrice(d, true);
                        }
                        decos.clear();
                    }

                    lastType = "color";
                    lastColor = String.valueOf(s.charAt(0));
                    multiplicator += colorPrice;
                    length -= 2;
                }

            } else if (Settings.decorations.get("&" + s.charAt(0)) != null) {
                if (isColorAllowed(String.valueOf(s.charAt(0)), true)) {
                    if (!decos.contains("&" + s.charAt(0))) {
                        lastType = "deco";
                        decos.add("&" + s.charAt(0));
                        multiplicator += Settings.getColorPrice("&" + s.charAt(0), true);
                        length -= 2;
                    }
                }

            }

            price += length * multiplicator;
        }
        return price;
    }

    public void toggleAds(Player p ) {
        String textAdmin = allowed ? Settings.getAdMessages("AdsDisabledInfoAdmin") : Settings.getAdMessages("AdsEnabledInfoAdmin");
        String textUser = allowed ? Settings.getAdMessages("AdsDisabledInfo") : Settings.getAdMessages("AdsEnabledInfo");
        String text;
        for (Player oPlayer : Bukkit.getOnlinePlayers()) {
            if (oPlayer.hasPermission("exoworld.ad.admin.infos")) {
                text = textAdmin;
            } else if (Settings.getAdBools("PublicAnnounceIfGetsDisabled")) {
                text = textUser;
            } else {
                continue;
            }
            text = PlaceholderAPI.setPlaceholders(oPlayer, text.replaceAll("%admin%", p.getName()));
            oPlayer.sendMessage(Component.text(text));
        }
        allowed = !allowed;
    }

    public Map<UUID, Long> getLastAdPlayer() {
        return lastAdPlayer;
    }

    public boolean adsAllowed() {
        return allowed;
    }
    public static AdManager getInstance() {
        return instance;
    }
}
