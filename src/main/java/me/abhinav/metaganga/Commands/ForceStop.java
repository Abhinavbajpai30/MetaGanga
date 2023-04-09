package me.abhinav.metaganga.Commands;

import me.abhinav.metaganga.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ForceStop implements CommandExecutor {
    Main main = Main.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player player) {
            if(!player.hasPermission("metaganga.admin")) {
                player.sendMessage(ChatColor.RED + "Command not found!");
                return false;
            }
        }
        if(!main.isRunning) {
            sender.sendMessage(ChatColor.RED + "Game is not running! ");
            return false;
        }
        main.isRunning=false;
        main.isLevelChanging=false;
        main.isStarting=false;
        main.isFinished=true;
        sender.sendMessage(ChatColor.GREEN + "Forcefully stopped the game!");
        return false;
    }
}
