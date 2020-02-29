package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.CountDownTimer;
import com.allu.minigameapi.CountDownTimerListener;
import com.allu.minigameapi.MessageHandler;

public class DuelsGame implements CountDownTimerListener {
	
	private enum GameState { FREE, STARTING, PLAYING, GAME_FINISH }
	private GameState currentGameState = GameState.FREE;
	private Gamemode gameMode;
	
	private ArrayList<Location> buildedBlocks = new ArrayList<Location>();
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private Location spawn1, spawn2;
	
	private Lobby lobby;
	private MessageHandler messages;
	private CountDownTimer timer;
	
	public DuelsGame(Lobby lobby, Location arenaCenterLoc, Gamemode gameMode, MessageHandler messages) {
		this.lobby = lobby;
		this.spawn1 = arenaCenterLoc.clone().add(0, 0, -26);
		this.spawn2 = arenaCenterLoc.clone().add(0, 0, 26);
		spawn2.setYaw(-180);
		this.gameMode = gameMode;
		this.timer = new CountDownTimer(this);
		this.messages = messages;
	}
	
	@Override
	public void onCountDownChange(int time) {
		
	}
	
	@Override
	public void onCountDownFinish() {
		if(currentGameState == GameState.STARTING) {
			currentGameState = GameState.PLAYING;
			for(DuelsPlayer dp : players) {
				Player p = dp.getPlayer();
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
				p.sendMessage(ChatColor.GREEN + "Duels alkaa!");
			}
		}
		else if(currentGameState == GameState.GAME_FINISH) {
			for(DuelsPlayer dp : players) {
				lobby.teleportToSpawn(dp.getPlayer());
				dp.setGameWhereJoined(null);
				dp.getPlayer().setScoreboard(dp.getSidebarHandler().getLobbyBoard());
				dp.getSidebarHandler().updateLobbySidebar();
			}
			timer.clearPlayers();
			players.clear();
			currentGameState = GameState.FREE;
		}
	}
	
	public void gameEnd(Player winner) {
		currentGameState = GameState.GAME_FINISH;
		for(DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			p.setGameMode(GameMode.SPECTATOR);
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(ChatColor.GREEN + "Voittaja: " + ChatColor.GOLD + winner.getName()));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
		}
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
	}
	
	public void startGame(List<DuelsPlayer> dplayers, Kit kit) {
		currentGameState = GameState.STARTING;
		for(DuelsPlayer dp : dplayers) {
			players.add(dp);
			Player p = dp.getPlayer();
			dp.setGameWhereJoined(this);
			timer.addPlayer(p);
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
			getSpawn(p);
			setKitItems(p, kit.getItems());
			p.setScoreboard(dp.getSidebarHandler().getGameBoard());
			dp.getSidebarHandler().updateGameSidebar();
		}
		timer.start(5, "Duelsin alkuun");
	}
	
	private void getSpawn(Player p) {
		for(int i = 0; i < players.size(); i++) {
			if(i % 2 == 0) {
				p.teleport(spawn1);
			} else {
				p.teleport(spawn2);
			}
		}
	}
	
	private void setKitItems(Player p, List<ItemStack> items) {
		for(ItemStack is : items) {
			p.getInventory().addItem(is);
		}
	}
}
