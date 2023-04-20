package me.abhinav.metaganga;

import me.abhinav.metaganga.Commands.AddPoints;
import me.abhinav.metaganga.Commands.ForceStop;
import me.abhinav.metaganga.Commands.Reload;
import me.abhinav.metaganga.Commands.SetMinPlayers;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public final class Main extends JavaPlugin {
    private static Main main;
    public MainListener mainListener;
    public boolean isRunning;
    public boolean isFinished;
    public boolean isStarting;
    public boolean isLevelChanging;
    public boolean isEnding;
    public boolean isSettingUp;
    public int level;
    public int totalPoints;
    public ArrayList<Player> players;
    public ArrayList<Player> animationPlayers;
    public ArrayList<Location> animationInUse;
    public ArrayList<Player> alreadyWashed;
    public HashMap<Entity, Player> boatPlayers;
    public BossBar bossBar;
    public WorldManager worldManager;
    public Plugin celebratePlugin;
    public ArrayList<Region> regions;
    public Utils utils;

    @Override
    public void onEnable() {
        // Plugin startup logic
        main = this;
        players = new ArrayList<>();
        animationPlayers = new ArrayList<>();
        mainListener = new MainListener();
        animationPlayers = new ArrayList<>();
        animationInUse = new ArrayList<>();
        alreadyWashed = new ArrayList<>();
        boatPlayers = new HashMap<>();

        isSettingUp=true;

        Bukkit.getPluginManager().registerEvents(mainListener, this);
        Bukkit.getPluginCommand("setminplayers").setExecutor(new SetMinPlayers());
        Bukkit.getPluginCommand("metagangareload").setExecutor(new Reload());
        Bukkit.getPluginCommand("addpoints").setExecutor(new AddPoints());
        Bukkit.getPluginCommand("forcestop").setExecutor(new ForceStop());

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null) {
            new Placeholders().register();
        }

        saveDefaultConfig();

        setup();
        reloadBossBar();
        setRegions();

        worldManager = new WorldManager();
        checkPlayers();

        utils = new Utils();

        new BukkitRunnable(){
            @Override
            public void run() {
                isSettingUp = false;
            }
        }.runTaskLater(main, 100L);
    }

    void setup() {
        bossBar = getServer().createBossBar(ChatColor.AQUA + "" + ChatColor.BOLD + "Points Collected: " + totalPoints + "/" + getConfig().getInt("points-per-level"), BarColor.GREEN, BarStyle.SOLID);
        new BukkitRunnable(){
            @Override
            public void run() {
                for(int i=134;i<=142;i++) {
                    for(int j=74;j<=80;j++) {
                        Bukkit.getWorld(getConfig().getString("level1.world")).getBlockAt(i, j, 91).setType(Material.GLASS);
                    }
                }
            }
        }.runTaskLater(main, 1L);

        //celebratePlugin = Bukkit.getPluginManager().getPlugin("Celebrate");
        new BukkitRunnable(){
            @Override
            public void run() {
                //Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Unloading Celebrate Plugin!");
                //Bukkit.getPluginManager().disablePlugin(celebratePlugin);
            }
        }.runTaskLater(main, 20L);
    }

    public void reloadBossBar() {
        double currentPoints = totalPoints;
        double maxPoints = getConfig().getInt("points-per-level");
        bossBar.setTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Points Collected: " + totalPoints + "/" + getConfig().getInt("points-per-level"));
        bossBar.setProgress(currentPoints/maxPoints);
    }

    public void readyPlayer(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.setExp(0);
        player.setLevel(0);
        player.setFlying(false);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.getInventory().clear();
    }

    void setRegions() {
        regions = new ArrayList<>();
        //0
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 288, 62, 115), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 226, 43, 290)));
        //1
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 109, 62, 290), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 327, 40, 365)));
        //2
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 287, 62, 69), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 133, 41, 20)));
        //3- Near temples Region
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 180, 62, 104), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 223, 44, 288)));
        //4 - Near temples smaller Region
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 180, 62, 104), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 223, 44, 223)));
        //5 - Parrot Location
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 171, 67, 143), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 176, 62, 208)));
        //6 - Parrot Location
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 210, 67, 208), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 203, 62, 128)));
        //7 - Parrot Location
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 290, 61, 235), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 235, 85, 87)));
        //8 - Parrot Location
        regions.add(new Region(new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 144, 73, 128), new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), 138, 83, 204)));
    }

    void placeTrash(int level) {
        ArrayList<String> trashList = new ArrayList<>();
        trashList.add("bottle blue_bottle");
        trashList.add("plastic plastic_bag");
        trashList.add("bottle green_bottle");
        trashList.add("plastic big_plastic_bag");
        trashList.add("garbage garbage_bag");
        trashList.add("garbage big_garbage_bag");

        int max = 0;
        switch(level) {
            case 1:
                max=400;
                break;
            case 2:
                max=350;
                break;
            case 3:
                max=300;
                break;
            case 4:
                max=250;
                break;
            case 5:
                max=200;
                break;
        }
        int finalMax = max;
        new BukkitRunnable(){
            int i = 0;
            @Override
            public void run() {
                if(i>= finalMax) {
                    cancel();
                    return;
                }
                String trash = trashList.get((new Random().nextInt(6)));
                getUtils().placeTrashRandomly(level, trash);
                i++;
            }
        }.runTaskTimer(this, 20L, 1L);
    }

    void checkPlayers() {
        new BukkitRunnable(){
            @Override
            public void run() {
                World mainWorld;
                if(main.level>0) {
                    mainWorld = Bukkit.getWorld(getConfig().getString("level" + main.level + ".world"));
                } else {
                    mainWorld = Bukkit.getWorld(getConfig().getString("spawnLocation.world"));
                }
                for(Player p: Bukkit.getOnlinePlayers()) {
                    World dummyWorld = Bukkit.getWorld(getConfig().getString("dummy-world"));
                    if(p.getWorld().equals(dummyWorld)) {
                        try {
                            Location loc = new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), getConfig().getDouble("spawnLocation.x"), getConfig().getDouble("spawnLocation.y"), getConfig().getDouble("spawnLocation.z"), (float) getConfig().getDouble("spawnLocation.yaw"),(float) getConfig().getDouble("spawnLocation.pitch"));
                            if (!main.isRunning && !main.isFinished) {
                                p.teleport(loc);
                            } else {
                                World world = Bukkit.getWorld(getConfig().getString("level" + main.level + ".world"));
                                loc.setWorld(world);
                                p.teleport(loc);
                            }
                        } catch(Exception err) {
                            p.kickPlayer(ChatColor.RED + "Something went wrong!");
                        }
                    } else if (!p.getWorld().equals(mainWorld)) {
                        if(!p.hasPermission("metaganga.admin")) {
                            p.sendMessage(ChatColor.RED + "Something went wrong. Teleporting you to the spawn location!");
                            Location loc = new Location(mainWorld, getConfig().getDouble("spawnLocation.x"), getConfig().getDouble("spawnLocation.y"), getConfig().getDouble("spawnLocation.z"), (float) getConfig().getDouble("spawnLocation.yaw"), (float) getConfig().getDouble("spawnLocation.pitch"));
                            p.teleport(loc);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    public void startTimer() {
        new BukkitRunnable(){
            @Override
            public void run() {
                if(main.isRunning) {
                    if (main.isLevelChanging) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(main.isRunning) {
                                    forceShutdown();
                                }
                            }
                        }.runTaskLater(main, 200L);
                    } else {
                        forceShutdown();
                    }
                }
            }
        }.runTaskLater(main, 72000L);
    }

    void forceShutdown() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Force stopping the game as 60 minutes timer is over!");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "game stop");
        utils.removeTrash(main.level);
        boatPlayers.clear();
        alreadyWashed.clear();
        level=5;
        World world = Bukkit.getWorld(getConfig().getString("level5.world"));
        for(Player p: Bukkit.getOnlinePlayers()) {
            //p.sendMessage(ChatColor.GREEN);
            if(p.getVehicle()!=null) {
                p.getVehicle().eject();
            }
            if(level<=4) {
                Location loc = p.getLocation();
                loc.setWorld(world);
                p.teleport(loc);
                readyPlayer(p);
            }
        }
        mainListener.stopGame();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance()
    {
        return main;
    }

    public Utils getUtils() {
        return utils;
    }
}
