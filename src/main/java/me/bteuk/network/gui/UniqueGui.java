package me.bteuk.network.gui;

import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UniqueGui extends Gui {

    public UniqueGui(int invSize, Component invName) {

        super(invSize, invName);

    }

    public void delete(NetworkUser u){

        //Delete gui.
        this.delete();

        //Remove the uniqueGui from the player.
        u.uniqueGui = null;
    }
}
