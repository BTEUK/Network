package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;

import java.util.HashMap;

public class FilterMenu extends Gui {

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    public FilterMenu(NetworkUser user) {
        super(invSize, invName);

        this.plotSQL = Network.getInstance().getPlotSQL();
        this.globalSQL = Network.getInstance().getGlobalSQL();
    }

    private void createGui() {

        // Get a list of all users that have completed plots.
        HashMap<String, Integer> map = plotSQL.getStringIntMap("SELECT uuid,COUNT(id) FROM accept_data GROUP BY uuid ORDER BY COUNT(id) DESC;");

    }

    @Override
    public void refresh() {

    }
}
