package me.abhinav.metaganga.Commands;

import me.abhinav.metaganga.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reload implements CommandExecutor {
    Main main = Main.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(!player.hasPermission("metaganga.admin")) {
                player.sendMessage(ChatColor.RED + "Command not found!");
                return false;
            }
        }
        main.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Successfully reload the config!");
        return true;
    }
}
