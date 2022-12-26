package com.github.devmanu.automaticbooks.book_openers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.github.devmanu.automaticbooks.AutomaticBooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BookOpener_1_8 extends BookOpener {


    private AutomaticBooks automaticBooks;


    public BookOpener_1_8(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }


    @Override
    public void openBook(Player player, List<String> pages) {

        if (automaticBooks.protocolLibError())
            return;

        int slot = player.getInventory().getHeldItemSlot();
        final ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, automaticBooks.getBook(player, pages, automaticBooks.isUsingPlaceholderAPI()));
        try {
            PacketContainer pc = ProtocolLibrary.getProtocolManager()
                    .createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
            pc.getModifier().writeDefaults();
            ByteBuf bf = Unpooled.buffer(256);
            bf.setByte(0, 0);
            bf.writerIndex(1);
            pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
            pc.getStrings().write(0, "MC|BOpen");
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, pc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.getInventory().setItem(slot, old);

    }

    @Override
    public ItemStack getEmptyBook() {
        return new ItemStack(Material.matchMaterial("BOOK_AND_QUILL"), 1, (byte) 0);
    }
}