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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.allu.duels.utils.ChallengeCreatedEvent;
import com.allu.duels.utils.Gamemode;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Events implements Listener, CommandExecutor {
	
	private List<ChallengeCreatedEvent> challenges = new ArrayList<ChallengeCreatedEvent>();
	
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
			
			if(args.length == 1) {
				Player opponent = Bukkit.getPlayerExact(args[0].toString());
				if(opponent == null) {
					p.sendMessage(ChatColor.RED + "Tämän nimistä pelaajaa ei löydy.");
					return true;
				}
				DuelsPlayer opponentDp = lobby.getDuelsPlayer(opponent);
				String opponentName = opponent.getName();
				if(opponentDp.getGameWhereJoined() != null) {
					p.sendMessage(ChatColor.RED + "Et voi haastaa pelaajaa " + opponentName + ", koska hänellä on peli menossa.");
				}
				if(args[0].equalsIgnoreCase(opponentName)) {
					if(p.getName().equals(opponentName)) {
						p.sendMessage(ChatColor.RED + "Et voi haastaa itseasi duelsiin.");
						return true;
					}
					p.openInventory(menuHandler.createKitMenu());
					dp.setChallengedPlayer(opponentDp);
				}
				return true;
			}
			
			if(args.length == 2) {
				if(args[0].equalsIgnoreCase("accept")) {
					Player player = Bukkit.getPlayerExact(args[1]);
					if(player == null) {
						p.sendMessage(ChatColor.RED + "Tämän nimistä pelaajaa ei löydy.");
						return true;
					}
					if(p.equals(player)) {
						p.sendMessage(ChatColor.RED + "Et voi hyväksyä omaa haastettasi.");
						return true;
					}
					for(ChallengeCreatedEvent e : new ArrayList<ChallengeCreatedEvent>(challenges)) {
						List<DuelsPlayer> challengePlayers = e.getDuelsPlayers();
						if(e.getChallengerDp().getPlayer().equals(player) && challengePlayers.contains(dp)) {
							DuelsGame game = lobby.getFreeGame(Gamemode.DUELS_1V1);
							if(game != null) {
								game.startGame(challengePlayers, e.getKit());
								challenges.remove(e);
								removeChallenges(challengePlayers);
								return true;
							} else {
								p.sendMessage(ChatColor.RED + "Vapaita pelejä ei tällä hetkellä ole.");
							}
						}
					}
					p.sendMessage(ChatColor.GRAY + "Kukaan ei ole lähettänyt sinulle duels pyyntöä.");
					return true;
				}
			}
			p.sendMessage(lobby.LINE);
			p.sendMessage(ChatColor.AQUA + "/duel <pelaajan_nimi>");
			p.sendMessage(ChatColor.AQUA + "/duel accept <pelaajan_nimi>.");
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

				
				if(targetDp.getGameWhereJoined() == null) {
					p.sendMessage(ChatColor.RED + "Kyseinen pelaaja on tällä hetkellä lobbyssa");
				}
				else {
					if(dp.getGameWhereJoined() != null) {
						dp.getGameWhereJoined().leaveGame(dp);
					}
					
					lobby.clearPlayerInventoryAndEquipment(p);
					lobby.clearPotionEffect(p);
					
					p.setGameMode(GameMode.SPECTATOR);
					p.teleport(targetPlayer);
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
			
			lobby.teleportToSpawn(dp.getPlayer());

			return true;
		}
		
		
		return false;
	}
	
	private void removeChallenges(List<DuelsPlayer> duelsPlayers) {
		for(DuelsPlayer dp : duelsPlayers) {
			for(ChallengeCreatedEvent e : new ArrayList<ChallengeCreatedEvent>(challenges)) {
				if(e.getDuelsPlayers().contains(dp)) {
					challenges.remove(e);
				}
			}
		}
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
	public void onChallengeCreated(ChallengeCreatedEvent e) {
		challenges.add(e);
		Player challenger = e.getChallengerDp().getPlayer();
		for(DuelsPlayer opponentDp : e.getDuelsPlayers()) {
			if(!opponentDp.getPlayer().equals(challenger)) {
				Player opponent = opponentDp.getPlayer();
				challenger.sendMessage(ChatColor.AQUA + "Haastoit pelaajan " + opponent.getName() + " " + e.getKit().getName() +" 1v1 duelsiin");
				opponent.sendMessage(ChatColor.GREEN + challenger.getName() + " haastoi sinut " + e.getKit().getName() + " duelsiin.");
				
				String commandString = "/duel accept " + challenger.getName();
				
				TextComponent msg1 = new TextComponent("Hyväksy haaste ");
				msg1.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				
				TextComponent msg2 = new TextComponent("klikkaamalla");
				msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandString));
				msg2.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
				msg2.setUnderlined(true);
				
				TextComponent msg3 = new TextComponent(" tai komennolla ");
				msg3.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				
				TextComponent msg4 = new TextComponent(commandString + ".");
				msg4.setColor(net.md_5.bungee.api.ChatColor.BLUE);
				
				msg1.addExtra(msg2);
				msg1.addExtra(msg3);
				msg1.addExtra(msg4);
				
				
				opponent.spigot().sendMessage(msg1);
			}	
		}
	}
	
	@EventHandler
	public void creatureSpawn(CreatureSpawnEvent e) {
		if(!e.getEntityType().equals(EntityType.ARMOR_STAND)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}
	
	
	@EventHandler
	public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (!e.getPlayer().isOp())
			e.setCancelled(true);
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
		e.setCancelled(true);
		menuHandler.inventoryClickHandler(lobby.getDuelsPlayer(p), e.getCurrentItem());
	}
	
	@EventHandler
    public void onEntityDamage(EntityDamageEvent e){
		
        if (lobby.isLobbyWorld(e.getEntity()))
        	e.setCancelled(true);
        
        if(!(e.getEntity() instanceof Player)) {
			return;
		}

		Player damaged = (Player) e.getEntity();	
		DuelsGame gameWhereJoined = lobby.getDuelsPlayer(damaged).getGameWhereJoined();
		
		if(gameWhereJoined == null) {
			return;
		}
		
		if(!gameWhereJoined.isGameOn()) {
			e.setCancelled(true);
			return;
		}
		
		// Checks if player is under arena. The player is announced as being dead.
		if (gameWhereJoined.isUnderArena(damaged.getLocation()) || !gameWhereJoined.isWithinArena(damaged.getLocation())) {
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
		if(!(e.getEntity() instanceof Player)) {
			return;
		}

		Player damaged = (Player) e.getEntity();
		if(lobby.isLobbyWorld(damaged)) {
			e.setCancelled(true);
			
			if(!(e.getDamager() instanceof Player)) {
				return;
			}
			Player damager = (Player) e.getDamager();
			
			if (damager.getItemInHand().equals(menuHandler.getChallengeItem())) {
				damager.performCommand("duel " + damaged.getName());
			}
			
			return;
		}
	}
	
	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent e) {
		if(e.getPlayer() != null) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		menuHandler.onPlayerInteract(e.getPlayer().getInventory().getItemInHand(), e.getAction());
	}
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
		lobby.onPlayerJoin(e.getPlayer());
	}
	
	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(e.getPlayer() == null)
			return;
		DuelsPlayer dp = lobby.getDuelsPlayer(p);
		if(dp == null) {
			return;
		}
		if(dp.getGameWhereJoined() != null) {
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
			e.setCancelled(true);
			p.teleport(e.getTo());
			DuelsGame gameWhereJoined = lobby.getDuelsPlayer(p).getGameWhereJoined();
			
			if (gameWhereJoined != null && !gameWhereJoined.isWithinArena(e.getTo())) {
				e.setCancelled(true);
				p.sendMessage(ChatColor.RED + "Areenalta ei saa poistua!");
			}
			return;
		}
	}
	
	@EventHandler
	public void onPlayerItemDamage(PlayerItemDamageEvent e) {
		e.getItem().setDurability((short)0);
		e.setCancelled(true);
	}
}
