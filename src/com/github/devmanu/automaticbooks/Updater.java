package com.github.devmanu.automaticbooks;

import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Updater {


    private AutomaticBooks automaticBooks;
    private String foundVersion;
    private List<String> warning = new ArrayList<>();
    private String updateText;
    private String updateLink;
    private String hoverText;
    private boolean firstSearch = true;
    private BukkitTask task;

    public Updater(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }


    public void searchForUpdates() {
        if (task != null)
            return;
        task = new BukkitRunnable() {
            @Override
            public void run() {


                try {
                    URL url = new URL("https://raw.githubusercontent.com/DevManu/AutomaticBooks/master/version.json");
                    URLConnection conn = url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));


                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject) parser.parse(reader);

                    String ver = (String) obj.get("version");

                    if (firstSearch) {
                        if (ver.equals(automaticBooks.getVersion()))
                            automaticBooks.consoleMessage("The plugin is up to date.");
                        else
                            automaticBooks.consoleMessage("§eThe plugin is not updated.");
                        firstSearch = false;
                    }


                    if (ver.equals(automaticBooks.getVersion()) || ver.equals(foundVersion))
                        return;

                    warning.clear();
                    foundVersion = ChatColor.translateAlternateColorCodes('&', ver);
                    updateText = ChatColor.translateAlternateColorCodes('&', (String) obj.get("update-text"));
                    updateLink = ChatColor.translateAlternateColorCodes('&', (String) obj.get("update-link"));
                    hoverText = ChatColor.translateAlternateColorCodes('&', (String) obj.get("hover-text"));


                    Bukkit.getOnlinePlayers().forEach(p -> sendWarning(p));


                } catch (IOException | ParseException e) {
                    automaticBooks.consoleMessage("§cError during update check.");
                }


            }
        }.runTaskTimerAsynchronously(automaticBooks, 0, 20 * 60 * 60 * 2);
    }


    public void stopSearching() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }


    public void sendWarning(Player player) {
        if (warning.contains(player.getName()) || foundVersion == null || updateLink == null || updateText == null || !player.hasPermission("AutomaticBooks.admin"))
            return;

        BaseComponent[] text = TextComponent.fromLegacyText(updateText);
        TextComponent t = new TextComponent(text);
        t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        t.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateLink));
        player.sendMessage("");
        player.spigot().sendMessage(t);
        warning.add(player.getName());
    }


}
