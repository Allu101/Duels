package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;

import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Lobby {
	
	public String LINE = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "                                                                            ";
	
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private ArrayList<DuelsGame> games = new ArrayList<DuelsGame>();
	private ArrayList<Challenge> challenges = new ArrayList<Challenge>();
	
	private Location spawnLocation;
	
	private MenuHandler menuHandler;
	
	private ArrayList<Player> rankedQueue = new ArrayList<>();
	

	public Lobby(FileConfiguration config, MenuHandler menuHandler) {
		spawnLocation = new Location(Bukkit.getWorld(config.getString("lobbyworldname")), config.getDouble("spawnloc.x"), config.getDouble("spawnloc.y"), config.getDouble("spawnloc.z"), 
				config.getInt("spawnloc.yaw"), config.getInt("spawnloc.pitch"));
		this.menuHandler = menuHandler;
	}
	
	public void addGame(DuelsGame game) {
		games.add(game);
	}
	
	public DuelsGame getFreeGame(Gamemode gamemode) {
		for(DuelsGame game : games) {
			if(game.getGamemode().equals(gamemode) && game.isFree()) {
				return game;
			}
		}
		
		System.out.println("gameCount: " + games.size());
		
		return null;
	}
	
	public DuelsPlayer getDuelsPlayer(Player p) {
		for(DuelsPlayer dp : players) {
			if(dp.is(p.getUniqueId().toString())) {
				return dp;
			}
		}
		return null;
	}
	
	public boolean isLobbyWorld(Entity e) {
		return e.getWorld().getName().equals(Duels.getLobbyWorldName());
	}
	
	public void onPlayerJoin(Player p) {
		DuelsPlayer dp = new DuelsPlayer(p, new PlayerSidebarHandler());
		Duels.plugin.dbHandler.loadStatsSQL(dp);
		sendPlayerToLobby(dp);
	}
	
	public void onPlayerLeave(DuelsPlayer dp) {
		players.remove(dp);
	}
	
	public void createNewChallenge(DuelsPlayer challenger, DuelsPlayer challenged, Kit kit) {
		
		Player challengerPlayer = challenger.getPlayer();
		Player challengedPlayer = challenged.getPlayer();
		
		Challenge existingChallenge = this.getChallenge(challengerPlayer, challengedPlayer);
		
		if (existingChallenge != null) {
			long timePassed = System.currentTimeMillis() - existingChallenge.getChallengeSendTime();
			
			if (timePassed < 20000) { // 20 seconds
				challengerPlayer.sendMessage(ChatColor.RED + "Odota hetki ennen uuden haasteen lähettämistä!");
				return;
			}
		}
		
		// Let everyone challenge others only once. (No multiple challenges from one player to another at the same time.)
		// New challenge will replace the old.
		this.challenges.remove(existingChallenge);

		Challenge challenge = new Challenge(challenger, challenged, kit);
		challenges.add(challenge);
		
		challengerPlayer.sendMessage(ChatColor.AQUA + "Haastoit pelaajan " + challengedPlayer.getName() + " " + kit.getName() +" 1v1 duelsiin");
		sendChallengeMessage(challenge);
	}
	
	/**
	 * Removes all challenges from the challenges list which contain any of the given players.
	 * @param players
	 */
	public void removeChallengesWithPlayers(DuelsPlayer... players) {
		for (DuelsPlayer dpp : players) {
			this.challenges.removeIf(c -> c.hasPlayer(dpp));
		}
	}
	
	/**
	 * If a challenge exists for given players, returns it.
	 * Otherwise returns null.
	 * @param challenger
	 * @param challenged
	 * @return
	 */
	public Challenge getChallenge(Player challenger, Player challenged) {
		for (Challenge challenge : this.challenges) {
			if (challenge.getChallenger().getPlayer().equals(challenger) && challenge.getChallenged().getPlayer().equals(challenged)) {
				return challenge;
			}
		}
		return null;
	}
	
	/**
	 * Returns challenge by challengeID. Otherwise returns null.
	 * @param challengeID
	 * @return
	 */
	public Challenge getChallenge(long challengeID) {
		for (Challenge challenge : this.challenges) {
			if (challenge.getChallengeID() == challengeID) {
				return challenge;
			}
		}
		return null;
	}
	
	private void sendChallengeMessage(Challenge challenge) {
		
		Player challenged = challenge.getChallenged().getPlayer();
		
		challenged.sendMessage(ChatColor.GREEN + challenge.getChallenger().getPlayer().getName() +
				" haastoi sinut " + challenge.getKit().getName() + " duelsiin.");
		
		TextComponent msg1 = new TextComponent("Hyväksy haaste ");
		msg1.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		
		TextComponent msg2 = new TextComponent("klikkaamalla tästä!");
		msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/duel accept " + challenge.getChallengeID()));
		msg2.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
		msg2.setUnderlined(true);
		
		msg1.addExtra(msg2);
		
		challenged.spigot().sendMessage(msg1);
	}
	
	/**
	 * This method should be used for returning players back to lobby!
	 * @param dp
	 */
	public void sendPlayerToLobby(DuelsPlayer dp) {
		
		System.out.println("Sending " + dp.getPlayer().getName() + " to lobby...");
		
		dp.setGameWhereJoined(null);
		
		dp.getPlayer().setScoreboard(dp.getSidebarHandler().getLobbyBoard());
		dp.getSidebarHandler().updateLobbySidebarWinsAndWinStreaks(
				dp.getWins(), dp.getCurrentWinStreak(), dp.getBestWinStreak(), dp.getPlayedGames(), dp.getEloScore());
		
		teleportToSpawn(dp.getPlayer());
		
		boolean playerFound = false;
		
		for (DuelsPlayer dp2 : players) {
			if (dp2.is(dp.getPlayer().getUniqueId().toString())) {
				playerFound = true;
				dp2.setWins(dp.getWins());
				dp2.setBestWinStreak(dp.getBestWinStreak());
				dp2.setCurrentWinStreak(dp.getCurrentWinStreak());
				dp2.setEloScore(dp.getEloScore());
				dp2.setPlayedGames(dp.getPlayedGames());
			}
		}
		
		if (!playerFound)
			players.add(dp);
	}
	
	public void teleportToSpawn(Player p) {
		p.teleport(spawnLocation, TeleportCause.PLUGIN);
		p.setGameMode(GameMode.ADVENTURE);
		p.setHealth(20);
		clearPlayerInventoryAndEquipment(p);
		menuHandler.setLobbyItems(p);
	}
	
	
	public void clearPlayerInventoryAndEquipment(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
	}
	
	public void clearPotionEffect(Player p) {
		for(PotionEffect effect : p.getActivePotionEffects()) {
		    p.removePotionEffect(effect.getType());
		}
	}
	
	public void addPlayerToRankedQueue(Player p) {
		
		DuelsPlayer dpp = getDuelsPlayer(p);
		
		if (this.rankedQueue.size() > 0) {
			
			if (this.rankedQueue.contains(p)) {
				p.sendMessage("§cOlet jo jonossa!");
				return;
			} else {
				Player opponent = rankedQueue.get(0);
				
				if (opponent.isOnline()) {
					DuelsPlayer dpOpponent = getDuelsPlayer(opponent);
					
					DuelsGame game = getFreeGame(Gamemode.DUELS_1V1);
					if(game != null) {
						List<DuelsPlayer> duelsPlayers = new ArrayList<DuelsPlayer>();
						duelsPlayers.add(dpp);
						duelsPlayers.add(dpOpponent);
						game.startGame(duelsPlayers, Duels.plugin.getKitByName("op duel"), DuelsGame.GameType.RANKED);
						this.rankedQueue.remove(opponent);
						return;
					} else {
						p.sendMessage(ChatColor.RED + "Vapaita pelejä ei tällä hetkellä ole.");
						return;
					}
					
				} else {
					this.rankedQueue.remove(opponent);
				}
			}
		}
		
		this.rankedQueue.add(p);
		p.sendMessage("§aOdotat nyt vastustajaa peliin!");
		dpp.setChallengedPlayer(null);
	}
	
	public void removePlayerFromRankedQueue(Player p) {
		this.rankedQueue.remove(p);
	}
}
