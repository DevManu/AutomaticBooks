package com.github.devmanu.automaticbooks;

import com.github.devmanu.automaticbooks.book_openers.BookOpener;
import com.github.devmanu.automaticbooks.book_openers.BookOpener_1_13;
import com.github.devmanu.automaticbooks.book_openers.BookOpener_1_8;
import com.github.devmanu.automaticbooks.commands.BookCommand;
import com.github.devmanu.automaticbooks.events.JoinEvent;
import com.google.common.base.Charsets;
import jdk.internal.util.xml.impl.ReaderUTF8;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import stats.Metrics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AutomaticBooks extends JavaPlugin {


    private BookOpener bookOpener;
    private boolean legacy = true;
    private boolean usingPlaceholderAPI;
    private File joinBook;
    private JSONObject joinData;
    private YamlConfiguration config;
    private File configFile = new File(getDataFolder() + File.separator + "config.yml");
    private ArrayList<String> pages = new ArrayList<>();
    private final String version = "2.0";
    private Updater updater;

    public boolean isLegacy() {
        return legacy;
    }

    @Override
    public void onEnable() {

        consoleMessage("§aPlugin enabled.");
        String version = Bukkit.getVersion();
        String[] specialVersions = {"1.13", "1.14", "1.15"};

        AutomaticBooksAPI.instance = this;

        for (String specialVersion : specialVersions)
            if (version.contains(specialVersion))
                legacy = false;

        if (!legacy)
            bookOpener = new BookOpener_1_13(this);
        else
            bookOpener = new BookOpener_1_8(this);


        Bukkit.getPluginManager().registerEvents(new JoinEvent(this), this);
        getCommand("book").setExecutor(new BookCommand(this));

        Metrics metrics = new Metrics(this);


        updater = new Updater(this);

        reloadPlugin();


        if (config.getBoolean("searchForUpdates"))
            updater.searchForUpdates();


    }


    @Override
    public void onDisable() {

        consoleMessage("§cPlugin disabled.");


    }


    public void consoleMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage("§b[AutomaticBooks] §r" + msg);
    }


    public ItemStack getEmptyBook() {

        if (!isLegacy())
            return new ItemStack(Material.matchMaterial("WRITABLE_BOOK"));
        else
            return new ItemStack(Material.matchMaterial("BOOK_AND_QUILL"), 1, (byte) 0);
    }

    public ItemStack getBook(Player player, List<String> pages, boolean placeholderAPI) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null)
            return null;

        List<String> colored = new ArrayList<String>();
        for (String s : pages) {


            //Official Placeholders
            s = s.replace("{Player}", player.getName());
            s = s.replace("{OnlinePlayers}", Bukkit.getOnlinePlayers().size() + "");

            //Placeholder compatibility
            if (placeholderAPI)
                s = PlaceholderAPI.setPlaceholders(player, s);


            colored.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        meta.setPages(colored);
        meta.setDisplayName("");
        meta.setAuthor(player.getName());
        book.setItemMeta(meta);
        return book;
    }


    public boolean isUsingPlaceholderAPI() {
        return usingPlaceholderAPI;
    }

    public File getJoinBook() {
        return joinBook;
    }

    public BookOpener getBookOpener() {
        return bookOpener;
    }


    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public void reloadPlugin() {


        if (!configFile.exists()) {
            saveDefaultConfig();
        }


        //data check

        File data = new File(getDataFolder() + File.separator + "data");

        if (!data.exists() || !data.isDirectory())
            data.mkdir();


        //data/join.json check
        joinBook = new File(getDataFolder() + File.separator + "data" + File.separator + "join.json");


        new BukkitRunnable() {
            @Override
            public void run() {


                if (!joinBook.exists()) {
                    try {

                        joinBook.createNewFile();
                        Writer writer = new OutputStreamWriter(new FileOutputStream(joinBook), StandardCharsets.UTF_8);

                        JSONObject obj = new JSONObject();
                        JSONArray array = new JSONArray();
                        array.add("&5Welcome to &6&lAutomaticBooks.\n\n&3Create your awesome books!\n\n\n\n&8&oMade with &c\u2764 &8&oby _Ma_nu_");
                        obj.put("pages", array);
                        writer.write(obj.toJSONString());
                        writer.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                try {
                    Reader reader = new InputStreamReader(new FileInputStream(joinBook), StandardCharsets.UTF_8);
                    JSONParser parser = new JSONParser();
                    joinData = (JSONObject) parser.parse(reader);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);

        try {
            ReaderUTF8 reader = new ReaderUTF8(new FileInputStream(configFile));
            config = YamlConfiguration.loadConfiguration(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        usingPlaceholderAPI = config.getBoolean("usePlaceholderAPI");
        if (usingPlaceholderAPI) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                consoleMessage("§3PlaceholderAPI detected and successfully activated");
            else {
                consoleMessage("§cPlaceholderAPI not detected. If you want to hide this messages, set usePlaceholderAPI to false in config.yml");
                usingPlaceholderAPI = false;
            }
        }


        if (config.getBoolean("searchForUpdates"))
            updater.searchForUpdates();
        else
            updater.stopSearching();

    }


    public JSONObject getJoinData() {
        return joinData;
    }

    public List<String> getJoinBookPages() {
        JSONArray array = (JSONArray) joinData.get("pages");


        List<String> pages = new ArrayList<String>();

        for (Object o : array) pages.add(o.toString());


        return pages;
    }


    public void sendMessage(Player player, String message) {
        String msg = config.getString("messages." + message);
        if (msg == null) {
            consoleMessage("§cThis message has been not found: " + message);
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public String getVersion() {
        return version;
    }


    public Updater getUpdater() {
        return updater;
    }




    public void saveConfig() {
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(configFile), StandardCharsets.UTF_8));
            try {
                out.write(config.saveToString());
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
