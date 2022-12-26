package com.github.devmanu.automaticbooks.book_openers;

import com.github.devmanu.automaticbooks.AutomaticBooks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BookOpener_1_14_2 extends BookOpener {


    private AutomaticBooks automaticBooks;

    public BookOpener_1_14_2(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }

    @Override
    public void openBook(Player player, List<String> pages) {
        int slot = player.getInventory().getHeldItemSlot();
        player.openBook(automaticBooks.getBook(player, pages, automaticBooks.isUsingPlaceholderAPI()));
    }

    @Override
    public ItemStack getEmptyBook() {
        return new ItemStack(Material.WRITABLE_BOOK);
    }
}
