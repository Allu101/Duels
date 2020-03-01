package com.allu.duels;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;

import com.allu.minigameapi.player.SidebarHandler;

public class PlayerSidebarHandler {
	
	private String header = ChatColor.BLUE + "" + ChatColor.BOLD + " Duels ";
	private String netAddress_row = ChatColor.GRAY + "www.slinkoncraft.net";
	
	private SidebarHandler gameSidebar = new SidebarHandler(header, getGameSidebarRows(""));
	private SidebarHandler lobbySidebar = new SidebarHandler(header, getLobbySidebarRows(0, 0, 0, 0));
	
	public PlayerSidebarHandler() {
		gameSidebar.updateSidebar(getGameSidebarRows(""));
	}
	
	public Scoreboard getGameBoard() {
		return gameSidebar.getBoard();
	}
	
	public Scoreboard getLobbyBoard() {
		return lobbySidebar.getBoard();
	}

	public void updateGameSidebar(String duelsMode) {
		gameSidebar.updateSidebar(getGameSidebarRows(duelsMode));
	}
	
	public void updateLobbySidebarWinsAndWinStreaks(int wins, int currentWinStreak, int bestWinStreak, int playedGames) {
		lobbySidebar.updateSidebar(getLobbySidebarRows(wins, currentWinStreak, bestWinStreak, playedGames));
	}
	
	private ArrayList<String> getGameSidebarRows(String duelsMode) {
		ArrayList<String> rows = new ArrayList<String>();
		
		rows.add(ChatColor.WHITE + "Mode: " + ChatColor.GRAY + duelsMode);
		rows.add("");
		rows.add(netAddress_row);
		return rows;
	}
	
	private ArrayList<String> getLobbySidebarRows(int wins, int currentWinStreak, int bestWinStreak, int playedGames) {
		ArrayList<String> rows = new ArrayList<String>();
		
		String totalWins_row = ChatColor.GRAY + "Voittoja: " + ChatColor.GOLD + wins;
		String currentWinStreak_row = ChatColor.GRAY + "Nykyinen voittoputki: " + ChatColor.GOLD + currentWinStreak;
		String bestWinStreak_row = ChatColor.GRAY + "Parhain voittoputki: " + ChatColor.GOLD + bestWinStreak;
		String playedGamesRow = ChatColor.GRAY + "Pelatut pelit: " + ChatColor.GOLD + playedGames;
		
		rows.add("");
		rows.add(totalWins_row);
		rows.add("");
		rows.add(currentWinStreak_row);
		rows.add(bestWinStreak_row);
		rows.add("");
		rows.add(playedGamesRow);
		rows.add("");
		rows.add(netAddress_row);
		return rows;
	}
	
}
