package com.allu.duels;

import org.bukkit.entity.Player;

public class DuelsPlayer {
	
	private Player player;
	private DuelsPlayer challengedPlayer;
	private DuelsGame gameWhereJoined;
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
	}
	
	public void addWin() {
		wins++;
		addCurrentWinStreak();
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
	
	public int getWins() {
		return wins;
	}
	
	public boolean is(String uuid) {
		return uuid.equals(player.getUniqueId().toString());
	}
	
	public void saveGameStats() {
		playedGames++;
		resetGameStats();
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
	
	private void resetGameStats() {
		sidebarHandler.resetGameSidebar();
	}
	
}
