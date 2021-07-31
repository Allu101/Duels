package com.allu.duels;

import org.bukkit.entity.Player;

import java.util.List;

public class DuelsPlayer {

	private Player player;
	private List<DuelsPlayer> challengedPlayers;
	private DuelsGame gameWhereJoined = null;
	private PlayerSidebarHandler sidebarHandler;
	
	private int bestWinStreak;
	private int currentWinStreak;
	private int playedGames;
	private int wins;
	private int eloScore;
	
	double gameDamageDone = 0;
	
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
	
	public List<DuelsPlayer> getChallengedPlayers() {
		return challengedPlayers;
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
	
	public void setChallengedPlayers(List<DuelsPlayer> players) {
		this.challengedPlayers = players;
	}
	
	public void setGameWhereJoined(DuelsGame gameWhereJoined) {
		this.gameWhereJoined = gameWhereJoined;
	}
	
	public void setWins(int wins) {
		this.wins = wins;
	}
	
	
	public int getEloScore() {
		return eloScore;
	}
	
	public void setEloScore(int score) {
		this.eloScore = score;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DuelsPlayer other = (DuelsPlayer) obj;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.getUniqueId().toString().equals(other.player.getUniqueId().toString()))
			return false;
		return true;
	}
}
