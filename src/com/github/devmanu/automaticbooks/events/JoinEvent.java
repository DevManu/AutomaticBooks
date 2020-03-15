package com.github.devmanu.automaticbooks.events;

import com.github.devmanu.automaticbooks.AutomaticBooks;
import com.github.devmanu.automaticbooks.Updater;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JoinEvent implements Listener {


    private AutomaticBooks automaticBooks;

    public JoinEvent(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {


        Player player = event.getPlayer();


        if (!player.hasPermission("AutomaticBooks.join.read"))
            return;

        int times = automaticBooks.getConfig().getInt("times");

        if (times == 0)
            return;


        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    JSONObject obj = automaticBooks.getJoinData();
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

                        long views = ((long) pl.get("views"));
                        if (times < 0 || views >= times) {
                            return;
                        }

                        pl.put("views", views + 1);
                    }


                    Writer writer = new OutputStreamWriter(new FileOutputStream(automaticBooks.getJoinBook()), StandardCharsets.UTF_8);
                    writer.write(obj.toString());
                    writer.flush();
                    writer.close();

                    automaticBooks.getBookOpener().openBook(player, automaticBooks.getJoinBookPages());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLaterAsynchronously(automaticBooks, automaticBooks.getConfig().getInt("delay") * 20);


        if (player.hasPermission("AutomaticBooks.admin")) {
            Updater updater = automaticBooks.getUpdater();
            updater.sendWarning(player);
        }


    }


}
