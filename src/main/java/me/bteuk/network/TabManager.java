package me.bteuk.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.bteuk.network.utils.TabPlayer;
import me.bteuk.network.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
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

    private boolean changed;
    private List<TabPlayer> players;
    private List<TabPlayer> playersOld;

    //Create packet.
    PacketContainer packetIn;
    PacketContainer packetOut;

    public TabManager(Network instance) {

        this.instance = instance;
        pm = ProtocolLibrary.getProtocolManager();

        pdOld = new ArrayList<>();
        pd = new ArrayList<>();

        players = new ArrayList<>();
        playersOld = new ArrayList<>();

        packetIn = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packetOut = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);

        packetIn.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        packetOut.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        startTab();
        instance.getLogger().info("Enabled Tab");

    }

    private void startTab() {

        //createPacketListener();

        //pm.addPacketListener(pl);

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            changed = false;

            //Create list from online players.
            for (String uuid : instance.globalSQL.getStringList("SELECT uuid FROM online_users;")) {


                //Get OfflinePlayer for role placeholder.
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                WrappedChatComponent displayName = WrappedChatComponent.fromJson(Utils.tabName(instance.globalSQL.getString("SELECT display_name FROM online_users WHERE uuid='" + uuid + "';")));

                TabPlayer tabPlayer = new TabPlayer(displayName, offlinePlayer);
                players.add(tabPlayer);

                //Check if the old list does not contain this player.
                if (!playersOld.contains(tabPlayer)) {

                    changed = true;

                }
            }

            //Check the length of players is different from playersOld, then something also changed.
            if (players.size() != playersOld.size()) {
                changed = true;
            }

            //If the list changed, then recreate tab.
            if (changed) {

                Network.getInstance().getLogger().info("Updating Tab");

                for (TabPlayer tp : players) {

                    WrappedGameProfile profile = new WrappedGameProfile(tp.player.getUniqueId(),
                            tp.player.getName());

                    pd.add(new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.CREATIVE, tp.displayName));

                }

                //Remove current list if it contains something.
                if (!pdOld.isEmpty()) {
                    packetOut.getPlayerInfoDataLists().write(0, pdOld);
                    pm.broadcastServerPacket(packetOut);
                }

                //Add new list.
                if (!pd.isEmpty()) {
                    packetIn.getPlayerInfoDataLists().write(0, pd);
                    pm.broadcastServerPacket(packetIn);
                }

                //Reset the tab players.
                pdOld.clear();
                pdOld.addAll(pd);
                pd.clear();

            }

            //Clear the players list for the next iteration.
            playersOld.clear();
            playersOld.addAll(players);
            players.clear();

        }, 0L, 40L);

    }

    //Updates tab without requiring a change, this will be used when a user switches server, this won't updat the online player list.
    public void updateTab(Player p) {

        //Create list from online players.
        for (String uuid : instance.globalSQL.getStringList("SELECT uuid FROM online_users;")) {


            //Get OfflinePlayer for role placeholder.
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            WrappedChatComponent displayName = WrappedChatComponent.fromJson(Utils.tabName(instance.globalSQL.getString("SELECT display_name FROM online_users WHERE uuid='" + uuid + "';")));

            TabPlayer tabPlayer = new TabPlayer(displayName, offlinePlayer);
            players.add(tabPlayer);

        }

        Network.getInstance().getLogger().info("Updating Tab for " + p.getName());

        for (TabPlayer tp : players) {

            WrappedGameProfile profile = new WrappedGameProfile(tp.player.getUniqueId(),
                    tp.player.getName());

            pd.add(new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.CREATIVE, tp.displayName));

        }

        try {
            if (!pd.isEmpty()) {
                packetIn.getPlayerInfoDataLists().write(0, pd);
                pm.sendServerPacket(p, packetIn);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        //Reset the tab players.
        pdOld.clear();
        pdOld.addAll(pd);
        pd.clear();


        //Clear the players list for the next iteration.
        playersOld.clear();
        playersOld.addAll(players);
        players.clear();

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
