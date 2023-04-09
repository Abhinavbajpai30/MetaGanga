package me.abhinav.metaganga;

import me.abhinav.metaganga.Commands.AddPoints;
import me.abhinav.metaganga.Commands.ForceStop;
import me.abhinav.metaganga.Commands.Reload;
import me.abhinav.metaganga.Commands.SetMinPlayers;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public final class Main extends JavaPlugin {
    private static Main main;
    public MainListener mainListener;
    public boolean isRunning;
    public boolean isFinished;
    public boolean isStarting;
    public boolean isLevelChanging;
    public boolean isEnding;
    public int level;
    public int totalPoints;
    public ArrayList<Player> players;
    public ArrayList<Player> animationPlayers;
    public ArrayList<Location> animationInUse;
    public ArrayList<Player> alreadyWashed;
    public BossBar bossBar;
    public WorldManager worldManager;
    public Plugin celebratePlugin;

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

        worldManager = new WorldManager();
        checkPlayers();
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
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Unloading Celebrate Plugin!");
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

    void checkPlayers() {
        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player p: Bukkit.getOnlinePlayers()) {
                    World dummyWorld = Bukkit.getWorld(getConfig().getString("dummy-world"));
                    if(p.getWorld().equals(dummyWorld)) {
                        try {
                            Location loc = new Location(Bukkit.getWorld(getConfig().getString("spawnLocation.world")), getConfig().getDouble("spawnLocation.x"), getConfig().getDouble("spawnLocation.y"), getConfig().getDouble("spawnLocation.z"), (float) getConfig().getDouble("spawnLocation.yaw"),(float) getConfig().getDouble("spawnLocation.pitch"));
                            if (!main.isRunning) {
                                p.teleport(loc);
                            } else {
                                World world = Bukkit.getWorld(getConfig().getString("level" + main.level + ".world"));
                                loc.setWorld(world);
                                p.teleport(loc);
                            }
                        } catch(Exception err) {
                            p.kickPlayer(ChatColor.RED + "Something went wrong!");
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance()
    {
        return main;
    }
}
