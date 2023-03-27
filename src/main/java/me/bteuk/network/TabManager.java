package me.bteuk.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TabManager {

    private Network instance;

    private ProtocolManager pm;
    private PacketListener pl;

    //private List<TextComponent> headers = new ArrayList<>();
    //private List<TextComponent> footers = new ArrayList<>();

    private HashMap<String, PlayerInfoData> fakePlayers;

    public TabManager(Network instance) {

        this.instance = instance;
        pm = ProtocolLibrary.getProtocolManager();

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

            instance.getLogger().info("Added " + name + " to fake players list.");
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

            instance.getLogger().info("Removed " + playerInfoData.getProfile().getName() + " from fake players list.");

            PacketContainer packetOut = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
            packetOut.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
            packetOut.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
            pm.broadcastServerPacket(packetOut);

        }

    }

    //Handles the different types of tab updates sent from other servers.
    public void updateAll(String message) {

        instance.getLogger().info(message);

        String[] info = message.split(" ");

        switch (info[0]) {

            case "add" -> {

                //Add the fake player to tab, if the player is on this server they won't be added.
                addFakePlayer(info[1]);

            }

            case "remove" -> {

                //Remove the fake player from tab, if the player isn't in the fake player list it won't do anything.
                removeFakePlayer(info[1]);

            }

            //TODO UPDATE

        }

    }

    public void updateDisplayName() {

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

                    String displayName = instance.globalSQL.getString("SELECT display_name FROM online_users WHERE uuid='" + info.getProfile().getUUID() + "';");

                    infoList.clear();
                    infoList.add(new PlayerInfoData(info.getProfile(), 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromJson(Utils.tabName(displayName))));

                    packet.getPlayerInfoDataLists().write(0, infoList);

                    instance.getLogger().info("Intercepting ADD_PLAYER packet, setting display name for " + displayName);

                    event.setPacket(packet);

                }
            }
        };
    }
}
