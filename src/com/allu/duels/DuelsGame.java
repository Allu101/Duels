package com.allu.duels;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.allu.duels.utils.Gamemode;
import com.allu.minigameapi.CountDownTimer;
import com.allu.minigameapi.CountDownTimerListener;
import com.allu.minigameapi.MessageHandler;

public class DuelsGame implements CountDownTimerListener {
	
	public enum GameState { FREE, STARTING, PLAYING, GAME_FINISH }
	public GameState currentGameState = GameState.FREE;
	
	private Gamemode gameMode;
	
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private Location spawnCenter1, spawnCenter2;
	
	private CountDownTimer timer;
	private Lobby lobby;
	private MessageHandler messages;
	
	public DuelsGame(Lobby lobby, Location arenaCenterLoc, Gamemode gameMode) {
		this.lobby = lobby;
		this.spawnCenter1 = arenaCenterLoc.clone().add(0, 0, -30);
		this.spawnCenter2 = arenaCenterLoc.clone().add(0, 0, 30);
		this.gameMode = gameMode;
		this.timer = new CountDownTimer(this);
	}
	
	public void joinGame(DuelsPlayer dp) {
		players.add(dp);
		if(gameCanBeStart()) {
			currentGameState = GameState.STARTING;
			startGame();
		}
	}
	
	public void leaveGame(DuelsPlayer dp) {
		players.remove(dp);
	}
	
	public void startGame() {
		for(DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			timer.addPlayer(p);
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0f);
			getSpawn(p);
		}
		timer.start(5, "Duelsin alkuun");
	}
	
	@Override
	public void onCountDownFinish() {
		if(currentGameState == GameState.STARTING) {
			currentGameState = GameState.PLAYING;
			for (DuelsPlayer dp : players) {
				Player p = dp.getPlayer();
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0f);
				p.sendMessage(ChatColor.GREEN + "Duels alkaa!");
			}
		}
		else if(currentGameState == GameState.GAME_FINISH) {
			for(DuelsPlayer dp : players) {
				lobby.teleportToSpawn(dp.getPlayer());
				dp.setGameWhereJoined(null);
			}
			timer.clearPlayers();
			currentGameState = GameState.FREE;
			players.clear();
		}
	}
	
	public void gameEnd(Player winner) {
		currentGameState = GameState.GAME_FINISH;
		for(DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			p.sendMessage(messages.getCenteredMessage(""));
			p.sendMessage(messages.getCenteredMessage(ChatColor.GREEN + "Voittaja: " + ChatColor.GOLD + winner.getName()));
			p.sendMessage(messages.getCenteredMessage(""));
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			timer.start(5, "");
		}
	}
	
	public boolean isFree() {
		return currentGameState == GameState.FREE;
	}
	
	public boolean isGameOn() {
		return currentGameState == GameState.PLAYING;
	}
	
	public Gamemode getGamemode() {
		return gameMode;
	}
	
	@Override
	public void onCountDownChange(int time) {
		
	}
	
	private boolean gameCanBeStart() {
		if(players.size() == gameMode.getTeamSize()) {
			return true;
		}
		return false;
	}
	
	private void getSpawn(Player p) {
		for(int i = 0; i < players.size(); i++) {
			if(players.size() % 2 == 0) {
				p.teleport(spawnCenter1);
			} else {
				p.teleport(spawnCenter2);
			}
		}
	}
}
