package net.bteuk.network.sql;

import net.bteuk.network.Network;
import net.bteuk.network.utils.Constants;
import net.bteuk.network.utils.regions.Inactivity;
import net.bteuk.network.utils.regions.Region;
import net.bteuk.network.utils.regions.RegionMember;
import net.bteuk.network.utils.regions.Request;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegionSQL extends AbstractSQL {
    public RegionSQL(BasicDataSource datasource) {
        super(datasource);
    }

    public ArrayList<Request> getRequestList(String sql) {

        ArrayList<Request> list = new ArrayList<>();

        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()
        ) {

            while (results.next()) {

                list.add(new Request(results.getString(1), results.getString(2)));

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }

    public ArrayList<Inactivity> getInactives(String sql) {

        ArrayList<Inactivity> list = new ArrayList<>();
        Region region;

        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()
        ) {

            while (results.next()) {

                region = Network.getInstance().getRegionManager().getRegion(results.getString(1));

                list.add(new Inactivity(region, results.getString(2)));

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }

    public List<RegionMember> getRegionMembers(String uuid) {

        List<RegionMember> list = new ArrayList<>();

        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement("SELECT region,uuid,is_owner,last_enter,tag,coordinate_id,pinned FROM region_members WHERE uuid=? ORDER BY pinned DESC, is_owner DESC, region ASC");
        ) {

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                list.add(new RegionMember(
                        results.getString(1),
                        results.getString(2),
                        results.getBoolean(3),
                        results.getLong(4),
                        results.getString(5),
                        results.getInt(6),
                        results.getBoolean(7)
                ));
            }
        } catch (SQLException e) {
            Constants.LOGGER.severe(String.format("An error occurred while fetching the region_members entries for %s", uuid));
        }
        return list;
    }
}
