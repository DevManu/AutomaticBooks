package com.github.devmanu.automaticbooks.book_openers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.github.devmanu.automaticbooks.AutomaticBooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_15_R1.EnumHand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BookOpener_1_14 extends BookOpener {


    private AutomaticBooks automaticBooks;

    public BookOpener_1_14(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }

    @Override
    public void openBook(Player player, List<String> pages) {
        int slot = player.getInventory().getHeldItemSlot();
        final ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, automaticBooks.getBook(player, pages, automaticBooks.isUsingPlaceholderAPI()));
        try {
            PacketContainer pc = ProtocolLibrary.getProtocolManager()
                    .createPacket(PacketType.Play.Server.OPEN_BOOK);


            pc.getModifier().write(0, EnumWrappers.getHandConverter().getGeneric(EnumWrappers.Hand.MAIN_HAND));

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, pc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.getInventory().setItem(slot, old);
    }

    @Override
    public ItemStack getEmptyBook() {
        return new ItemStack(Material.WRITABLE_BOOK);
    }
}
