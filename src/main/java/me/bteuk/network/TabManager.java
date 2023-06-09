package me.bteuk.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.bteuk.network.utils.PlayerDisplayName;
import me.bteuk.network.utils.Roles;
import me.bteuk.network.utils.Utils;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TabManager {

    private final Network instance;

    private final ProtocolManager pm;
    private PacketListener pl;

    private final PlayerDisplayName playerDisplayName;

    //private List<TextComponent> headers = new ArrayList<>();
    //private List<TextComponent> footers = new ArrayList<>();

    private final HashMap<String, PlayerInfoData> fakePlayers;

    public TabManager(Network instance) {

        this.instance = instance;
        pm = ProtocolLibrary.getProtocolManager();

        playerDisplayName = new PlayerDisplayName();

        fakePlayers = new HashMap<>();

        startTab();
        instance.getLogger().info("Enabled Tab");

    }

    private void startTab() {

        createPacketListener();
        pm.addPacketListener(pl);

    }

    public void addFakePlayer(String uuid) {

        //Check if the player is not connected to this server, and is not already contained in the list.
        if (!instance.hasPlayer(uuid) && !fakePlayers.containsKey(uuid)) {

            //Get name from database
            String name = instance.globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");

            WrappedGameProfile profile = new WrappedGameProfile(UUID.fromString(uuid), name);

            PlayerInfoData playerInfoData = new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.CREATIVE, null);

            //instance.getLogger().info("Added " + name + " to fake players list.");
            fakePlayers.put(uuid, playerInfoData);

            //Also add them to tab.
            PacketContainer packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
            packetIn.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
            packetIn.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
            pm.broadcastServerPacket(packetIn);

        }

    }

    public void removeFakePlayer(String uuid) {

        //Remove the uuid from the map if exists.
        PlayerInfoData playerInfoData = fakePlayers.remove(uuid);

        //If the uuid exists remove them from tab.
        if (playerInfoData != null) {

            //instance.getLogger().info("Removed " + playerInfoData.getProfile().getName() + " from fake players list.");

            PacketContainer packetOut = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
            packetOut.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
            packetOut.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
            pm.broadcastServerPacket(packetOut);

        }

    }

    //Handles the different types of tab updates sent from other servers.
    public void updateAll(String message) {

        String[] info = message.split(" ");

        switch (info[0]) {

            case "add" -> //Add the fake player to tab, if the player is on this server they won't be added.
                    addFakePlayer(info[1]);

            case "remove" -> //Remove the fake player from tab, if the player isn't in the fake player list it won't do anything.
                    removeFakePlayer(info[1]);

            case "update" -> //Update the player info in tab, this can be for fake or real players. Usually this would imply a change in prefix.
                    updatePlayer(info[1]);

        }

    }

    public void updatePlayer(String uuid) {

        //First remove the player from tab and then readd them again.
        //This allows the packet to be intercepted and the new displayname to be added.

        //Get name from database
        String name = instance.globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");

        WrappedGameProfile profile = new WrappedGameProfile(UUID.fromString(uuid), name);

        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.CREATIVE, null);

        PacketContainer packetOut = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packetOut.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        packetOut.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        pm.broadcastServerPacket(packetOut);

        PacketContainer packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packetIn.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        packetIn.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        pm.broadcastServerPacket(packetIn);

    }

    //Add all players from other servers.
    public void loadTab(Player p) {

        //Add all players from the fake player list.
        for (PlayerInfoData playerInfoData : fakePlayers.values()) {

            PacketContainer packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
            packetIn.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
            packetIn.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
            try {
                pm.sendServerPacket(p, packetIn);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        instance.getLogger().info("Loaded tab for " + p.getName());

    }

    public void closeTab() {
        pm.removePacketListener(pl);
        instance.getLogger().info("Disabled Tab");
    }

    private void createPacketListener() {
        pl = new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {

            @Override
            public void onPacketSending(PacketEvent event) {

                //If packet is for adding a player, intercept it and add the custom display name.
                if (event.getPacket().getPlayerInfoAction().getValues().get(0).equals(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) {

                    PacketContainer packet = event.getPacket();

                    List<PlayerInfoData> infoList = event.getPacket().getPlayerInfoDataLists().getValues().get(0);

                    PlayerInfoData info = infoList.get(0);

                    //If player is not online, skip.
                    if (!instance.globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + info.getProfile().getUUID() + "';")) {
                        event.setCancelled(true);
                        return;
                    }

                    String displayName = instance.globalSQL.getString("SELECT display_name FROM online_users WHERE uuid='" + info.getProfile().getUUID() + "';");

                    //Get the name of the team which the player needs adding to, this is to sort tab.
                    char teamName = Roles.tabSorting(instance.globalSQL.getString("SELECT primary_role FROM online_users WHERE uuid='" + info.getProfile().getUUID() + "';"));

                    //Add player to the correct team.
                    playerDisplayName.addEntry(info.getProfile().getName(), String.valueOf(teamName));

                    infoList.clear();

                    infoList.add(new PlayerInfoData(info.getProfile(), 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromJson(Utils.tabName(displayName))));

                    packet.getPlayerInfoDataLists().write(0, infoList);

                    //instance.getLogger().info("Intercepting ADD_PLAYER packet, setting display name for " + displayName);

                    event.setPacket(packet);

                }
            }
        };
    }
}
