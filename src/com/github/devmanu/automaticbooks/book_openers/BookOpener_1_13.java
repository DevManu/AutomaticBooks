package com.github.devmanu.automaticbooks.book_openers;

import com.github.devmanu.automaticbooks.AutomaticBooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.PacketDataSerializer;
import net.minecraft.server.v1_14_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BookOpener_1_13 implements BookOpener {


    private AutomaticBooks automaticBooks;

    public BookOpener_1_13(AutomaticBooks automaticBooks) {
        this.automaticBooks = automaticBooks;
    }

    @Override
    public void openBook(Player player, List<String> pages) {
        int slot = player.getInventory().getHeldItemSlot();
        final ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, automaticBooks.getBook(player, pages, automaticBooks.isUsingPlaceholderAPI()));
        ByteBuf bf = Unpooled.buffer(256);
        bf.setByte(0, 0);
        bf.writerIndex(1);
        String ver = Bukkit.getVersion();


        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(MinecraftKey.a("open_book"), new PacketDataSerializer(bf));
        connection.sendPacket(packet);
        player.getInventory().setItem(slot, old);
    }
}
