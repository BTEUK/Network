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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME;
import static com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.ADD_PLAYER;
import static com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.UPDATE_LISTED;
import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class TabManager {

    private final Network instance;

    private final ProtocolManager pm;
    private PacketListener pl;
    private PacketListener player_info_remove;

    private final PlayerDisplayName playerDisplayName;

    private final HashMap<String, PlayerInfoData> fakePlayers;

    private final EnumSet<EnumWrappers.PlayerInfoAction> actions = EnumSet.of(ADD_PLAYER, UPDATE_DISPLAY_NAME, UPDATE_LISTED);

    public TabManager(Network instance) {

        this.instance = instance;
        pm = ProtocolLibrary.getProtocolManager();

        playerDisplayName = new PlayerDisplayName();

        fakePlayers = new HashMap<>();

        startTab();
        instance.getLogger().info("Enabled Tab");

    }

    private void startTab() {

        createPacketListeners();
        pm.addPacketListener(pl);
        pm.addPacketListener(player_info_remove);

    }

    public void addFakePlayer(String sUuid) {

        //Check if the player is not connected to this server, and is not already contained in the list.
        if (!instance.hasPlayer(sUuid) && !fakePlayers.containsKey(sUuid)) {

            //Get name from database
            String name = instance.globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + sUuid + "';");

            UUID uuid = UUID.fromString(sUuid);
            WrappedGameProfile profile = new WrappedGameProfile(uuid, name);

            PlayerInfoData playerInfoData = new PlayerInfoData(uuid, 0, true, EnumWrappers.NativeGameMode.CREATIVE, profile, null);

            //instance.getLogger().info("Added " + name + " to fake players list.");
            fakePlayers.put(sUuid, playerInfoData);

            //Also add them to tab.
            PacketContainer packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);

            packetIn.getPlayerInfoActions().write(0, actions);
            packetIn.getPlayerInfoDataLists().write(1, Collections.singletonList(playerInfoData));
            pm.broadcastServerPacket(packetIn);

        }

    }

    public void removeFakePlayer(String uuid) {

        //Remove the uuid from the map if exists.
        PlayerInfoData playerInfoData = fakePlayers.remove(uuid);

        //If the uuid exists remove them from tab.
        if (playerInfoData != null) {

            //instance.getLogger().info("Removed " + playerInfoData.getProfile().getName() + " from fake players list.");

            PacketContainer packetOut = pm.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
            packetOut.getUUIDLists().write(0, Collections.singletonList(UUID.fromString(uuid)));
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

    public void updatePlayer(String sUuid) {

        UUID uuid = UUID.fromString(sUuid);

        //Get name from database
        String name = instance.globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + sUuid + "';");

        String displayName = instance.globalSQL.getString("SELECT display_name FROM online_users WHERE uuid='" + sUuid + "';");

        //Get the name of the team which the player needs adding to, this is to sort tab.
        char teamName = Roles.tabSorting(instance.globalSQL.getString("SELECT primary_role FROM online_users WHERE uuid='" + sUuid + "';"));

        //Add player to the correct team.
        playerDisplayName.addEntry(name, String.valueOf(teamName));

        WrappedGameProfile profile = new WrappedGameProfile(uuid, name);

        PlayerInfoData playerInfoData = new PlayerInfoData(uuid, 0, true, EnumWrappers.NativeGameMode.CREATIVE, profile, WrappedChatComponent.fromJson(Utils.tabName(displayName)));

        PacketContainer packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);

        packetIn.getPlayerInfoActions().write(0, Collections.singleton(UPDATE_DISPLAY_NAME));
        packetIn.getPlayerInfoDataLists().write(1, Collections.singletonList(playerInfoData));
        pm.broadcastServerPacket(packetIn);

    }

    //Add all players from other servers.
    public void loadTab(Player p) {

        //Add all players from the fake player list.
        List<PlayerInfoData> players = new ArrayList<>();
        fakePlayers.forEach((uuid, info) -> players.add(info));

        PacketContainer packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packetIn.getPlayerInfoActions().write(0, actions);
        packetIn.getPlayerInfoDataLists().write(1, players);
        pm.sendServerPacket(p, packetIn);

        //Add header/footer.
        //This information is static and does not require constant updates.
        p.sendPlayerListHeaderAndFooter(
                Component.text("BTE ", NamedTextColor.AQUA, TextDecoration.BOLD)
                        .append(Component.text("UK", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
                        .append(Component.newline()),
                Component.newline()
                        .append(Utils.line("Server Info: "))
                        .append(Component.text("/help", NamedTextColor.GRAY))
                        .append(Component.newline())
                        .append(Utils.line("More Info: "))
                        .append(Component.text("/discord", NamedTextColor.GRAY))
        );

        instance.getLogger().info("Loaded tab for " + p.getName());

    }

    public void closeTab() {
        pm.removePacketListener(pl);
        pm.removePacketListener(player_info_remove);
        instance.getLogger().info("Disabled Tab");
    }

    private void createPacketListeners() {
        pl = new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {

            @Override
            public void onPacketSending(PacketEvent event) {

                PacketContainer packet = event.getPacket();

                //If packet is for adding a player, intercept it and add the custom display name.
                if (packet.getPlayerInfoActions().read(0).contains(ADD_PLAYER)) {

                    List<PlayerInfoData> infoList = packet.getPlayerInfoDataLists().read(1);
                    List<PlayerInfoData> newInfoList = new ArrayList<>();

                    infoList.forEach(info -> {

                        //If player is not online, skip.
                        if (!instance.globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + info.getProfile().getUUID() + "';")) {
                            return;
                        }

                        String displayName = instance.globalSQL.getString("SELECT display_name FROM online_users WHERE uuid='" + info.getProfile().getUUID() + "';");

                        //Get the name of the team which the player needs adding to, this is to sort tab.
                        char teamName = Roles.tabSorting(instance.globalSQL.getString("SELECT primary_role FROM online_users WHERE uuid='" + info.getProfile().getUUID() + "';"));

                        //Add player to the correct team.
                        playerDisplayName.addEntry(info.getProfile().getName(), String.valueOf(teamName));

                        newInfoList.add(new PlayerInfoData(info.getProfileId(), 0, true, EnumWrappers.NativeGameMode.CREATIVE, info.getProfile(), WrappedChatComponent.fromJson(Utils.tabName(displayName)), info.getRemoteChatSessionData() == null ? null : info.getRemoteChatSessionData()));

                        instance.getLogger().info("Intercepting ADD_PLAYER packet, setting display name for " + displayName);

                    });

                    packet.getPlayerInfoDataLists().write(1, newInfoList);

                    event.setPacket(packet);

                }
            }
        };

        player_info_remove = new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO_REMOVE) {

            @Override
            public void onPacketSending(PacketEvent event) {

                PacketContainer packet = event.getPacket();

                //Get all uuids to be removed, if they are still on the network, but on another server, try adding them as fake player.
                packet.getUUIDLists().read(0).forEach(uuid -> {

                    //If the player is on a different server.
                    if (instance.globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "' AND server<>'" + SERVER_NAME + "';")) {

                        LOGGER.info("Removed player with uuid " + uuid + " from TAB, but they are on another server");
                        LOGGER.info("Adding them back as a fake player.");
                        Bukkit.getScheduler().runTask(instance, () -> addFakePlayer(uuid.toString()));

                    }
                });

            }
        };
    }
}
