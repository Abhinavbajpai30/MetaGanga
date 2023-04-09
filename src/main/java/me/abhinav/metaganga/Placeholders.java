package me.abhinav.metaganga;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
    Main main = Main.getInstance();
    @Override
    public @NotNull String getIdentifier() {
        return "metaganga";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MetaGanga";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if(params.equalsIgnoreCase("totalpoints")) {
            if(!main.isRunning && !main.isFinished) {
                return "0";
            }
            return main.totalPoints+"";
        }
        if(params.equalsIgnoreCase("level")) {
            if(!main.isRunning && !main.isFinished) {
                return "0";
            }
            return main.level+"";
        }
        return null;
    }
}
