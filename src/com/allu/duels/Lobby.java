package com.allu.duels;

import com.allu.duels.DuelsGame.GameType;
import com.allu.duels.utils.Kit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lobby {
	
	public String LINE = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "                                                                            ";
	
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private ArrayList<DuelsGame> games = new ArrayList<DuelsGame>();
	private ArrayList<Challenge> challenges = new ArrayList<Challenge>();
	
	private Location spawnLocation;
	
	private MenuHandler menuHandler;
	
	private List<Player> rankedQueue = new ArrayList<>();
	
	private List<String> rankedKitNames;
	

	public Lobby(FileConfiguration config, MenuHandler menuHandler) {
		
		spawnLocation = new Location(Bukkit.getWorld(config.getString("lobbyworldname")), config.getDouble("spawnloc.x"), config.getDouble("spawnloc.y"), config.getDouble("spawnloc.z"), 
				config.getInt("spawnloc.yaw"), config.getInt("spawnloc.pitch"));
		this.menuHandler = menuHandler;
		
		rankedKitNames = config.getStringList("ranked-duels");
		
		if (rankedKitNames == null) {
			rankedKitNames = new ArrayList<>();
			rankedKitNames.add("classic duel");
			rankedKitNames.add("op duel");
			rankedKitNames.add("jousi duel");
		}
	}
	
	public void addGame(DuelsGame game) {
		games.add(game);
	}
	
	public DuelsGame getFreeGame(String arenaType) {
		for(DuelsGame game : games) {
			if(game.getArenaType().equals(arenaType) && game.isFree()) {
				return game;
			}
		}
		
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
		final DuelsPlayer dp = new DuelsPlayer(p, new PlayerSidebarHandler());
		Bukkit.getScheduler().runTaskAsynchronously(Duels.plugin, () -> {
			Duels.plugin.dbHandler.loadStatsSQL(dp);
			refreshLobbyStatsDisplay(dp);
		});
		players.add(dp);
		sendPlayerToLobby(dp);
	}
	
	public void onPlayerLeave(DuelsPlayer dp) {
		this.removePlayerFromRankedQueue(dp.getPlayer());
		this.removeChallengesWithPlayers(dp);
		players.remove(dp);
	}
	
	public void createNewChallenge(DuelsPlayer challenger, List<DuelsPlayer> challengedPlayers, Kit kit) {
		
		Player challengerPlayer = challenger.getPlayer();
		//Player challengedPlayer = challenged.getPlayer();
		String challengedNames = "";
		
		if (challenger.getGameWhereJoined() != null) {
			challengerPlayer.sendMessage(ChatColor.RED + "Et voi luoda haastetta, sillä olet itse pelissä!");
		}
		for(DuelsPlayer challenged : challengedPlayers) {
			if (challenged.getGameWhereJoined() != null) {
				challengerPlayer.sendMessage(ChatColor.RED + "Et voi luoda haastetta, sillä haastamasi pelaaja on jo pelissä!");
			}
			challengedNames = String.join(",", challengedNames, challenged.getPlayer().getName());
		}
		
		Challenge existingChallenge = this.getChallenge(challengerPlayer, challengedPlayers);
		
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

		Challenge challenge = new Challenge(challenger, challengedPlayers, kit);
		challenges.add(challenge);
		
		challengerPlayer.sendMessage(ChatColor.AQUA + "Haastoit pelaajan " + challengedNames + " " + kit.getName() +" 1v1 duelsiin");
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
	 * @param challengedPlayers
	 * @return
	 */
	public Challenge getChallenge(Player challenger, List<DuelsPlayer> challengedPlayers) {
		for (Challenge challenge : this.challenges) {
			if (challenge.getChallenger().getPlayer().equals(challenger) && challenge.getChallengedPlayers().equals(challengedPlayers)) {
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
		for (DuelsPlayer dp : challenge.getChallengedPlayers()) {

			TextComponent msg = new TextComponent(challenge.getChallenger().getPlayer().getName() +
					" haastoi sinut " + challenge.getKit().getName() + " duelsiin.");
			msg.setColor(net.md_5.bungee.api.ChatColor.GREEN);

			TextComponent acceptMsg = new TextComponent("\nHyväksy haaste ");
			acceptMsg.setColor(net.md_5.bungee.api.ChatColor.GREEN);

			TextComponent commandMsg = new TextComponent("klikkaamalla tästä!");
			commandMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
					"/duel accept " + challenge.getChallengeID()));
			commandMsg.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
			commandMsg.setUnderlined(true);

			msg.addExtra(acceptMsg);
			msg.addExtra(commandMsg);

			dp.getPlayer().spigot().sendMessage(msg);
		}
	}
	
	/**
	 * 
	 * @param duelsPlayers
	 * @param kit
	 * @param gameType
	 * @param activator
	 */
	public void startNewDuelsMatch(List<DuelsPlayer> duelsPlayers, Kit kit, GameType gameType, Player activator) {
		
		DuelsGame game = getFreeGame(kit.getArenaType());
		
		if(game == null) {
			activator.sendMessage(ChatColor.RED + "Vapaita pelejä ei tällä hetkellä ole.");
			return;
		}
		
		boolean alreadyInGame = false;
		for (DuelsPlayer dpp : duelsPlayers) {
			removePlayerFromRankedQueue(dpp.getPlayer());
			if (dpp.getGameWhereJoined() != null) {
				alreadyInGame = true;
			}
		}
		
		if (alreadyInGame) {
			activator.sendMessage(ChatColor.RED + "Tapahtui virhe, peliä ei voida aloittaa!");
			return;
		}

		Collections.shuffle(duelsPlayers);
		int halfSize = duelsPlayers.size() / 2;
		game.startGame(new ArrayList<>(duelsPlayers.subList(0, halfSize)),
				new ArrayList<>(duelsPlayers.subList(halfSize, duelsPlayers.size())), kit, gameType);
		removeChallengesWithPlayers(duelsPlayers.toArray(new DuelsPlayer[2]));
		
		printQueueToConsole();
	}
	
	/**
	 * This method should be used for returning players back to lobby!
	 * @param dp
	 */
	public void sendPlayerToLobby(DuelsPlayer dp) {
		dp.setGameWhereJoined(null);
		Player player = dp.getPlayer();
		player.setScoreboard(dp.getSidebarHandler().getLobbyBoard());
		
		teleportToSpawn(player);
		
		refreshLobbyStatsDisplay(dp);
		
		Duels.plugin.eloRanking.updatePlayerOwnStatsHologram(player);
		Duels.plugin.winsRanking.updatePlayerOwnStatsHologram(player);
	}
	
	private void refreshLobbyStatsDisplay(DuelsPlayer dp) {
		dp.getSidebarHandler().updateLobbySidebarWinsAndWinStreaks(
				dp.getWins(), dp.getCurrentWinStreak(), dp.getBestWinStreak(), dp.getPlayedGames(), dp.getEloScore());
	}
	
	public void teleportToSpawn(Player p) {
		p.teleport(spawnLocation, TeleportCause.PLUGIN);
		p.setGameMode(GameMode.ADVENTURE);
		p.setHealth(20);
		clearPlayerInventoryAndEquipment(p.getInventory());
		menuHandler.setLobbyItems(p);
	}
	
	public void clearPlayerInventoryAndEquipment(PlayerInventory playerInventory) {
		playerInventory.clear();
		playerInventory.setHelmet(null);
		playerInventory.setChestplate(null);
		playerInventory.setLeggings(null);
		playerInventory.setBoots(null);
	}
	
	public void clearPotionEffect(Player p) {
		for(PotionEffect effect : p.getActivePotionEffects()) {
		    p.removePotionEffect(effect.getType());
		}
	}
	
	public void addPlayerToRankedQueue(Player p) {
		
		DuelsPlayer dpp = getDuelsPlayer(p);
		
		if (dpp.getGameWhereJoined() != null) {
			p.sendMessage(ChatColor.RED + "Et voi liittyä jonoon, sillä olet pelissä!");
		}
		
		if (this.rankedQueue.size() > 0) {
			
			if (this.rankedQueue.contains(p)) {
				p.sendMessage("§cOlet jo jonossa!");
				return;
			} else {
				Player opponent = rankedQueue.get(0);
				
				if (!opponent.isOnline()) {
					
					this.rankedQueue.remove(opponent);
					
				} else {
					
					DuelsPlayer dpOpponent = getDuelsPlayer(opponent);
					
					if (dpOpponent.getGameWhereJoined() == null) {
					
						p.sendMessage("Vastustaja läytyi: " + opponent.getDisplayName());
						
						List<DuelsPlayer> duelsPlayers = new ArrayList<DuelsPlayer>();
						duelsPlayers.add(dpp);
						duelsPlayers.add(dpOpponent);

						
						this.startNewDuelsMatch(
								duelsPlayers,
								Duels.plugin.getKitByName(this.rankedKitNames.get((int)(Math.random() * this.rankedKitNames.size()))),
								DuelsGame.GameType.RANKED,
								p);
						return;
					}
				}
			}
		}
		
		this.rankedQueue.add(p);
		p.sendMessage("§aOdotat nyt vastustajaa peliin!");
		menuHandler.addExitQueueItemToPlayer(p);
		dpp.setChallengedPlayers(null);
		
		printQueueToConsole();
	}
	
	private void printQueueToConsole() {
		String consoleMsg = "Jonossa ovat nyt: ";
		for (Player player : this.rankedQueue) {
			consoleMsg += player.getDisplayName() + ", ";
		}
		System.out.println(consoleMsg);
	}
	
	/**
	 * Removes the player from ranked queue if they are in one.
	 * Also removes the exit queue item from the players inventory.
	 * @param p
	 */
	public void removePlayerFromRankedQueue(Player p) {
		rankedQueue.removeIf(player -> player.getUniqueId().equals(p.getUniqueId()));
		menuHandler.removeExitQueueItemFromPlayer(p);
	}
}
