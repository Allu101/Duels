package com.allu.duels;

import org.bukkit.entity.Player;

public class DuelsPlayer {
	
	public enum PlayerState { NORMAL, RECEIVED_REQUEST }
	public PlayerState playerState = PlayerState.NORMAL;
	
	private Player player;
	private DuelsPlayer requesterPlayer;
	private DuelsGame gameWhereJoined;
	
	private int bestWinStreak;
	private int currentWinStreak;
	private int wins;
	private int winLoseRatio;
	
	public DuelsPlayer(Player p) {
		this.player = p;
	}

	public Player getPlayer() {
		return player;
	}

	public PlayerState getPlayerState() {
		return playerState;
	}
	
	public boolean is(String uuid) {
		return uuid.equals(player.getUniqueId().toString());
	}
	
	public void setPlayerState(PlayerState playerState) {
		this.playerState = playerState;
	}

	public DuelsPlayer getRequesterPlayer() {
		return requesterPlayer;
	}

	public void setRequesterPlayer(DuelsPlayer dp) {
		this.requesterPlayer = dp;
	}

	public DuelsGame getGameWhereJoined() {
		return gameWhereJoined;
	}

	public void setGameWhereJoined(DuelsGame gameWhereJoined) {
		this.gameWhereJoined = gameWhereJoined;
	}

	
	public int getBestWinStreak() {
		return bestWinStreak;
	}

	public void setBestWinStreak(int bestWinStreak) {
		this.bestWinStreak = bestWinStreak;
	}

	public int getCurrentWinStreak() {
		return currentWinStreak;
	}

	public void setCurrentWinStreak(int currentWinStreak) {
		this.currentWinStreak = currentWinStreak;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getWinLoseRatio() {
		return winLoseRatio;
	}

	public void setWinLoseRatio(int winLoseRatio) {
		this.winLoseRatio = winLoseRatio;
	}
	
}
