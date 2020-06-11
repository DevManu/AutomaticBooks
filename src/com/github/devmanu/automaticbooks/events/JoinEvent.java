package com.github.devmanu.automaticbooks.events;

import com.github.devmanu.automaticbooks.AutomaticBooks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {


    private AutomaticBooks automaticBooks;

    public JoinEvent(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (automaticBooks.getConfig().getInt("delay") >= 0) {
            automaticBooks.openJoinBook(event.getPlayer());
        }

    }


}
