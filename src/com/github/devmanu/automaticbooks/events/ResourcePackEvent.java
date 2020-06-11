package com.github.devmanu.automaticbooks.events;

import com.github.devmanu.automaticbooks.AutomaticBooks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.ArrayList;

public class ResourcePackEvent implements Listener {

    private AutomaticBooks automaticBooks;
    private ArrayList<String> shown = new ArrayList<String>();

    public ResourcePackEvent(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }



    @EventHandler
    public void onLoad(PlayerResourcePackStatusEvent event) {

        String name = event.getPlayer().getName();
        boolean alreadyShown = shown.contains(name);
        boolean enabled = automaticBooks.getConfig().getInt("delay") == -1;
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        boolean stopLoading = status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED ||
                status == PlayerResourcePackStatusEvent.Status.DECLINED ||
                status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD;

        if (!alreadyShown && enabled && stopLoading) {
            automaticBooks.openJoinBook(event.getPlayer());
            shown.add(name);
        }

    }



    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        shown.remove(event.getPlayer().getName());
    }



}
