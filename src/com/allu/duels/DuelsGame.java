package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.CountDownTimer;
import com.allu.minigameapi.CountDownTimerListener;
import com.allu.minigameapi.MessageHandler;
import com.allu.minigameapi.ranking.SimpleRanking;

public class DuelsGame implements CountDownTimerListener {

	private enum GameState {
		FREE, STARTING, PLAYING, GAME_FINISH
	}

	private GameState currentGameState = GameState.FREE;
	private Gamemode gameMode;

	private ArrayList<Location> buildedBlocks = new ArrayList<Location>();
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private Location arenaCenterLoc, spawn1, spawn2;

	private Lobby lobby;
	private MessageHandler messages;
	private CountDownTimer timer;
	
	private int arenaXWidth = 50;
	private int arenaZWidth = 80;
	
	private SimpleRanking winsRanking;

	public DuelsGame(Lobby lobby, Location arenaCenterLoc, Gamemode gameMode, MessageHandler messages, SimpleRanking winsRanking) {
		this.lobby = lobby;
		this.arenaCenterLoc = arenaCenterLoc;
		this.spawn1 = arenaCenterLoc.clone().add(0, 0, -26);
		this.spawn2 = arenaCenterLoc.clone().add(0, 0, 26);
		spawn2.setYaw(-180);
		this.gameMode = gameMode;
		this.timer = new CountDownTimer(this);
		this.messages = messages;
		this.winsRanking = winsRanking;
	}

	@Override
	public void onCountDownChange(int time) {

	}

	@Override
	public void onCountDownFinish() {
		if (currentGameState == GameState.STARTING) {
			currentGameState = GameState.PLAYING;
			for (DuelsPlayer dp : players) {
				Player p = dp.getPlayer();
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
				p.sendMessage(ChatColor.GREEN + "Duels alkaa!");
			}
		} else if (currentGameState == GameState.GAME_FINISH) {
			
			for (DuelsPlayer dp : players) {
				lobby.sendPlayerToLobby(dp);
			}
			
			timer.clearPlayers();
			players.clear();
			currentGameState = GameState.FREE;
		}
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
		
		for (DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			lobby.clearPotionEffect(p);
			
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			p.sendMessage("");
			p.sendMessage(
					messages.getCenteredMessage(ChatColor.GREEN + "Voittaja: " + ChatColor.GOLD + winner.getPlayer().getName()));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			
			if (dp.equals(winner)) {
				dp.addWin();
			} else {
				dp.addLose();
			}
			
			Duels.plugin.dbHandler.saveStatsToDatabaseSQL(dp);
		}
		
		winsRanking.updateRankingWithPlayers(Duels.plugin.dbHandler.loadTop10PlayersToWinsScoreboard());
		
		timer.start(5, "");
	}

	
	
	public Gamemode getGamemode() {
		return gameMode;
	}

	public ArrayList<Location> getPlacedBlocks() {
		return buildedBlocks;
	}

	public boolean isFree() {
		return currentGameState == GameState.FREE;
	}

	public boolean isGameOn() {
		return currentGameState == GameState.PLAYING;
	}

	public void leaveGame(DuelsPlayer dp) {
		players.remove(dp);
		gameEnd(players.get(0));
		
	}

	public void startGame(List<DuelsPlayer> dplayers, Kit kit) {
		for (Entity entity : spawn1.getWorld().getEntities()) {
			if (!entity.getType().equals(EntityType.ARMOR_STAND))
				entity.remove();
		}
		
		currentGameState = GameState.STARTING;
		for (DuelsPlayer dp : dplayers) {
			players.add(dp);
			Player p = dp.getPlayer();
			dp.setGameWhereJoined(this);
			timer.addPlayer(p);
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
			getSpawn(p);
			setKitItems(p, kit.getItems());
			lobby.clearPotionEffect(p);
			p.setScoreboard(dp.getSidebarHandler().getGameBoard());
			dp.getSidebarHandler().updateGameSidebar("1 vs 1");
		}
		timer.start(5, "Duelsin alkuun");
	}

	private void getSpawn(Player p) {
		for (int i = 0; i < players.size(); i++) {
			if (i % 2 == 0) {
				p.teleport(spawn1);
			} else {
				p.teleport(spawn2);
			}
		}
	}
	
	public boolean isWithinArena(Location loc) {
		
		if (!loc.getWorld().equals(this.arenaCenterLoc.getWorld()))
			return false;
		
		if (loc.getBlockX() < this.arenaCenterLoc.getBlockX() - this.arenaXWidth / 2
				|| loc.getBlockX() > this.arenaCenterLoc.getBlockX() + this.arenaXWidth / 2
				|| loc.getBlockZ() < this.arenaCenterLoc.getBlockZ() - this.arenaZWidth / 2
				|| loc.getBlockZ() > this.arenaCenterLoc.getBlockZ() + this.arenaZWidth / 2) {
			return false;
		}
		return true;
	}
	
	public boolean isUnderArena(Location loc) {
		if (!loc.getWorld().equals(this.arenaCenterLoc.getWorld()))
			return false;
		
		return loc.getBlockY() < this.arenaCenterLoc.getBlockY() - 4;
	}

	private void setKitItems(Player p, List<ItemStack> items) {
		
		p.getInventory().clear();
		
		for (ItemStack is : items) {

			if (is.getType().toString().contains("HELMET")) {
				p.getInventory().setHelmet(new ItemStack(is));
			}
			else if (is.getType().toString().contains("LEGGINGS")) {
				p.getInventory().setLeggings(new ItemStack(is));
			}
			else if (is.getType().toString().contains("CHESTPLATE")) {
				p.getInventory().setChestplate(new ItemStack(is));
			}
			else if (is.getType().toString().contains("_BOOTS")) {
				p.getInventory().setBoots(new ItemStack(is));
			}
			else {
				p.getInventory().addItem(new ItemStack(is));
			}
		}
		
		p.updateInventory();
	}
	
}
