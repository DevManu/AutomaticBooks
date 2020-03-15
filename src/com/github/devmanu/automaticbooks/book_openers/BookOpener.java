package com.github.devmanu.automaticbooks.book_openers;

import org.bukkit.entity.Player;

import java.util.List;

public interface BookOpener {
    void openBook(Player player, List<String> pages);
}
