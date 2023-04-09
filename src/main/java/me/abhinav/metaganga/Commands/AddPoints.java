package me.abhinav.metaganga.Commands;

import me.abhinav.metaganga.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddPoints implements CommandExecutor {
    Main main = Main.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player player) {
            if(!player.hasPermission("metaganga.admin")) {
                player.sendMessage(ChatColor.RED + "Command not found!");
                return false;
            }
        }
        if(main.isFinished) {
            return false;
        }
        if(!main.isRunning) {
            sender.sendMessage(ChatColor.RED + "Event has not started yet!");
            return false;
        }
        if(main.isLevelChanging) {
            return false;
        }
        if(args.length!=1) {
            sender.sendMessage(ChatColor.RED + "Format - /addpoints (points)");
            return false;
        }
        int points;
        try {
            points = Integer.parseInt(args[0]);
        } catch (NumberFormatException err) {
            sender.sendMessage(ChatColor.RED + "Points must be a number!");
            return false;
        }
        main.totalPoints+=points;
        if(main.totalPoints>=main.getConfig().getInt("points-per-level")) {
            main.totalPoints = 550;
            main.reloadBossBar();
            main.mainListener.nextLevel();
            return true;
        }
        main.reloadBossBar();
        return false;
    }
}
