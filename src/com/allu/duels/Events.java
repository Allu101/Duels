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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
					p.openInventory(menuHandler.createKitMenu());
					dp.setChallengedPlayer(lobby.getDuelsPlayer(opponent));
				}
				return true;
			}
			
			if(args.length == 2) {
				if(args[0].equalsIgnoreCase("accept")) {
					Player player = Bukkit.getPlayerExact(args[1]);
					if(player == null) {
						p.sendMessage("Tämän nimistä pelaajaa ei löydy.");
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
					p.sendMessage(ChatColor.GRAY + "Kukaan ei ole lähettänyt sinulle duels pyyntöä.");
					return true;
				}
			}
			p.sendMessage(lobby.LINE);
			p.sendMessage(ChatColor.AQUA + "/duel <pelaajan_nimi>");
			p.sendMessage(ChatColor.AQUA + "/duel accept <pelaajan_nimi> tai klikkaamalla tästä.");
			p.sendMessage(lobby.LINE);
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
		Player challenger = e.getDuelsPlayer().getPlayer();
		for(DuelsPlayer opponentDp : e.getDuelsPlayers()) {
			if(!opponentDp.getPlayer().equals(challenger)) {
				Player opponent = opponentDp.getPlayer();
				challenger.sendMessage(ChatColor.AQUA + "Haastoit pelaajan " + opponent.getName() + " 1v1 duelsiin");
				opponent.sendMessage(ChatColor.GREEN + challenger.getName() + " haastoi sinut Duelsiin.");
				opponent.sendMessage(ChatColor.GREEN + "Hyväksy haaste komennolla " + ChatColor.BLUE + "/duel accept " + challenger.getName() + ".");
			}	
		}
	}
	
	@EventHandler
	public void creatureSpawn(CreatureSpawnEvent e) {
		e.setCancelled(true);
		if(!e.getEntityType().equals(EntityType.ARMOR_STAND)) {
			e.setCancelled(true);
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
