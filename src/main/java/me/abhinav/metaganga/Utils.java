package me.abhinav.metaganga;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Utils {
    Main main = Main.getInstance();
    public void placeTrashRandomly(int level, String trash) {
        Region region = main.regions.get((new Random().nextInt(3)));
        Location loc = null;
        World world;
        if(!main.isRunning && !main.isFinished) {
            world = Bukkit.getWorld(main.getConfig().getString("spawnLocation.world"));
        } else {
            world = Bukkit.getWorld(main.getConfig().getString("level" + level + ".world"));
        }
        do {
            loc = region.randomLocation(world);
        } while (!loc.getBlock().getType().equals(Material.WATER));

        //Trash command
        //Bukkit.broadcastMessage("location - " + world.getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "trashpoints:place " + trash + " " + world.getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
    }

    public void removeTrash(int level) {
        World world = world = Bukkit.getWorld(main.getConfig().getString("level" + level + ".world"));;
        for(Entity entity: world.getEntities()) {
            if(entity instanceof Interaction) {
                entity.remove();
            } else if(entity instanceof ItemDisplay) {
                entity.remove();
            }
        }
    }

    public void removeMobs(int level) {
        World world = Bukkit.getWorld(main.getConfig().getString("level" + level + ".world"));;
        for(Entity entity: world.getEntities()) {
            if(entity instanceof Mob) {
                entity.remove();
            }
        }
    }

    public void spawnMobs() {
        World world;
        if(!main.isRunning && !main.isFinished) {
            world = Bukkit.getWorld(main.getConfig().getString("spawnLocation.world"));
        } else {
            world = Bukkit.getWorld(main.getConfig().getString("level" + main.level + ".world"));
        }
        switch(main.level) {
            case 5:
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=50) { //Number of dolphins to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get((new Random().nextInt(4)));
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.WATER));

                        world.spawnEntity(loc, EntityType.DOLPHIN);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);
            case 4:
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=10) { //Number of Turtles to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get(4);
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.WATER));

                        world.spawnEntity(loc, EntityType.TURTLE);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);
            case 3:
                //Tropical Fish
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=200) { //Number of Tropical Fishes to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get((new Random().nextInt(4)));
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.WATER));

                        world.spawnEntity(loc, EntityType.TROPICAL_FISH);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);

                //Salmon
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=40) { //Number of Salmon to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get((new Random().nextInt(4)));
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.WATER));

                        world.spawnEntity(loc, EntityType.SALMON);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);

                //Cod
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=40) { //Number of Cod to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get((new Random().nextInt(4)));
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.WATER));

                        world.spawnEntity(loc, EntityType.COD);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);

                //Parrot 1 -
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=10) { //Number of Parrots to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get(5);
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.AIR));

                        world.spawnEntity(loc, EntityType.PARROT);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);

                //Parrot 2 -
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=10) { //Number of Parrots to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get(6);
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.AIR));

                        world.spawnEntity(loc, EntityType.PARROT);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);

                //Parrot 3 -
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=30) { //Number of Parrots to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get(7);
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.AIR));

                        world.spawnEntity(loc, EntityType.PARROT);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);

                //Parrot 4 -
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=5) { //Number of Parrots to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get(8);
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.AIR));

                        world.spawnEntity(loc, EntityType.PARROT);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);
            case 2:
                new BukkitRunnable(){
                    int i=0;
                    @Override
                    public void run() {
                        if(i>=20) { //Number of Frogs to spawn
                            cancel();
                            return;
                        }
                        Region region = main.regions.get(4);
                        Location loc = null;
                        do {
                            loc = region.randomLocation(world);
                        } while (!loc.getBlock().getType().equals(Material.WATER));

                        Frog frog = (Frog) world.spawnEntity(loc, EntityType.FROG);
                        frog.setVariant(Frog.Variant.COLD);
                        i++;
                    }
                }.runTaskTimer(main, 0, 1L);
        }
    }
}
