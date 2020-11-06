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

	private List<DuelsPlayer> players = new ArrayList<DuelsPlayer>();

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
				currentGameState = GameState.FREE;
			}
		});
	}
	
	private void checkForDrawGame() {
		Bukkit.getScheduler().runTaskLater(Duels.plugin, () -> {
			
			if (currentGameState != GameState.PLAYING)
				return;
			
			long timeElapsed = System.currentTimeMillis() - gameStartTimeMillis;
			if (timeElapsed >= Duels.matchMinutesUntilDraw * 60 * 1000) {
				// Draw game
				gameEnd(null);
			} else {
				checkForDrawGame();
			}
			
		}, 100);
	}
	
	
	public void onPlayerDie(Player deadPlayer) {	
		deadPlayer.setGameMode(GameMode.SPECTATOR);
		deadPlayer.getWorld().strikeLightningEffect(deadPlayer.getLocation());
		lobby.clearPlayerInventoryAndEquipment(deadPlayer);
		
		for (DuelsPlayer dp : players) {
			if (!dp.getPlayer().equals(deadPlayer)) {
				gameEnd(dp);
				return;
			}
		}
	}

	public void gameEnd(DuelsPlayer winner) {	
		currentGameState = GameState.GAME_FINISH;
		
		String winMessage = winner == null ? (ChatColor.GOLD + "Tasapeli")
				: ChatColor.GREEN + "Voittaja: " + ChatColor.GOLD + winner.getPlayer().getName();
		for (DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			lobby.clearPotionEffect(p);
			
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(winMessage));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));

			if (winner == null) {
				titleHandler.sendTitle(p, "§e§lTASAPELI");

			} else if (dp.equals(winner)) {
				dp.addWin();
				titleHandler.sendTitle(p, "§a§lVOITTO");
			} else {
				dp.addLose();
				titleHandler.sendTitle(p, "§c§lTAPPIO");
			}
			
			if (gameType.equals(GameType.FRIEND_CHALLENGE)) {
				p.sendMessage(ChatColor.GRAY + "Kaveripelit eivät vaikuta ranking-pisteisiin");
			}
			else {
				DuelsPlayer opponent = getOtherPlayer(dp);
				
				if (dp != null && opponent != null) {
					
					double result = 0;
					if (winner == null) {
						result = 0.5;
					}
					else if (dp.equals(winner))
						result = 1;
					
					double expectedScore = getExpectedScore(dp.getEloScore(), opponent.getEloScore());
					double eloChange = (result - expectedScore) * 32 + 0.2; // + 0.2 for little total score increase over time.
					int finalEloChange = (int)Math.round(eloChange);
					
					if (dp.getEloScore() + finalEloChange < 0) {
						finalEloChange = -dp.getEloScore();
					}
					dp.setEloScore(dp.getEloScore() + finalEloChange);
					
					if (finalEloChange == 0) {
						p.sendMessage(ChatColor.GRAY + "Ranking-pisteesi eiv�t muuttuneet pelin tuloksena.");
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

			
			Duels.plugin.dbHandler.saveStatsToDatabaseSQL(dp);
		}
		winsRanking.updateRanking(Duels.plugin.dbHandler.loadPlayersToWinsScoreboard());
		eloRanking.updateRanking(Duels.plugin.dbHandler.loadPlayersToEloScoreScoreboard());
		
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
		if (players.size() > 0 && isGameOn()) {
			gameEnd(players.get(0));
		}
	}

	public void startGame(List<DuelsPlayer> dplayers, Kit kit, GameType gameType) {
		
		this.kit = kit;
		
		currentGameState = GameState.STARTING;
		
		this.players = dplayers;
		this.gameType = gameType;
		
		this.arena.clearArrows();
		
		teleportPlayersToSpawnPoints();
		
		for (DuelsPlayer dp : dplayers) {
			dp.setGameWhereJoined(this);
			
			Player p = dp.getPlayer();
			
			timer.addPlayer(p);
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
			lobby.clearPotionEffect(p);
			p.setScoreboard(dp.getSidebarHandler().getGameBoard());
			
			DuelsPlayer opponent = this.getOtherPlayer(dp);
			String opponentString = "";
			if (opponent != null) {
				opponentString = opponent.getPlayer().getName();
			}
			dp.getSidebarHandler().updateGameSidebar(getGameTypeString(), kit.getName(), opponentString);
			
			Bukkit.getScheduler().runTaskLater(Duels.plugin, () -> {
				setKitItems(p, kit.getItems());
				p.getPlayer().setFlying(false);
				p.getPlayer().setAllowFlight(false);
			}, 5);
		}
		
		timer.start(3, "Duelsin alkuun %time% sekuntia");
		
		FileHandler.increaseKitPlayedCount(kit.getName());
	}
	
	private void setKitItems(Player p, List<ItemStack> items) {
		
		p.getInventory().clear();
		
		for (ItemStack is : items) {
			String itemTypeString = is.getType().toString();
			if (itemTypeString.contains("HELMET")) {
				p.getInventory().setHelmet(new ItemStack(is));
			}
			else if (itemTypeString.contains("LEGGINGS")) {
				p.getInventory().setLeggings(new ItemStack(is));
			}
			else if (itemTypeString.contains("CHESTPLATE")) {
				p.getInventory().setChestplate(new ItemStack(is));
			}
			else if (itemTypeString.contains("_BOOTS")) {
				p.getInventory().setBoots(new ItemStack(is));
			}
			else {
				if (is.getMaxStackSize() == 1) {
					int count = is.getAmount();
					ItemStack oneItemIS = new ItemStack(is);
					oneItemIS.setAmount(1);
					for (int i = 0; i < count; i++) {
						p.getInventory().addItem(new ItemStack(oneItemIS));
					}
				}
				else {
					p.getInventory().addItem(new ItemStack(is));
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
	
	private DuelsPlayer getOtherPlayer(DuelsPlayer dpp) {
		for (DuelsPlayer dpp2: this.players) {
			if (!dpp.equals(dpp2)) {
				return dpp2;
			}
		}
		return null;
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
