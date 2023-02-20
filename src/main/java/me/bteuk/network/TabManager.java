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
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TabManager {

    private Network instance;

    private ProtocolManager pm;
    private PacketListener pl;

    private int taskID;

    private List<TextComponent> headers = new ArrayList<>();
    private List<TextComponent> footers = new ArrayList<>();

    private List<PlayerInfoData> pd;
    private List<PlayerInfoData> pdOld;

    //Create packet.
    PacketContainer packetIn;
    PacketContainer packetOut;

    public TabManager(Network instance) {

        this.instance = instance;
        pm = ProtocolLibrary.getProtocolManager();
        pd = new ArrayList<>();
        pdOld = new ArrayList<>();

        packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packetOut = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);

        packetIn.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        packetOut.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        startTab();
        instance.getLogger().info("Enabled Tab");

    }

    public void startTab() {

        //createPacketListener();

        //pm.addPacketListener(pl);

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            pdOld.addAll(pd);
            pd.clear();

            //Create list from online players.
            for (String uuid : instance.globalSQL.getStringList("SELECT uuid FROM online_users;")) {

                String name = instance.globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
                WrappedGameProfile profile = new WrappedGameProfile(UUID.fromString(uuid),
                        name);

                //Get OfflinePlayer for role placeholder.
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                String placeholder = PlaceholderAPI.setPlaceholders(offlinePlayer, "%luckperms_prefix%");

                WrappedChatComponent displayName = WrappedChatComponent.fromJson(Utils.tabName(placeholder, name));

                pd.add(new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.CREATIVE, displayName));

            }

            //If the list has different players, remove the current list first.
            if (pd.size() != pdOld.size()) {
                packetOut.getPlayerInfoDataLists().write(0, pdOld);
                pm.broadcastServerPacket(packetOut);
            } else {
                for (PlayerInfoData pi : pd) {
                    if (!pdOld.contains(pi)) {
                        packetOut.getPlayerInfoDataLists().write(0, pdOld);
                        pm.broadcastServerPacket(packetOut);
                        break;
                    }
                }
            }

            pdOld.clear();

            //Send new tablist packet.
            if (!pd.isEmpty()) {
                packetIn.getPlayerInfoDataLists().write(0, pd);
                pm.broadcastServerPacket(packetIn);
            }

        },0L, 40L);

    }

    public void closeTab() {
        //pm.removePacketListener(pl);
        Bukkit.getScheduler().cancelTask(taskID);
        instance.getLogger().info("Disabled Tab");
    }

    /*
    private void createPacketListener() {
        pl = new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {

            @Override
            public void onPacketSending(PacketEvent event) {

                //Intercept and cancel Add and Remove player packets.


                //Cancel the packet and send custom data instead.
                PacketContainer packet = event.getPacket();

                if (packet.getPlayerInfoAction().read(0).equals(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) {

                    instance.getLogger().info("Intercepting Packet.");

                    //Create custom packet.
                    List<PlayerInfoData> pd = new ArrayList<>();

                    //Create list from online players.
                    for (String uuid : instance.globalSQL.getStringList("SELECT uuid FROM online_users;")) {

                        String name = instance.globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
                        WrappedGameProfile profile = new WrappedGameProfile(UUID.fromString(uuid),
                                name);

                        //Get OfflinePlayer for role placeholder.
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                        String placeholder = PlaceholderAPI.setPlaceholders(offlinePlayer, "%luckperms_prefix%");

                        WrappedChatComponent displayName = WrappedChatComponent.fromJson(Utils.tabName(placeholder, name));

                        pd.add(new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.CREATIVE, displayName));

                    }

                    //Update the packet.
                    packet.getPlayerInfoDataLists().write(0, pd);
                    event.setPacket(packet);

                }
            }
        };
    }
     */
}
