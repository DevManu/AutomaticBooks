package com.github.devmanu.automaticbooks;

import com.github.devmanu.automaticbooks.book_openers.*;
import com.github.devmanu.automaticbooks.commands.BookCommand;
import com.github.devmanu.automaticbooks.events.JoinEvent;
import com.github.devmanu.automaticbooks.events.ResourcePackEvent;
import stats.Metrics;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AutomaticBooks extends JavaPlugin {


    private BookOpener bookOpener;
    private boolean usingPlaceholderAPI;
    boolean requiresProtocolLib = true;
    private File joinBook;
    private JSONObject joinData;
    private YamlConfiguration config;
    private File configFile = new File(getDataFolder() + File.separator + "config.yml");
    private static final String VERSION = "3.0";
    private Updater updater;


    @Override
    public void onEnable() {

        consoleMessage("§aPlugin enabled.");
        AutomaticBooksAPI.instance = this;
        String ver = Bukkit.getVersion();
        String[] version = ver.replace(")", "").split("\\.");
        int v1 = Integer.valueOf(version[1]);
        int v2 = 0;
        if (version.length >=3)
            v2 = Integer.valueOf(version[2]);

        consoleMessage("" + v2);
        if (v1 >= 8 && v1 <= 12)
            bookOpener = new BookOpener_1_8(this);
        else if (v1 == 13)
            bookOpener = new BookOpener_1_13(this);
        else if (v1 == 14 && (v2 == 0 || v2 == 1))
            bookOpener = new BookOpener_1_14(this);
        else {
            bookOpener = new BookOpener_1_14_2(this);
            requiresProtocolLib = false;
        }

        Bukkit.getPluginManager().registerEvents(new JoinEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ResourcePackEvent(this), this);
        getCommand("book").setExecutor(new BookCommand(this));

        Metrics metrics = new Metrics(this, 5715);


        updater = new Updater(this);

        reloadPlugin();


        if (config.getBoolean("searchForUpdates"))
            updater.searchForUpdates();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (protocolLibError()) {
                    consoleMessage("-------------------------------------");
                    consoleMessage("§c§lPlease install ProtocolLib!");
                    consoleMessage("-------------------------------------");
                }
            }
        }.runTaskLaterAsynchronously(this, 20 * 10);


        //Config fix

        if (getConfig().get("resetViewsOnBookChange") == null) {
            getConfig().set("resetViewsOnBookChange", true);
            saveConfig();
        }

        if (getConfig().get("messages.openUsage") == null) {
            getConfig().set("messages.openUsage", "&aOpen the book with /book open");
            saveConfig();
        }

    }


    @Override
    public void onDisable() {

        consoleMessage("§cPlugin disabled.");


    }


    public void consoleMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage("§b[AutomaticBooks] §r" + msg);
    }


    public ItemStack getEmptyBook() {
        return bookOpener.getEmptyBook();
    }

    public ItemStack getBook(Player player, List<String> pages, boolean placeholderAPI) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null || pages == null)
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
        meta.setTitle("§eAutomaticBooks");
        meta.setDisplayName("§eAutomaticBooks");
        meta.setAuthor(player.getName());
        book.setItemMeta(meta);
        return book;
    }

    public boolean protocolLibError() {
        return requiresProtocolLib && !Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
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
                        array.add("&3Welcome to &6&lAutomaticBooks&3.\n\n&2&nCreate your awesome books!\n\n&9Use &9&l/book&9 to start configuring this plugin.\n\n&8Please leave a review!\n\n\n&8&oMade with &c\u2764 &8&oby _Ma_nu_");
                        obj.put("pages", array);
                        writer.write(obj.toJSONString());
                        writer.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(joinBook), StandardCharsets.UTF_8));
                    JSONParser parser = new JSONParser();
                    joinData = (JSONObject) parser.parse(reader);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
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

        if (array == null)
            return null;

        List<String> pages = new ArrayList<String>();

        for (Object o : array) pages.add(o.toString());


        return pages;
    }


    public void sendMessage(Player player, String message) {
        String msg = config.getString("messages." + message);
        if (msg == null) {
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public String getVersion() {
        return VERSION;
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


    public void openJoinBook(Player player) {


        if (config.getBoolean("usePermission") && !player.hasPermission("AutomaticBooks.join.read"))
            return;

        int times = getConfig().getInt("times");

        if (times == 0)
            return;


        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    JSONObject obj = getJoinData();
                    JSONArray array = (JSONArray) obj.get("players");
                    if (array == null) {
                        array = new JSONArray();
                        obj.put("players", array);
                    }
                    JSONObject pl = null;
                    for (int i = 0; i < array.size(); i++) {
                        if (((JSONObject) array.get(i)).get("name").equals(player.getName())) {
                            pl = (JSONObject) array.get(i);
                            break;
                        }
                    }


                    if (pl == null) {
                        pl = new JSONObject();
                        pl.put("name", player.getName());
                        pl.put("views", 1);
                        array.add(pl);
                    } else {
                        int views = (Integer.parseInt(pl.get("views").toString()));
                        if (times >= 0 && views >= times) {
                            return;
                        }

                        pl.put("views", views + 1);
                    }


                    Writer writer = new OutputStreamWriter(new FileOutputStream(getJoinBook()), StandardCharsets.UTF_8);
                    writer.write(obj.toString());
                    writer.flush();
                    writer.close();

                    getBookOpener().openBook(player, getJoinBookPages());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLaterAsynchronously(this, getConfig().getInt("delay") * 20);


        if (player.hasPermission("AutomaticBooks.admin")) {
            Updater updater = getUpdater();
            updater.sendWarning(player);
        }


    }

}
