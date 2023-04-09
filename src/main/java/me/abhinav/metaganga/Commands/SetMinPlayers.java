package me.abhinav.metaganga.Commands;

import me.abhinav.metaganga.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMinPlayers implements CommandExecutor {
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
        if(args.length!=1) {
            sender.sendMessage(ChatColor.RED + "Format - /setminplayers (number)");
            return false;
        }
        int min;
        try{
            min = Integer.parseInt(args[0]);
        } catch(NumberFormatException err) {
            sender.sendMessage(ChatColor.RED + "You must enter a number!");
            return false;
        }
        main.getConfig().set("min-players", min);
        main.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Successfully set minimum players " + min + "!");

        return true;
    }
}
