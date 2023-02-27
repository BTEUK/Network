package me.bteuk.network.utils;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.OfflinePlayer;

public class TabPlayer {

    public WrappedChatComponent displayName;
    public OfflinePlayer player;

    public TabPlayer(WrappedChatComponent displayName, OfflinePlayer player) {

        this.displayName = displayName;
        this.player = player;

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TabPlayer p) {
            return (p.player.getUniqueId().toString().equals(player.getUniqueId().toString()) && p.displayName.getJson().equals(displayName.getJson()));
        } else {
            return false;
        }
    }
}
