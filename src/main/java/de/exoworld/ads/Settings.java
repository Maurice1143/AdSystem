package de.exoworld.ads;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {
    private static FileConfiguration config;

    private static final Map<String, Double> adPriceCache = new HashMap<>();
    private static final Map<String, String> adStringCache = new HashMap<>();
    private static final Map<String, List<String>> adStringListCache  = new HashMap<>();
    private static final Map<String, Integer> adTimeCache = new HashMap<>();
    private static final Map<String, Boolean> adAllowedCache = new HashMap<>();
    private static final Map<String, String> adColorDisplayName = new HashMap<>();

    public static final Map<String, String> colorsAndDecos = new HashMap<>() {{
        put("&0", "black");
        put("&1", "dark_blue");
        put("&2", "dark_green");
        put("&3", "dark_aqua");
        put("&4", "dark_red");
        put("&5", "dark_purple");
        put("&6", "gold");
        put("&7", "gray");
        put("&8", "dark_gray");
        put("&9", "blue");
        put("&a", "green");
        put("&b", "aqua");
        put("&c", "red");
        put("&d", "light_purple");
        put("&e", "yellow");
        put("&f", "white");
        put("&l", "bold");
        put("&m", "strikethrough");
        put("&n", "underline");
        put("&o", "italic");
    }};

    public static final Map<String, String> colors = new HashMap<>() {{
        put("&0", "black");
        put("&1", "dark_blue");
        put("&2", "dark_green");
        put("&3", "dark_aqua");
        put("&4", "dark_red");
        put("&5", "dark_purple");
        put("&6", "gold");
        put("&7", "gray");
        put("&8", "dark_gray");
        put("&9", "blue");
        put("&a", "green");
        put("&b", "aqua");
        put("&c", "red");
        put("&d", "light_purple");
        put("&e", "yellow");
        put("&f", "white");
    }};

    public static final Map<String, String> decorations = new HashMap<>() {{
        put("&l", "bold");
        put("&m", "strikethrough");
        put("&n", "underline");
        put("&o", "italic");

    }};

    public Settings() {
        Main.getInstance().saveDefaultConfig();
        config = Main.getInstance().getConfig();
        loadAdVariables();

    }


    //Ad System//
    private static void loadAdVariables() {
        adPriceCache.clear();
        adTimeCache.clear();
        adStringCache.clear();
        adStringListCache.clear();

        adAllowedCache.clear();
        adColorDisplayName.clear();
        for (Map.Entry<String, String> entry : colorsAndDecos.entrySet()) {
            String color = String.format("Ad.Price.Colors.%s.Price", entry.getValue());
            adPriceCache.put(color, config.getDouble(color, 0));
            adAllowedCache.put(entry.getValue(), config.getBoolean(String.format("Ad.Price.Colors.%s.Enabled", entry.getValue()), false));
            adColorDisplayName.put(entry.getValue(), config.getString(String.format("Ad.Price.Colors.%s.DisplayName", entry.getValue()), entry.getValue()));
        }
    }

    public static String getAdMessages(String text) {
        String base = String.format("Ad.Messages.%s", text);
        String cache = adStringCache.get(base);
        if (cache != null) {
            return cache;
        }
        String configResult = config.getString(base, "null");

        adStringCache.put(base, configResult);
        return configResult;
    }
    public static List<String> getAdStringList(String text) {
        String base = String.format("Ad.Messages.%s", text);
        List<String> cache = adStringListCache.get(base);
        if (cache != null) {
            return cache;
        }
        List<String> configResult = config.getStringList(base);

        adStringListCache.put(base, configResult);
        return configResult;
    }

    public static double getAdPrice(String text, boolean isColor) {
        String base = isColor ? "Ad.Price.Colors.%s.Price" : "Ad.Price.%s";
        base = String.format(base, text);
        Double cache = adPriceCache.get(base);
        if (cache != null) {
            return cache;
        }
        double configResult = config.getDouble(base);

        adPriceCache.put(base, configResult);
        return configResult;
    }

    public static int getAdTime(String text) {
        String base = String.format("Ad.Time.%s", text);
        Integer cache = adTimeCache.get(base);
        if (cache != null) {
            return cache;
        }
        int configResult = config.getInt(base);

        adTimeCache.put(base, configResult);
        return configResult;
    }

    public static boolean getAdBools(String text) {
        String base = String.format("Ad.%s", text);
        Boolean cache = adAllowedCache.get(base);
        if (cache != null) {
            return cache;
        }
        boolean configResult = config.getBoolean(base);

        adAllowedCache.put(base, configResult);
        return configResult;
    }

    public static Map<String, Boolean> getAllowedColors() {
        return adAllowedCache;
    }

    public static String getColorDisplayName(String color, boolean convert) {
        String tempColor = color;
        if (convert) {
            tempColor = colorsAndDecos.get(color);
        }

        return adColorDisplayName.getOrDefault(tempColor, color);
    }

    public static double getColorPrice(String color, boolean convert) {
        String tempColor = color;
        if (convert) {
            tempColor = colorsAndDecos.get(color);
        }

        return adPriceCache.getOrDefault(String.format("Ad.Price.Colors.%s.Price", tempColor), 0d);
    }

    //Ad System End//

    public static void reloadSettings() {
        Main.getInstance().reloadConfig();
        config = Main.getInstance().getConfig();

        loadAdVariables();
    }

}