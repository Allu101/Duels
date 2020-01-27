package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.allu.duels.utils.ChallengeCreatedEvent;
import com.allu.duels.utils.Gamemode;


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
			System.out.println("[Duels] Komentoa ei voi k�ytt�� consolesta");
			return true;
		}
		Player p = (Player)sender;
		if(cmd.getName().equalsIgnoreCase("duel")) {
			DuelsPlayer dp = lobby.getDuelsPlayer(p);
			if(args.length == 1) {
				Player opponent = Bukkit.getPlayerExact(args[0].toString());
				if(opponent == null) {
					p.sendMessage(ChatColor.RED + "T�m�n nimist� pelaajaa ei l�ydy.");
					return true;
				}
				String opponentName = opponent.getName();
				if(args[0].equalsIgnoreCase(opponentName)) {
					if(p.getName().equals(opponentName)) {
						p.sendMessage(ChatColor.RED + "Et voi haastaa itseasi duelsiin.");
						return true;
					}
					p.openInventory(menuHandler.createKitMenu());
					dp.setChallengedPlayer(lobby.getDuelsPlayer(opponent));
				}
				return true;
			}
			
			if(args[0].equalsIgnoreCase("accept")) {
				if(args.length > 1) {
					Player player = Bukkit.getPlayerExact(args[1]);
					if(player == null) {
						p.sendMessage("T�m�n nimist� pelaajaa ei l�ydy.");
						return true;
					}
					for(ChallengeCreatedEvent e : new ArrayList<ChallengeCreatedEvent>(challenges)) {
						if(e.getDuelsPlayer().getPlayer().equals(player) && e.getDuelsPlayers().contains(dp)) {
							DuelsGame game = lobby.getFreeGame(Gamemode.DUELS_1V1);
							if(game != null) {
								game.startGame(e.getDuelsPlayers(), e.getKit());
								challenges.remove(e);
								return true;
							}
						}
					}
					p.sendMessage(ChatColor.GRAY + "Kukaan ei ole l�hett�nyt sinulle duels pyynt��.");
				} else {
					p.sendMessage(ChatColor.RED + "Hyv�ksy pyynt� /duels accept <pelaajan_nimi> tai klikkaamalla t�st�.");
				}
			}
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
	public void onChallengeCreated(ChallengeCreatedEvent e) {
		challenges.add(e);
		DuelsPlayer duelsPlayer = e.getDuelsPlayer();
		for(DuelsPlayer dp : e.getDuelsPlayers()) {
			Player p = dp.getPlayer();
			duelsPlayer.getPlayer().sendMessage(ChatColor.AQUA + "Haastoit pelaajan" + p.getName() + "1v1 duelsiin");
			p.sendMessage(ChatColor.GREEN + p.getName() + " haastoi sinut Duelsiin.");
			p.sendMessage(ChatColor.GREEN + "Hyv�ksy haaste komennolla" + ChatColor.BLUE + "/duels accept" + p.getName() + ".");
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
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
	public void onPlayerDamage(EntityDamageByEntityEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		Player damaged = (Player) e.getEntity();
		if(lobby.isLobbyWorld(damaged)) {
			e.setCancelled(true);
			return;
		}
		
		Player damager = getDamager(e);		
		DuelsGame gameWhereJoined = lobby.getDuelsPlayer(damaged).getGameWhereJoined();
		if(damager == null || gameWhereJoined == null) {
			return;
		}
		
		if(!gameWhereJoined.isGameOn()) {
			e.setCancelled(true);
			return;
		}
		
		if(damaged.getHealth() - e.getDamage() > 0) {
			return;
		}
		// Player would be dead at this point, so cancel and do other stuff --->
		e.setCancelled(true);
		gameWhereJoined.gameEnd(damager);
	}
	
	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent e) {
		if(e.getPlayer() != null) {
			e.setCancelled(true);
		}
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
		lobby.onPlayerLeave(dp);
	}
	
	private Player getDamager(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if(damager instanceof Arrow) {
			Arrow arrow = (Arrow) damager;
			if(arrow.getShooter() instanceof Player) {
				return (Player) arrow.getShooter();
			}
		} else if(damager instanceof FishHook)  {
			FishHook fh = (FishHook) damager;
			return (Player) fh.getShooter();
		} else if(damager instanceof Player) {
			return (Player) damager;
		}
		return null;
	}

}
