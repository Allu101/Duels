package com.allu.duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.allu.duels.DuelsPlayer.PlayerState;


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
		if(cmd.getName().equalsIgnoreCase("duel") && args.length == 1) {
			if(args[0].equalsIgnoreCase("accept")) {
				if(dp.getPlayerState() == PlayerState.RECEIVED_REQUEST) {
					dp.setPlayerState(PlayerState.NORMAL);
//					DuelsGame game = lobby.getFreeGame(games);
					dp.getRequesterPlayer().getGameWhereJoined().joinGame(dp);
				} else {
					p.sendMessage(ChatColor.RED + "Kukaan ei ole lähettänyt sinulle duels pyyntöä.");
				}
				return true;
			}
			Player opponentP = Bukkit.getPlayerExact(args[0].toString());
			if(opponentP == null) {
				p.sendMessage(ChatColor.RED + "Tämän nimistä pelaajaa ei löydy.");
				return true;
			}
			
			if(args[0].equalsIgnoreCase(opponentP.getName())) {
				if(p.getName().equals(opponentP.getName())) {
					p.sendMessage(ChatColor.RED + "Et voi haastaa itseasi duelsiin.");
					return true;
				}
				p.openInventory(menuHandler.createKitMenu(p));
				
				p.sendMessage(ChatColor.AQUA + "Haastoit pelaajan" + opponentP.getName() + "1v1 duelsiin");
				opponentP.sendMessage(ChatColor.GREEN + p.getName() + " haastoi sinut Duelsiin.");
				opponentP.sendMessage(ChatColor.GREEN + "Hyväksy haaste komennolla" + ChatColor.BLUE + "/duels accept.");
				DuelsPlayer opponentDuelsP = lobby.getDuelsPlayer(opponentP);
				opponentDuelsP.setPlayerState(PlayerState.RECEIVED_REQUEST);
				opponentDuelsP.setRequesterPlayer(dp);
			}
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(lobby.isLobbyWorld(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(lobby.isLobbyWorld(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(lobby.isLobbyWorld((Player) e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		ItemStack is = e.getCurrentItem();
		if(is == null || is.getType().equals(Material.AIR)) {
			return;
		}
		menuHandler.inventoryClickHandler((Player)e.getWhoClicked(), e);
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
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
	
	private Player getDamager(EntityDamageEvent e) {
		if(e instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) e;
			Entity damager = damageByEntityEvent.getDamager();
			if(damager instanceof Arrow) {
				Arrow arrow = (Arrow) damageByEntityEvent.getDamager();
				if(arrow.getShooter() instanceof Player) {
					return (Player) arrow.getShooter();
				}
			} else if(damager instanceof FishHook)  {
				FishHook fh = (FishHook) damageByEntityEvent.getDamager();
				return (Player) fh.getShooter();
			} else if(damager instanceof Player) {
				return (Player) damageByEntityEvent.getDamager();
			}
		}
		return null;
	}

}
