package me.abhinav.metaganga;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class Region {
    private final Vector minV, maxV;

    public Region(Location min, Location max) {

        double xPos1 = Math.min(min.getX(), max.getX());
        double yPos1 = Math.min(min.getY(), max.getY());
        double zPos1 = Math.min(min.getZ(), max.getZ());
        double xPos2 = Math.max(min.getX(), max.getX());
        double yPos2 = Math.max(min.getY(), max.getY());
        double zPos2 = Math.max(min.getZ(), max.getZ());

        minV = new Vector(xPos1, yPos1, zPos1);
        maxV = new Vector(xPos2, yPos2, zPos2);
    }

    public boolean containsLocation(Location location) {
        return location.toVector().isInAABB(minV, maxV);
    }

    public Location randomLocation(World world) {
        int x = ThreadLocalRandom.current().nextInt(minV.getBlockX(), maxV.getBlockX() + 1);
        int y = ThreadLocalRandom.current().nextInt(minV.getBlockY(), maxV.getBlockY() + 1);
        int z = ThreadLocalRandom.current().nextInt(minV.getBlockZ(), maxV.getBlockZ() + 1);
        Location loc = new Location(world, x, y, z);
        return loc;
    }
}
