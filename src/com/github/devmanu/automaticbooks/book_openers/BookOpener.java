package com.github.devmanu.automaticbooks.book_openers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class BookOpener {
    public abstract void openBook(Player player, List<String> pages);
    public abstract ItemStack getEmptyBook();
}
