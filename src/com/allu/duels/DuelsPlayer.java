package com.allu.duels;

import org.bukkit.entity.Player;

public class DuelsPlayer {
	
	private Player player;
	private Player challengedPlayer;
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
	
	public boolean is(String uuid) {
		return uuid.equals(player.getUniqueId().toString());
	}

	public Player getChallengedPlayer() {
		return challengedPlayer;
	}

	public void setChallengedPlayer(Player p) {
		this.challengedPlayer = p;
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
