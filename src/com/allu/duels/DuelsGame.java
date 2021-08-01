package com.allu.duels;

import com.allu.duels.utils.FileHandler;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.CountDownTimer;
import com.allu.minigameapi.CountDownTimerListener;
import com.allu.minigameapi.MessageHandler;
import com.allu.minigameapi.player.TitleHandler;
import com.allu.minigameapi.ranking.SimpleRanking;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DuelsGame implements CountDownTimerListener {

	public enum GameType {
		RANKED, FRIEND_CHALLENGE
	}
	
	private enum GameState {
		FREE, STARTING, PLAYING, GAME_FINISH
	}
	
	private GameState currentGameState = GameState.FREE;

	private GameType gameType;

	private List<DuelsPlayer> players = new ArrayList<>();
	private List<DuelsPlayer> team1 = new ArrayList<>();
	private List<DuelsPlayer> team2 = new ArrayList<>();

	private Lobby lobby;
	private MessageHandler messages;
	private CountDownTimer timer;
	
	private SimpleRanking winsRanking;
	private SimpleRanking eloRanking;
	
	private Kit kit;
	
	private Arena arena;

	private TitleHandler titleHandler = new TitleHandler();
	
	private long gameStartTimeMillis;

	
	public DuelsGame(Lobby lobby, Arena arena, SimpleRanking winsRanking, SimpleRanking eloRanking) {
		this.lobby = lobby;
		this.arena = arena;
		this.winsRanking = winsRanking;
		this.eloRanking = eloRanking;
		
		this.messages = new MessageHandler();
		this.timer = new CountDownTimer(this);
	}

	@Override
	public void onCountDownChange(int time) {

	}

	@Override
	public void onCountDownFinish() {
		Bukkit.getScheduler().runTask(Duels.plugin, () -> {
			
			if (currentGameState == GameState.STARTING) {
				
				currentGameState = GameState.PLAYING;
				for (DuelsPlayer dp : players) {
					Player p = dp.getPlayer();
					dp.gameDamageDone = 0;
					p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
					p.setHealth(p.getMaxHealth());
					p.sendMessage(ChatColor.GREEN + "Duels alkaa!");
					titleHandler.sendTitle(p, "§9§lDuels alkaa!");
				}
				gameStartTimeMillis = System.currentTimeMillis();
				checkForDrawGame();
				
			} else if (currentGameState == GameState.GAME_FINISH) {
				
				for (DuelsPlayer dp : players) {
					lobby.sendPlayerToLobby(dp);
				}
				
				timer.clearPlayers();
				players.clear();
				team1.clear();
				team2.clear();
				currentGameState = GameState.FREE;
			}
		});
	}
	
	private void checkForDrawGame() {
		if (currentGameState != GameState.PLAYING)
			return;
		new BukkitRunnable() {
			@Override
			public void run() {
				long timeElapsed = System.currentTimeMillis() - gameStartTimeMillis;
				if (timeElapsed >= Duels.matchMillisecondsUntilDraw) {
					// Draw game
					gameEnd(new ArrayList<>());
					cancel();
				}
			}
		}.runTaskTimer(Duels.plugin, 1000, 100);
	}
	
	public void onPlayerDie(Player deadPlayer) {	
		deadPlayer.setGameMode(GameMode.SPECTATOR);
		deadPlayer.getWorld().strikeLightningEffect(deadPlayer.getLocation());
		lobby.clearPlayerInventoryAndEquipment(deadPlayer.getInventory());

		gameEnd(getOpponentPlayers(lobby.getDuelsPlayer(deadPlayer)));
	}

	public void gameEnd(List<DuelsPlayer> winnerTeam) {
		currentGameState = GameState.GAME_FINISH;

		String winnersMsg = "";
		if (winnerTeam.size() != 0) {
			for (DuelsPlayer winner : winnerTeam) {
				winnersMsg = String.join(" ", winnersMsg, winner.getPlayer().getName());
			}
			winnersMsg = winnersMsg.substring(1).replace(" ", ", ");
		}
		winnersMsg = winnerTeam.size() == 1 ? "Voittaja: " + ChatColor.GOLD + winnersMsg : "Voittajat: " + ChatColor.GOLD + winnersMsg;
		String winMessage = winnerTeam.size() == 0 ? ChatColor.GOLD + "Tasapeli" : ChatColor.GREEN + winnersMsg;
		for (DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			lobby.clearPotionEffect(p);
			for (Location loc : getPlacedBlocks()) {
				loc.getBlock().setType(Material.AIR);
			}

			Scoreboard scoreboard = p.getScoreboard();
			getOwnTeamPlayers(dp).forEach(teamMate -> scoreboard.getTeam("own").removeEntry(teamMate.getPlayer().getName()));
			getOpponentPlayers(dp).forEach(opponent -> scoreboard.getTeam("opponent").removeEntry(opponent.getPlayer().getName()));
			
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(winMessage));
			p.sendMessage("");

			List<DuelsPlayer> opponent = getOpponentPlayers(dp);
			
			if (winnerTeam.size() == 0) {
				titleHandler.sendTitle(p, "§e§lTASAPELI");

			} else if (winnerTeam.contains(dp)) {
				dp.addWin();
				titleHandler.sendTitle(p, "§a§lVOITTO");
			} else {
				dp.addLose();
				titleHandler.sendTitle(p, "§c§lTAPPIO");
				p.sendMessage("§7Vastustajan HP: §6" + new DecimalFormat("00.#").format(getOpponentPlayers(winnerTeam.get(0)).get(0).getPlayer().getHealth()));
			}
			
			p.sendMessage("§7Vahinkoa tehty: §6" + Math.round(dp.gameDamageDone));
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			
			if (gameType.equals(GameType.FRIEND_CHALLENGE)) {
				p.sendMessage(ChatColor.GRAY + "Kaveripelit eivät vaikuta ranking-pisteisiin");
			}
			else {
				if (dp != null && !opponent.isEmpty()) {
					
					double result = 0;
					if (winnerTeam.isEmpty()) {
						result = 0.5;
					}
					else if (winnerTeam.contains(dp))
						result = 1;
					
					double expectedScore = getExpectedScore(dp.getEloScore(), getOpponentPlayers(winnerTeam.get(0)).get(0).getEloScore());
					double eloChange = (result - expectedScore) * 32 + 0.2; // + 0.2 for little total score increase over time.
					int finalEloChange = (int)Math.round(eloChange);
					
					if (dp.getEloScore() + finalEloChange < 0) {
						finalEloChange = -dp.getEloScore();
					}
					dp.setEloScore(dp.getEloScore() + finalEloChange);
					
					if (finalEloChange == 0) {
						p.sendMessage(ChatColor.GRAY + "Ranking-pisteesi eivät muuttuneet pelin tuloksena.");
					}
					else if (finalEloChange < 0) {
						p.sendMessage(ChatColor.RED + "Menetit " + (-finalEloChange) + " ranking-pistettä");
					}
					else if (finalEloChange > 0) {
						p.sendMessage(ChatColor.GREEN + "Sait " + finalEloChange + " ranking-pistettä!");
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 0f);
					}
				}
			}

		}
		Bukkit.getScheduler().runTaskAsynchronously(Duels.plugin, () -> {
			for (DuelsPlayer dp : players) {
				Duels.plugin.dbHandler.saveStatsToDatabaseSQL(dp);
			}
			Duels.plugin.dbHandler.loadAndUpdateWinsScoreboard(winsRanking);
			Duels.plugin.dbHandler.loadAndUpdateEloScoreScoreboard(eloRanking);
		});
		
		timer.start(5);
	}
	
	private double getExpectedScore(int eloOwn, int eloOther) {
		return 1.0 / (1.0 + Math.pow(10, (eloOther - eloOwn) / 400.0));
	}
	
	public String getArenaType() {
		return this.arena.getArenaName();
	}

	public List<Location> getPlacedBlocks() {
		return this.arena.getPlacedBlocks();
	}

	public boolean isFree() {
		return currentGameState == GameState.FREE;
	}

	public boolean isGameOn() {
		return currentGameState == GameState.PLAYING;
	}
	
	public boolean isGameStarting() {
		return currentGameState == GameState.STARTING;
	}

	public void leaveGame(DuelsPlayer dp) {
		players.remove(dp);
		if (players.size() > 0 && players.size() - getOpponentPlayers(dp).size() < 1 && (isGameOn() || isGameStarting())) {
			gameEnd(getOpponentPlayers(dp));
		}
	}

	public void startGame(List<DuelsPlayer> team1, List<DuelsPlayer> team2, Kit kit, GameType gameType) {
		currentGameState = GameState.STARTING;

		this.kit = kit;
		this.players.addAll(team1);
		this.players.addAll(team2);
		this.team1 = team1;
		this.team2 = team2;
		this.gameType = gameType;
		
		this.arena.clearArrows();
		
		teleportPlayersToSpawnPoints();
		
		for (DuelsPlayer dp : players) {
			dp.setGameWhereJoined(this);
			
			Player p = dp.getPlayer();
			
			timer.addPlayer(p);
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
			lobby.clearPotionEffect(p);
			p.setScoreboard(dp.getSidebarHandler().getGameBoard());
			
			List<DuelsPlayer> otherTeamPlayers = this.getOpponentPlayers(dp);
			List<String> opponentNames = new ArrayList<>();
			otherTeamPlayers.forEach(opponent -> opponentNames.add(opponent.getPlayer().getName()));
			dp.getSidebarHandler().updateGameSidebar(getGameTypeString(), kit.getName(), opponentNames);
			
			Bukkit.getScheduler().runTaskLater(Duels.plugin, () -> {
				setKitItems(p, kit.getItems());
				p.setFlying(false);
				p.setAllowFlight(false);
			}, 5);

			Scoreboard scoreboard = p.getScoreboard();
			getOwnTeamPlayers(dp).forEach(teamMate -> scoreboard.getTeam("own").addEntry(teamMate.getPlayer().getName()));
			getOpponentPlayers(dp).forEach(opponent -> scoreboard.getTeam("opponent").addEntry(opponent.getPlayer().getName()));
		}
		
		timer.start(3, "Duelsin alkuun %time% sekuntia");
		
		FileHandler.increaseKitPlayedCount(kit.getName());
	}
	
	private void setKitItems(Player p, List<ItemStack> items) {

		PlayerInventory pInv = p.getInventory();
		pInv.clear();
		
		for (ItemStack is : items) {
			String itemTypeString = is.getType().toString();
			if (itemTypeString.contains("HELMET")) {
				pInv.setHelmet(new ItemStack(is));
			}
			else if (itemTypeString.contains("LEGGINGS")) {
				pInv.setLeggings(new ItemStack(is));
			}
			else if (itemTypeString.contains("CHESTPLATE")) {
				pInv.setChestplate(new ItemStack(is));
			}
			else if (itemTypeString.contains("_BOOTS")) {
				pInv.setBoots(new ItemStack(is));
			}
			else {
				if (is.getMaxStackSize() == 1) {
					int count = is.getAmount();
					ItemStack oneItemIS = new ItemStack(is);
					oneItemIS.setAmount(1);
					for (int i = 0; i < count; i++) {
						pInv.addItem(new ItemStack(oneItemIS));
					}
				}
				else {
					pInv.addItem(new ItemStack(is));
				}
			}
		}
		
		p.updateInventory();
	}
	

	private void teleportPlayersToSpawnPoints() {
		for (int i = 0; i < players.size(); i++) {
			players.get(i).getPlayer().teleport(this.arena.getSpawn(i), TeleportCause.PLUGIN);
		}
	}
	
	private List<DuelsPlayer> getOpponentPlayers(DuelsPlayer dp) {
		return team1.contains(dp) ? team2 : team1;
	}

	private List<DuelsPlayer> getOwnTeamPlayers(DuelsPlayer dp) {
		return team1.contains(dp) ? team1 : team2;
	}
	
	public String getGameTypeString() {
		if (gameType.equals(GameType.FRIEND_CHALLENGE)) return "Kaverihaaste";
		if (gameType.equals(GameType.RANKED)) return "Kilpailullinen";
		return "???";
	}
	
	public boolean isDamageDisabled() {
		if (this.kit == null) return false;
		return this.kit.isInvulnerable();
	}
	
	public boolean isWithinArena(Location loc) {
		return this.arena.isWithinArena(loc);
	}
	
	public boolean isNearArenaFloorLevel(double y) {
		return this.arena.isNearArenaFloorLevel(y);
	}
	
	public boolean isUnderArena(Location loc) {
		return this.arena.isUnderArena(loc);
	}
	
	public Kit getKit() {
		return kit;
	}
	
	public List<DuelsPlayer> getPlayers() {
		return this.players;
	}
}
