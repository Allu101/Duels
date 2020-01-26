package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

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
			System.out.println("[Duels] Komentoa ei voi käyttää consolesta");
			return true;
		}
		Player p = (Player)sender;
		if(cmd.getName().equalsIgnoreCase("duel")) {
			DuelsPlayer dp = lobby.getDuelsPlayer(p);
			if(args.length == 1) {
				Player opponent = Bukkit.getPlayerExact(args[0].toString());
				if(opponent == null) {
					p.sendMessage(ChatColor.RED + "Tämän nimistä pelaajaa ei löydy.");
					return true;
				}
				String opponentName = opponent.getName();
				if(args[0].equalsIgnoreCase(opponentName)) {
					if(p.getName().equals(opponentName)) {
						p.sendMessage(ChatColor.RED + "Et voi haastaa itseasi duelsiin.");
						return true;
					}
					p.openInventory(menuHandler.createKitMenu(p));
					dp.setChallengedPlayer(opponent);
					
				}
				return true;
			}
			
			if(args[0].equalsIgnoreCase("accept")) {
				if(args.length > 1) {
					Player player = Bukkit.getPlayerExact(args[1]);
					if(player != null) {
						for(ChallengeCreatedEvent e : challenges) {
							if(e.getPlayer().equals(player) && e.getOpponent().equals(p)) {
								DuelsGame game = lobby.getFreeGame(Gamemode.DUELS_1V1);
								if(game != null) {
									game.joinGame(lobby.getDuelsPlayer(player));
									game.joinGame(dp);
								}
							}
						}
					}
				} else {
					p.sendMessage(ChatColor.RED + "Hyväksy pyyntö /duels accept <pelaajan_nimi> tai klikkaamalla tästä.");
				}
				return true;
				
//				if(dp.getPlayerState() == PlayerState.RECEIVED_REQUEST) {
//					dp.setPlayerState(PlayerState.NORMAL);
					
//					dp.getRequesterPlayer().getGameWhereJoined().joinGame(dp);
//				} else {
//					p.sendMessage(ChatColor.RED + "Kukaan ei ole lähettänyt sinulle duels pyyntöä.");
//				}
//				return true;
				
			}
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onChallengeCreated(ChallengeCreatedEvent e) {
		challenges.add(e);
		Player p = e.getPlayer();
		Player opponent = e.getOpponent();
		p.sendMessage(ChatColor.AQUA + "Haastoit pelaajan" + opponent.getName() + "1v1 duelsiin");
		opponent.sendMessage(ChatColor.GREEN + p.getName() + " haastoi sinut Duelsiin.");
		opponent.sendMessage(ChatColor.GREEN + "Hyväksy haaste komennolla" + ChatColor.BLUE + "/duels accept" + p.getName() + ".");
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
