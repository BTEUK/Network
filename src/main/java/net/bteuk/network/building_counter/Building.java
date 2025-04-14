package net.bteuk.network.building_counter;

import net.bteuk.network.Network;
import org.bukkit.Location;

public record Building(Integer buildingId, Location coordinate, String playerId, Integer coordinateId) {
}
