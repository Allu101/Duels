package com.allu.duels;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;


public class Events implements Listener, CommandExecutor {

	private Lobby lobby;
	private MenuHandler menuHandler;

	public Events(Lobby lobby, MenuHandler menuHandler) {
		this.lobby = lobby;
		this.menuHandler = menuHandler;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			System.out.println("[Duels] Komentoa ei voi käyttää consolesta");
			return true;
		}
		
		Player p = (Player)sender;
		DuelsPlayer dp = lobby.getDuelsPlayer(p);
		
		if(cmd.getName().equalsIgnoreCase("duel")) {
			
			if (dp.getGameWhereJoined() != null) {
				p.sendMessage("§cEt voi tehdä tätä, kun olet pelissä!");
				return true;
			}
			
			if (args.length == 0) {
				lobby.addPlayerToRankedQueue(p);
				return true;
			}
			
			if(args.length % 2 != 0) {
				lobby.removePlayerFromRankedQueue(p);
				List<DuelsPlayer> players = new ArrayList<>();

				for (String name : args) {
					Player opponent = Bukkit.getPlayerExact(name);
					if(opponent == null) {
						p.sendMessage(ChatColor.RED + name + " nimistä pelaajaa ei löydy.");
						return true;
					}
					DuelsPlayer opponentDp = lobby.getDuelsPlayer(opponent);
					if (opponent.getName().equals(p.getName())) {
						p.sendMessage(ChatColor.RED + "Et voi haastaa itseasi duelsiin.");
						return true;
					}
					if(opponentDp.getGameWhereJoined() != null) {
						p.sendMessage(ChatColor.RED + "Et voi haastaa pelaajaa " + name + ", koska hänellä on peli menossa.");
						return true;
					}
					players.add(opponentDp);
				}
				dp.setChallengedPlayers(players);
				p.openInventory(menuHandler.createKitMenu());

				return true;
			}
			
			if(args.length == 2) {
				if(args[0].equalsIgnoreCase("accept")) {
					
					long challengeID = 0;
					try {
						challengeID  = Long.parseLong(args[1]);
					}
					catch (NumberFormatException e) {
						p.sendMessage(ChatColor.RED + "Virheellinen komento!");
						return true;
					}
					
					Challenge challenge = lobby.getChallenge(challengeID);
					
					// Return failure, if non exist, or not pointed to the player.
					if (challenge == null || !challenge.getChallengedPlayers().contains(dp)) {
						p.sendMessage(ChatColor.GRAY + "Haaste, jota koitat hyväksyä ei ole voimassa.");
						return true;
					}
					challenge.acceptChallenge(p.getUniqueId().toString());

					if (challenge.hasAllAccept()) {
						lobby.startNewDuelsMatch(challenge.getDuelsPlayers(), challenge.getKit(),
								DuelsGame.GameType.FRIEND_CHALLENGE, p);
					}
					
					return true;
				}
			}
			p.sendMessage(lobby.LINE);
			p.sendMessage(ChatColor.AQUA + "(1v1) /duel <pelaajan_nimi>");
			p.sendMessage(ChatColor.AQUA + "(2v2) /duel <pelaajan1_nimi> <pelaajan2_nimi> <pelaajan3_nimi>.");
			p.sendMessage(lobby.LINE);
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("spectate")) {
			
			if(args.length == 1) {
				
				Player targetPlayer = Bukkit.getPlayerExact(args[0].toString());
				
				if(targetPlayer == null) {
					p.sendMessage(ChatColor.RED + "Tämän nimistä pelaajaa ei löydy.");
					return true;
				}
				
				if (targetPlayer.equals(p)) {
					p.sendMessage(ChatColor.RED + "Et voi spectatea itseäsi.");
					return true;
				}
				
				DuelsPlayer targetDp = lobby.getDuelsPlayer(targetPlayer);
				DuelsGame game = targetDp.getGameWhereJoined();
				
				if(game == null) {
					p.sendMessage(ChatColor.RED + "Kyseinen pelaaja on tällä hetkellä lobbyssa");
				}
				else {
					if(dp.getGameWhereJoined() != null) {
						dp.getGameWhereJoined().leaveGame(dp);
					}
					
					lobby.clearPlayerInventoryAndEquipment(p.getInventory());
					lobby.clearPotionEffect(p);
					
					p.setGameMode(GameMode.SPECTATOR);
					p.teleport(targetPlayer);
					
					p.setScoreboard(dp.getSidebarHandler().getSpectatorBoard());
					
					
					List<String> playerNames = new ArrayList<>();
					for (DuelsPlayer duelsPlayer : game.getPlayers()) {
						playerNames.add(duelsPlayer.getPlayer().getName());
					}
					
					dp.getSidebarHandler().updateSpectatorSidebar(game.getGameTypeString(), game.getKit().getName(), playerNames.toArray(new String[0]));
				}

			}
			else {
				p.sendMessage("§cKäyttö: /spectate <pelaajan nimi>");
			}
			
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("lobby")) {
			
			if(dp.getGameWhereJoined() != null) {
				dp.getGameWhereJoined().leaveGame(dp);
			}
			
			lobby.sendPlayerToLobby(dp);

			return true;
		}	
		
		return false;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		DuelsGame game = lobby.getDuelsPlayer(p).getGameWhereJoined();
		if(game != null && game.isGameOn()) {
			Location bLoc = e.getBlock().getLocation();
			if(game.getPlacedBlocks().contains(bLoc)) {
				game.getPlacedBlocks().remove(bLoc);
				return;
			}
		}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		DuelsGame game = lobby.getDuelsPlayer(p).getGameWhereJoined();
		if(game != null && game.isGameOn()) {
			game.getPlacedBlocks().add(e.getBlock().getLocation());
			return;
		}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		
		if (!e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
			return;
		}
		
		DuelsPlayer dp = lobby.getDuelsPlayer(e.getPlayer());
		if (dp == null) return;
		
		DuelsGame game = dp.getGameWhereJoined();
		if (game == null)
			return;
		
	    Material m = e.getPlayer().getLocation().getBlock().getType();
	    if (game.isGameOn() && m == Material.STATIONARY_WATER || m == Material.WATER) {
	        game.onPlayerDie(e.getPlayer());
	    }
	    
	    if (game.isGameStarting() && ((e.getTo().getX() != e.getFrom().getX()) || (e.getTo().getZ() != e.getFrom().getZ()))) {
            e.setTo(e.getFrom());
            return;
        }
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		ItemStack is = e.getCurrentItem();
		if(is == null || is.getType().equals(Material.AIR)) {
			return;
		}
		Player p = (Player) e.getWhoClicked();
		if(e.getClick().isKeyboardClick() && !p.getWorld().getName().equals(Duels.getLobbyWorldName())) {
			return;
		}
		
		DuelsPlayer dp = lobby.getDuelsPlayer(p);
		if (dp != null && dp.getGameWhereJoined() != null)
			return;
		
		e.setCancelled(true);
		menuHandler.inventoryClickHandler(lobby.getDuelsPlayer(p), e.getCurrentItem());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent e){
        if (lobby.isLobbyWorld(e.getEntity()))
        	e.setCancelled(true);
        
        if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player damaged = (Player) e.getEntity();	
		DuelsGame gameWhereJoined = lobby.getDuelsPlayer(damaged).getGameWhereJoined();
		
		if (gameWhereJoined == null) {
			return;
		}
		
		if (!gameWhereJoined.isGameOn()) {
			e.setCancelled(true);
			return;
		}
		if (gameWhereJoined.isDamageDisabled()) {
			e.setDamage(0.001);
		}

		// Checks if player is under arena. The player is announced as being dead.
		if (gameWhereJoined.isUnderArena(damaged.getLocation())) {
			e.setCancelled(true);
			gameWhereJoined.onPlayerDie(damaged);
		}
		
		if(damaged.getHealth() - e.getFinalDamage() > 0) {
			return;
		}
		// Player would be dead at this point, so cancel and do other stuff --->
		e.setCancelled(true);
		gameWhereJoined.onPlayerDie(damaged);
        
    }
	
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player damaged = (Player) e.getEntity();
		if (lobby.isLobbyWorld(damaged)) {
			e.setCancelled(true);
			
			if (!(e.getDamager() instanceof Player)) {
				return;
			}
			Player damager = (Player) e.getDamager();
			
			if (damager.getItemInHand().equals(menuHandler.getChallengeItem())) {
				damager.performCommand("duel " + damaged.getName());
			}
		}
		
		if (e.getDamager() instanceof Player) {
			lobby.getDuelsPlayer((Player)e.getDamager()).gameDamageDone += e.getFinalDamage();
		}
		if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)e.getDamager();
			if (arrow.getShooter() instanceof Player) {
				lobby.getDuelsPlayer((Player)arrow.getShooter()).gameDamageDone += e.getFinalDamage();
			}
		}
	}
	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (p.getInventory().getItemInHand().equals(menuHandler.getQueueItem())) {
			DuelsPlayer dpp = lobby.getDuelsPlayer(p);
			
			if (dpp.getGameWhereJoined() == null) {
				lobby.removeChallengesWithPlayers(lobby.getDuelsPlayer(p));
				lobby.addPlayerToRankedQueue(p);
			} else {
				p.sendMessage("§cOlet jo pelissä!");
			}
		}
		else if (p.getInventory().getItemInHand().equals(menuHandler.getExitQueueItem())) {
			DuelsPlayer dpp = lobby.getDuelsPlayer(p);
			if (dpp.getGameWhereJoined() == null) {
				lobby.removePlayerFromRankedQueue(p);
			}
		}
		else if (p.getInventory().getItemInHand().getType() == Material.ENDER_PEARL) {
			DuelsPlayer dpp = lobby.getDuelsPlayer(p);
			if (dpp.getGameWhereJoined() != null && !dpp.getGameWhereJoined().isGameOn()) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		final Player player = event.getPlayer();

        if (event.getItem().getType().equals(Material.POTION)) {
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Duels.plugin, new Runnable() {
                public void run() {
                    player.setItemInHand(new ItemStack(Material.AIR));
                }
            }, 1L);
        }
	}
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().setFoodLevel(20);
		lobby.onPlayerJoin(e.getPlayer());
	}
	
	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (p == null) {
			return;
		}
		DuelsPlayer dp = lobby.getDuelsPlayer(p);
		if (dp == null) {
			return;
		}
		if (dp.getGameWhereJoined() != null) {
			dp.getGameWhereJoined().leaveGame(dp);
		}
		lobby.onPlayerLeave(dp);
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		
		if (e.getPlayer() == null)
			return;
		
		Player p = e.getPlayer();
		
		if (e.getCause().equals(TeleportCause.PLUGIN)) {
			return;
		}
		
		if (e.getCause().equals(TeleportCause.SPECTATE)) {
			e.setCancelled(true);
			return;
		}
		
		if (p.getGameMode().equals(GameMode.SPECTATOR)) {
			p.setSpectatorTarget(p);
			return;
		}
		
		if (e.getCause().equals(TeleportCause.ENDER_PEARL)) {

			DuelsGame gameWhereJoined = lobby.getDuelsPlayer(p).getGameWhereJoined();
			
			if (gameWhereJoined != null &&
					(!gameWhereJoined.isWithinArena(e.getTo()) || !gameWhereJoined.isNearArenaFloorLevel(e.getTo().getY()))) {
				e.setCancelled(true);
				p.sendMessage(ChatColor.RED + "Areenalta ei saa poistua!");
			} else {
				e.setCancelled(true);
				p.teleport(e.getTo());
			}
			return;
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if(e.getPlayer() != null) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (!e.getPlayer().isOp())
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerItemDamage(PlayerItemDamageEvent e) {
		e.getItem().setDurability((short)0);
		e.setCancelled(true);
	}
	
	@EventHandler
	public void creatureSpawn(CreatureSpawnEvent e) {
		if(!e.getEntityType().equals(EntityType.ARMOR_STAND) && e.getSpawnReason() == SpawnReason.DEFAULT) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Prevent hunger
	 * @param e
	 */
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		e.setFoodLevel(20);
		e.setCancelled(true);
	}
	
	/**
	 * Prevent raining on any of the worlds
	 * @param e
	 */
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (e.toWeatherState()) { // If starting to rain
			e.setCancelled(true);
		}
	}
}
