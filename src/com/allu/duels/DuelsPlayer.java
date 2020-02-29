package com.allu.duels;

import org.bukkit.entity.Player;

public class DuelsPlayer {
	
	private Player player;
	private DuelsPlayer challengedPlayer;
	private DuelsGame gameWhereJoined = null;
	private PlayerSidebarHandler sidebarHandler;
	
	private int bestWinStreak;
	private int currentWinStreak;
	private int playedGames;
	private int wins;
	
	public DuelsPlayer(Player p, PlayerSidebarHandler sidebarHandler) {
		this.player = p;
		this.sidebarHandler = sidebarHandler;
	}
	
	private void addCurrentWinStreak() {
		currentWinStreak++;
		if (currentWinStreak > bestWinStreak)
			bestWinStreak = currentWinStreak;
	}
	
	public void addWin() {
		wins++;
		playedGames++;
		addCurrentWinStreak();
	}
	public void addLose() {
		this.currentWinStreak = 0;
		playedGames++;
	}
	
	
	public int getBestWinStreak() {
		return bestWinStreak;
	}
	
	public DuelsPlayer getChallengedPlayer() {
		return challengedPlayer;
	}
	
	public int getCurrentWinStreak() {
		return currentWinStreak;
	}
	
	public DuelsGame getGameWhereJoined() {
		return gameWhereJoined;
	}

	public Player getPlayer() {
		return player;
	}
	
	public PlayerSidebarHandler getSidebarHandler() {
		return sidebarHandler;
	}
	
	public double getWinLoseRatio() {
		int loses = playedGames - wins;
		if(loses == 0) {
			return wins;
		}
		return ((double)wins / loses);
	}
	
	public int getPlayedGames() {
		return this.playedGames;
	}
	
	public int getWins() {
		return wins;
	}
	
	public boolean is(String uuid) {
		return uuid.equals(player.getUniqueId().toString());
	}
	
	public void setPlayedGames(int playedGames) {
		this.playedGames = playedGames;
	}
	
	public void setCurrentWinStreak(int currentWinStreak) {
		this.currentWinStreak = currentWinStreak;
	}
	
	public void setBestWinStreak(int bestWinStreak) {
		this.bestWinStreak = bestWinStreak;
	}
	
	public void setChallengedPlayer(DuelsPlayer dp) {
		this.challengedPlayer = dp;
	}
	
	public void setGameWhereJoined(DuelsGame gameWhereJoined) {
		this.gameWhereJoined = gameWhereJoined;
	}
	
	public void setWins(int wins) {
		this.wins = wins;
	}
	
}
