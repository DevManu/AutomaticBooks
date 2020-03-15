package com.github.devmanu.automaticbooks.commands;

import com.github.devmanu.automaticbooks.AutomaticBooks;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.List;


public class BookCommand implements CommandExecutor {


    private AutomaticBooks automaticBooks;


    public BookCommand(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


        Player player = (Player) sender;

        if (!sender.hasPermission("automaticbooks.admin")) {
            TextComponent t1 = new TextComponent("§8[§6AutomaticBooks§8] §eThis server uses §6AutomaticBooks " + automaticBooks.getVersion() + "§e!");
            TextComponent download = new TextComponent("§eDownload: ");
            TextComponent click = new TextComponent("§6click here");
            TextComponent author = new TextComponent("§e. Author: §6_Ma_nu_§e.");
            click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/automaticbooks-book-on-join-announces-in-game-customizable.66639/"));
            click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§eDownload AutomaticBooks").create()));
            player.spigot().sendMessage(t1);
            player.spigot().sendMessage(download, click, author);
            return true;
        }


        Configuration config = automaticBooks.getConfig();
        List<String> pages = getBookInHand(player);


        if (args.length == 0) {
            sendGuide(player);
            return true;
        }

        String arg = args[0];

        if (arg.equalsIgnoreCase("reload")) {

            automaticBooks.reloadPlugin();
            automaticBooks.sendMessage(player, "reload");

        } else if (arg.equalsIgnoreCase("craft")) {

            if (pages != null) {
                ItemStack book = automaticBooks.getBook(player, pages, automaticBooks.isUsingPlaceholderAPI());
                BookMeta meta = (BookMeta) book.getItemMeta();
                if (args.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 1; i < args.length; i++)
                        name.append(args[i]).append(" ");
                    String s = ChatColor.translateAlternateColorCodes('&', name.toString());
                    meta.setTitle(s);
                    meta.setAuthor(s);
                } else {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("defaultCraftedBookName")));
                }
                book.setItemMeta(meta);
                player.getInventory().addItem(book);
                automaticBooks.sendMessage(player, "craftedBook");
            } else {
                automaticBooks.sendMessage(player, "notBook");
            }


        } else if (arg.equalsIgnoreCase("preview")) {

            if (pages != null) {
                automaticBooks.getBookOpener().openBook(player, pages);
                automaticBooks.sendMessage(player, "preview");
            } else {
                automaticBooks.sendMessage(player, "notBook");
            }
        } else if (arg.equalsIgnoreCase("open")) {
            automaticBooks.getBookOpener().openBook(player, automaticBooks.getJoinBookPages());
            automaticBooks.sendMessage(player, "open");
        } else if (arg.equalsIgnoreCase("save")) {
            if (pages != null) {

                File join = automaticBooks.getJoinBook();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter(join));
                            JSONObject obj = automaticBooks.getJoinData();

                            obj.remove("pages");
                            JSONArray array = new JSONArray();
                            array.addAll(pages);
                            obj.put("pages", array);
                            obj.remove("players");

                            writer.write(obj.toString());
                            writer.flush();

                            automaticBooks.sendMessage(player, "changedBook");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(automaticBooks);
            } else {
                automaticBooks.sendMessage(player, "notBook");
            }


        } else if (arg.equalsIgnoreCase("announce")) {
            boolean perm = config.getBoolean("usePermissions");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (perm && !p.hasPermission("AutomaticBooks.announce.read"))
                    continue;
                automaticBooks.getBookOpener().openBook(p, pages);
            }
            automaticBooks.sendMessage(player, "announceSent");


        } else if (arg.equalsIgnoreCase("give")) {
            player.getInventory().addItem(automaticBooks.getEmptyBook());
            automaticBooks.sendMessage(player, "givedBook");
        } else if (arg.equalsIgnoreCase("times")) {

            String n = args[1];
            if (n.startsWith("-"))
                n = n.substring(1);
            if (arg.length() == 1) {
                automaticBooks.sendMessage(player, "timesUsage");
            } else if (!StringUtils.isNumeric(n)) {
                automaticBooks.sendMessage(player, "notNumeric");
            } else {
                automaticBooks.getConfig().set("times", Integer.valueOf(args[1]));
                automaticBooks.saveConfig();
                automaticBooks.sendMessage(player, "timesChanged");
            }

        } else if (arg.equalsIgnoreCase("delete")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        BufferedWriter writer = new BufferedWriter(new PrintWriter(automaticBooks.getJoinBook()));
                        JSONObject obj = automaticBooks.getJoinData();
                        obj.remove("pages");
                        obj.remove("players");
                        writer.write(obj.toJSONString());
                        writer.flush();
                        automaticBooks.sendMessage(player, "deletedBook");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(automaticBooks);
        } else {
            sendGuide(player);
        }


        return true;
    }


    private List<String> getBookInHand(Player player) {
        ItemStack book = player.getItemInHand();
        Material type = book.getType();
        if (type == automaticBooks.getEmptyBook().getType() || type == Material.WRITTEN_BOOK)
            return ((BookMeta) book.getItemMeta()).getPages();
        else
            return null;
    }


    private void sendGuide(Player player) {

        List<String> rows = automaticBooks.getConfig().getStringList("messages.guide");

        for (String row : rows)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', row));

    }


}
