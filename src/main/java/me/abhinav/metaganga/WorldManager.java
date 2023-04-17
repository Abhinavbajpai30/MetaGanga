package me.abhinav.metaganga;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldManager {
    Main main = Main.getInstance();
    MultiverseCore mv;
    MVWorldManager mvWorldManager;
    WorldManager() {
        mv = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        mvWorldManager = mv.getMVWorldManager();
        setup();
    }

    void setup() {
        /*new BukkitRunnable(){
            @Override
            public void run() {
                mvWorldManager.unloadWorld(main.getConfig().getString("level2.world"));
                mvWorldManager.unloadWorld(main.getConfig().getString("level3.world"));
                mvWorldManager.unloadWorld(main.getConfig().getString("level4.world"));
                mvWorldManager.unloadWorld(main.getConfig().getString("level5.world"));
            }
        }.runTaskLater(main, 60L);*/
    }

    void reloadWorlds() {
        int level = main.level;
        //mvWorldManager.loadWorld(main.getConfig().getString("level" + (level+1) + ".world"));
        new BukkitRunnable(){
            @Override
            public void run() {
                //mvWorldManager.unloadWorld((main.getConfig().getString("level" + (level) + ".world")));
            }
        }.runTaskLater(main, 200L);
    }
}
