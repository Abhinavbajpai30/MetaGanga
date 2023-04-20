package me.abhinav.metaganga;

import dev.lone.itemsadder.api.CustomPlayer;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.facet.Facet;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class MainListener implements Listener {
    Main main = Main.getInstance();
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        e.setJoinMessage(null);
        if(!main.isFinished && !main.isRunning && !main.isStarting) {
            main.readyPlayer(e.getPlayer());
            if(Bukkit.getOnlinePlayers().size()>=main.getConfig().getInt("min-players")) {
                main.isStarting = true;
                new BukkitRunnable(){
                    int i=10;
                    @Override
                    public void run() {
                        if(Bukkit.getOnlinePlayers().size()<main.getConfig().getInt("min-players")) {
                            for(Player p: Bukkit.getOnlinePlayers()) {
                                p.sendTitle("", "");
                                p.sendMessage(ChatColor.RED + "Waiting for more players...");
                            }
                            main.isStarting=false;
                            cancel();
                            return;
                        }
                        if(i==0) {
                            startGame();
                            cancel();
                            return;
                        }
                        for(Player p: Bukkit.getOnlinePlayers()) {
                            p.sendTitle(ChatColor.BOLD + "" + ChatColor.AQUA  + "Starting in " + ChatColor.RED + i + ChatColor.AQUA + " seconds!", ChatColor.GREEN + "Get Ready!");
                            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        }
                        i--;
                    }
                }.runTaskTimer(main, 20L, 20L);
            } else {
                for(Player p: Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.AQUA + "" + (main.getConfig().getInt("min-players") - Bukkit.getOnlinePlayers().size()) + " more players required to Start!");
                }
            }
        }
        if(main.isRunning || main.isFinished) {
            Player player = e.getPlayer();
            main.bossBar.addPlayer(player);
            World dummyWorld = Bukkit.getWorld(main.getConfig().getString("dummy-world"));
            if(!player.getWorld().equals(dummyWorld)) {
                try {
                    Location loc = new Location(Bukkit.getWorld(main.getConfig().getString("level" + main.level + ".world")), main.getConfig().getDouble("spawnLocation.x"), main.getConfig().getDouble("spawnLocation.y"), main.getConfig().getDouble("spawnLocation.z"), (float) main.getConfig().getDouble("spawnLocation.yaw"),(float) main.getConfig().getDouble("spawnLocation.pitch"));
                    player.teleport(loc);
                } catch(Exception err) {
                    player.kickPlayer(ChatColor.RED + "Something went wrong!");
                }
            }
        }
    }

    void startGame() {
        main.startTimer();
        main.isRunning = true;
        main.isStarting=false;
        main.level=1;
        main.placeTrash(1);
        for(int i=134;i<=142;i++) {
            for(int j=74;j<=80;j++) {
                Bukkit.getWorld(main.getConfig().getString("level1.world")).getBlockAt(i, j, 91).setType(Material.AIR);
            }
        }
        for(Player p: Bukkit.getOnlinePlayers()) {
            main.players.add(p);
            main.bossBar.addPlayer(p);
            main.readyPlayer(p);
            p.sendTitle(ChatColor.GREEN + "The Doors have been opened!", ChatColor.AQUA + "Move forward and help us in clearing the river.");
            p.playSound(p, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "game start");
    }

    public void nextLevel() {
        main.getConfig().set("points-collected", main.getConfig().getInt("points-collected") + main.getConfig().getInt("points-per-level"));
        main.saveConfig();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "game stop");
        main.utils.removeTrash(main.level);
        main.boatPlayers.clear();
        main.alreadyWashed.clear();
        if(main.level>=5) {
            stopGame();
            return;
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                main.worldManager.reloadWorlds();
            }
        }.runTaskLater(main, 20L);
        main.placeTrash(main.level+1);

        main.isLevelChanging = true;
        for(Player p: Bukkit.getOnlinePlayers()) {
            if(p.getVehicle()!=null) {
                p.getVehicle().eject();
            }
            p.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "You have completed Level " + main.level, ChatColor.AQUA + "Level " + (main.level+1) + " will begin soon!");
            p.sendMessage(ChatColor.GREEN + "You have Completed Level " + main.level + "!");
            p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        String title="";
        String subtitle="";
        String message="";
        ArrayList<ItemStack> items = new ArrayList<>();
        ItemStack helmet = null;
        ItemStack chestplate = null;
        ItemStack leggings = null;
        ItemStack boots = null;

        ToastNotification notification = null;

        switch(main.level+1) {
            case 2:
                title = ChatColor.GREEN + "You received " + ChatColor.RED + "Fishing Rod" + ChatColor.GREEN + " as your reward!";
                subtitle = ChatColor.AQUA + "Thank you for your contribution!";
                message = ChatColor.GREEN + "You received " + ChatColor.RED + "Fishing Rod" + ChatColor.GREEN + " as your reward! Thank you for your contribution!";
                items.add(new ItemStack(Material.FISHING_ROD));

                notification = new ToastNotification(new ItemStack(Material.PUFFERFISH), new TextComponent(ChatColor.DARK_GREEN + "Frogs have returned to the river!").toLegacyText(), AdvancementDisplay.AdvancementFrame.TASK);
                break;
            case 3:
                title = ChatColor.GREEN + "You received " + ChatColor.RED + "Boat" + ChatColor.GREEN + " as your reward!";
                subtitle = ChatColor.AQUA + "Thank you for your contribution!";
                message = ChatColor.GREEN + "You received " + ChatColor.RED + "Boat" + ChatColor.GREEN + " as your reward! Thank you for your contribution!";
                items.add(new ItemStack(Material.FISHING_ROD));
                items.add(new ItemStack(Material.OAK_BOAT));

                notification = new ToastNotification(new ItemStack(Material.TROPICAL_FISH), new TextComponent(ChatColor.GOLD + "Good news! Fish and birds have returned to the river!").toLegacyText(), AdvancementDisplay.AdvancementFrame.TASK);
                break;
            case 4:
                title = ChatColor.GREEN + "You received " + ChatColor.RED + "Turtle Shell" + ChatColor.GREEN + " as your reward!";
                subtitle = ChatColor.AQUA + "Thank you for your contribution!";
                message = ChatColor.GREEN + "You received " + ChatColor.RED + "Turtle Shell" + ChatColor.GREEN + " as your reward! Thank you for your contribution!";
                helmet = new ItemStack(Material.TURTLE_HELMET);
                ItemMeta meta1 = helmet.getItemMeta();
                meta1.addEnchant(Enchantment.OXYGEN, 3, true);
                meta1.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                helmet.setItemMeta(meta1);

                notification = new ToastNotification(new ItemStack(Material.SCUTE), new TextComponent(ChatColor.GREEN + "Turtles have been spotted basking in the sun on the riverbanks!").toLegacyText(), AdvancementDisplay.AdvancementFrame.TASK);
                break;
            case 5:
                title = ChatColor.GREEN + "You received " + ChatColor.RED + "Spear" + ChatColor.GREEN + " as your reward!";
                subtitle = ChatColor.AQUA + "Thank you for your contribution!";
                message = ChatColor.GREEN + "You received " + ChatColor.RED + "Spear" + ChatColor.GREEN + " as your reward! Thank you for your contribution!";
                ItemStack trident = new ItemStack(Material.TRIDENT);
                ItemMeta tridentMeta = trident.getItemMeta();
                tridentMeta.addEnchant(Enchantment.RIPTIDE, 3, true);
                tridentMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                tridentMeta.setDisplayName("Spear");
                trident.setItemMeta(tridentMeta);
                items.add(trident);
                helmet = new ItemStack(Material.TURTLE_HELMET);
                ItemMeta meta2 = helmet.getItemMeta();
                meta2.addEnchant(Enchantment.OXYGEN, 3, true);
                meta2.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                helmet.setItemMeta(meta2);

                notification = new ToastNotification(new ItemStack(Material.HEART_OF_THE_SEA), new TextComponent(ChatColor.AQUA + "Playful dolphin seen back in river after years.").toLegacyText(), AdvancementDisplay.AdvancementFrame.TASK);
                break;
        }

        String finalTitle = title;
        String finalSubtitle = subtitle;
        String finalMessage = message;
        ItemStack finalHelmet = helmet;
        ToastNotification finalNotification = notification;
        new BukkitRunnable(){
            @Override
            public void run() {
                main.level++;
                String path = "level"+main.level;
                World world = Bukkit.getWorld(main.getConfig().getString(path+".world"));
                //Location loc = new Location(world, main.getConfig().getInt(path+".x"), main.getConfig().getInt(path+".y"), main.getConfig().getInt(path+".z"));

                for(Player p: Bukkit.getOnlinePlayers()) {
                    Location loc = p.getLocation();
                    loc.setWorld(world);
                    p.teleport(loc);
                    main.readyPlayer(p);
                    p.sendTitle(finalTitle, finalSubtitle);
                    p.sendMessage(finalMessage);
                    p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                    for(ItemStack item: items) {
                        p.getInventory().addItem(item);
                    }
                    p.getInventory().setHelmet(finalHelmet);
                    p.getInventory().setChestplate(chestplate);
                    p.getInventory().setLeggings(leggings);
                    p.getInventory().setBoots(boots);

                    if(finalNotification!=null) {
                        finalNotification.send(p);
                    }
                }
                main.totalPoints=0;
                main.reloadBossBar();
                main.isLevelChanging = false;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "game start");
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        main.getUtils().spawnMobs();
                        main.getUtils().removeMobs(main.level-1);
                    }
                }.runTaskLater(main, 20L);
            }
        }.runTaskLater(main, 100L);

    }

    public void stopGame() {
        main.isFinished = true;
        main.isRunning = false;
        checkLeftPlayers();
        //Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loading Celebrate Plugin!");
        //Bukkit.getPluginManager().enablePlugin(main.celebratePlugin);
        String title = ChatColor.AQUA + "" + ChatColor.BOLD + "Thank you for your contribution";
        String subtitle = ChatColor.GREEN + "in cleaning the river. Please proceed to wash your hands.";
        String message = ChatColor.GREEN + "Thank you for your contribution in cleaning the water!";
        main.bossBar.setTitle(ChatColor.AQUA + "Congratulations! You have successfully finished the task.");
        main.bossBar.setProgress(1);
        for(Player p: Bukkit.getOnlinePlayers()) {
            String finalSubtitle = PlaceholderAPI.setPlaceholders(p, subtitle);
            p.sendTitle(title, finalSubtitle);
            String finalMessage = PlaceholderAPI.setPlaceholders(p, message);
            p.sendMessage(finalMessage);
            p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + p.getName() + " parent add finished");
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                World world = Bukkit.getWorld(main.getConfig().getString("level5.world"));
                main.isEnding = true;
                setupEndingBossbar();
                for(Player player: Bukkit.getOnlinePlayers()) {
                    main.readyPlayer(player);
                    player.sendMessage(ChatColor.AQUA + "Please wash your hands at any nearby washing station to save your score!");
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
            }
        }.runTaskLater(main, 100L);
    }

    void checkLeftPlayers() {
        for(Player p: main.players) {
            if(!p.isOnline()) {
                //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reset uuid " + p.getUniqueId());
            }
        }
    }

    void setupEndingBossbar() {
        int minutes = 5;
        int seconds = 0;
        main.bossBar.setTitle(ChatColor.GREEN + "Please Wash your hands within " + ChatColor.RED + minutes + " minute" + ChatColor.GREEN + " and " + ChatColor.RED + seconds + " seconds" + ChatColor.GREEN + "!");
        main.bossBar.setProgress(1);
        main.bossBar.setColor(BarColor.RED);
        new BukkitRunnable(){
            int min = minutes;
            int sec = seconds;
            final double finalTotal = (min*60) + sec;
            @Override
            public void run() {
                if(sec>=1) {
                    sec--;
                } else {
                    if(min>=1) {
                        min--;
                        sec=60;
                    } else {
                        cancel();
                        endGame();
                        return;
                    }
                }
                double total = (min*60) + sec;
                if(min==0) {
                    main.bossBar.setTitle(ChatColor.GREEN + "Please Wash your hands within " + ChatColor.RED + sec + " seconds" + ChatColor.GREEN + "!");
                } else if(sec==0) {
                    main.bossBar.setTitle(ChatColor.GREEN + "Please Wash your hands within " + ChatColor.RED + min + " minute " + ChatColor.GREEN + "!");
                } else {
                    main.bossBar.setTitle(ChatColor.GREEN + "Please Wash your hands within " + ChatColor.RED + min + " minute" + ChatColor.GREEN + " and " + ChatColor.RED + sec + " seconds" + ChatColor.GREEN + "!");
                }
                main.bossBar.setProgress(total/finalTotal);
            }
        }.runTaskTimer(main, 20L, 20L);
    }

    void endGame() {
        for(Player p: Bukkit.getOnlinePlayers()) {
            finishPlayer(p);
        }
        //Shutting down the server forcefully if it is not done by PlayerQuitEvent
        new BukkitRunnable(){
            @Override
            public void run() {
                main.getServer().shutdown();
            }
        }.runTaskLater(main, 200L);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        if(player.hasPermission("metaganga.admin")) {
            return;
        }
        /*if(player.hasPermission("metaganga.finished")) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "You have already played and made your contribution!");
            return;
        }*/
        if(main.isRunning) {
            if(main.players.contains(player)) {
                player.sendMessage(ChatColor.GREEN + "You have rejoined the game!");
                return;
            }
        }
        if(main.isRunning || main.isFinished) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Game has been already started. Please wait!");
        }
        if(Bukkit.getOnlinePlayers().size() >= main.getConfig().getInt("max-players")) {
            if(!player.hasPermission("metaganga.admin")) {
                e.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.RED + "Game is full!");
            }
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent e) {
        if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.COMMAND) || e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM) || e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.EGG)) {
            e.getEntity().setRemoveWhenFarAway(false);
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if(player.hasPermission("admin.place")) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if((e.getDamager() instanceof Player)) {
            if(!e.getDamager().hasPermission("admin.damage")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setCancelled(true);
            e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if(player.hasPermission("admin.build")) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if(!player.hasPermission("metaganga.admin")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehiclePlace(EntityPlaceEvent e) {
        Player player = e.getPlayer();
        if(main.isRunning && !main.isLevelChanging && main.level==3) {
            if(e.getEntity() instanceof Boat) {
                main.boatPlayers.put(e.getEntity(), player);
                return;
            }
        }
        if(player!=null && player.hasPermission("metaganga.admin")) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onRide(VehicleEnterEvent e) {
        if(e.getEntered() instanceof Player) {
            Player player = (Player) e.getEntered();
            if(player.hasPermission("metaganga.admin")) {
                return;
            }
            if(main.isRunning && !main.isLevelChanging && main.level==3) {
                if(e.getVehicle() instanceof Boat) {
                    return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        if(e.getAttacker() instanceof Player) {
            Player player = (Player) e.getAttacker();
            if(main.isRunning && !main.isLevelChanging && main.level==3) {
                if(main.boatPlayers.containsKey(e.getVehicle())) {
                    if(main.boatPlayers.get(e.getVehicle()).equals(player)) {
                        main.boatPlayers.remove(e.getVehicle());
                        player.getInventory().addItem(new ItemStack(Material.OAK_BOAT));
                        return;
                    } else {
                        if(player.hasPermission("metaganga.admin")) {
                            return;
                        }
                        player.sendMessage(ChatColor.RED + "You can only break your own boat!");
                        e.setCancelled(true);
                    }
                }
            }
            if(player.hasPermission("metaganga.admin")) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if(main.animationPlayers.contains(player)) {
            if(!player.hasPermission("metaganga.admin")) {
                Location to = e.getFrom();
                to.setY(e.getTo().getYaw());
                to.setPitch(e.getTo().getPitch());
                //e.setTo(to);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(main.isEnding) {
            Player player = e.getPlayer();
            if(!player.hasPermission("metaganga.admin")) {
                player.sendMessage(ChatColor.RED + "Chat is disabled!");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if(main.animationPlayers.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        if(main.isRunning || main.isFinished || main.isSettingUp) {
            e.setMotd("Game in progress!");
        } else {
            e.setMotd("Waiting for more players!");
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        e.getInventory().setResult(null);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if((main.isRunning || main.isEnding) && !main.isLevelChanging && e.getHand().equals(EquipmentSlot.HAND) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock()!=null && e.getClickedBlock().getType().equals(Material.FLETCHING_TABLE)) {
            if(main.animationPlayers.contains(player)) {
                return;
            }
            if(!player.hasPermission("metaganga.admin") && main.alreadyWashed.contains(player)) {
                player.sendMessage(ChatColor.RED + "You have already washed your hands this round!");
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            if(main.animationInUse.contains(e.getClickedBlock().getLocation())) {
                player.sendMessage(ChatColor.RED + "Someone else is using the Washing Basin!");
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            main.animationPlayers.add(player);
            main.alreadyWashed.add(player);
            main.animationInUse.add(e.getClickedBlock().getLocation());
            e.getClickedBlock().setType(Material.REINFORCED_DEEPSLATE);
            Location loc = e.getClickedBlock().getLocation();
            loc.add(1.3, 0, 0.5);
            loc.setYaw(90f);
            loc.setPitch(20f);
            player.teleport(loc);
            if(main.isEnding) {
                for(Player p: Bukkit.getOnlinePlayers()) {
                    player.hidePlayer(main, p);
                }
            }
            new BukkitRunnable(){
                @Override
                public void run() {
                    CustomPlayer.playEmote(player, "show_item");
                }
            }.runTaskLater(main, 20L);
            new BukkitRunnable(){
                @Override
                public void run() {
                    CustomPlayer.stopEmote(player);
                    player.sendMessage(ChatColor.AQUA + "You have washed and sanitized your hands with Lifebuoy!");
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    e.getClickedBlock().setType(Material.FLETCHING_TABLE);
                    if(main.isEnding) {
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                main.animationPlayers.remove(player);
                                main.animationInUse.remove(e.getClickedBlock().getLocation());
                                finishPlayer(player);
                            }
                        }.runTaskLater(main, 40L);
                    } else {
                        main.animationPlayers.remove(player);
                        main.animationInUse.remove(e.getClickedBlock().getLocation());
                    }
                }
            }.runTaskLater(main, 100L);
        } else if(!player.hasPermission("metaganga.admin")) {
            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.PHYSICAL)) {
                if(e.getPlayer().getItemInHand().getType().equals(Material.OAK_BOAT) && e.getClickedBlock().getType().equals(Material.WATER)) {
                    return;
                }
                e.setCancelled(true);
            }
        }
    }

    void finishPlayer(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iaplaytotemanimation animatedtitles:lifeboy " + player.getName());
        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        String kickmsg = "";
        kickmsg+=ChatColor.GREEN + "Thank you for collecting %trashpoints_points% points,\n";
        kickmsg+="which equals %trashpoints_points% grams of plastic waste.\n";
        kickmsg+="\n";
        kickmsg+="We will match your score and collect the same amount of plastic\n";
        kickmsg+="to build handwashing stations and\n";
        kickmsg+="serve children of this community.\n";
        kickmsg+="\n";
        kickmsg+="A big thank you\n";
        kickmsg+="from both, the children\n";
        kickmsg+="and the river.\n";

        String finalKickmsg = PlaceholderAPI.setPlaceholders(player, kickmsg);;
        new BukkitRunnable(){
            @Override
            public void run() {
                player.kickPlayer(finalKickmsg);
            }
        }.runTaskLater(main, 100L);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if(!player.hasPermission("metaganga.admin")) {
            player.sendMessage(ChatColor.RED + "You are not allowed to drop an item!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            if(!player.hasPermission("metaganga.admin")) {
                if (e.getSlotType().equals(InventoryType.SlotType.ARMOR) || e.getSlotType().equals(InventoryType.SlotType.RESULT)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if(main.isRunning || main.isFinished) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    if(Bukkit.getOnlinePlayers().size()==0) {
                        if(main.isFinished) {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Game finished and all the players left. Restarting the server!");
                        } else {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "All players left in between the game. Restarting the server!");
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.getServer().shutdown();
                            }
                        }.runTaskLater(main, 20L);
                    }
                }
            }.runTaskLater(main, 20L);
        }
    }
}
