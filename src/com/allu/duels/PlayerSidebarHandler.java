package com.allu.duels;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;

import com.allu.minigameapi.player.SidebarHandler;

public class PlayerSidebarHandler {
	
	private int wins;
	private int bestWinStreak;
	private int currentWinStreak;
	private String bestWinStreak_row;
	private String currentWinStreak_row;
	private String header = ChatColor.BLUE + "" + ChatColor.BOLD + " Duels ";
	private String duelsMode;
	private String netAddress_row = ChatColor.GRAY + "www.slinkoncraft.net";
	private String totalWins_row;
	
	private SidebarHandler gameSidebar = new SidebarHandler(header, getGameSidebarRows());
	private SidebarHandler lobbySidebar = new SidebarHandler(header, getLobbySidebarRows());
	
	public PlayerSidebarHandler() {
		this.duelsMode = "";
		this.wins = 0;
		gameSidebar.updateSidebar(getGameSidebarRows());
	}
	
	public Scoreboard getGameBoard() {
		return gameSidebar.getBoard();
	}
	
	public Scoreboard getLobbyBoard() {
		return lobbySidebar.getBoard();
	}
	
	public void resetGameSidebar() {
		duelsMode = "";
	}

	public void updateGameSidebar() {
		lobbySidebar.updateSidebar(getGameSidebarRows());
	}
	
	public void updateLobbySidebar() {
		lobbySidebar.updateSidebar(getLobbySidebarRows());
	}
	
	public void updateLobbySidebarWinsAndWinStreaks(int wins, int currentWinStreak, int bestWinStreak) {
		this.wins = wins;
		this.currentWinStreak = currentWinStreak;
		this.bestWinStreak = bestWinStreak;
		lobbySidebar.updateSidebar(getLobbySidebarRows());
	}
	
	private ArrayList<String> getGameSidebarRows() {
		ArrayList<String> rows = new ArrayList<String>();
		
		rows.add(ChatColor.WHITE + "Mode: " + ChatColor.GRAY + duelsMode);
		rows.add("");
		rows.add(netAddress_row);
		return rows;
	}
	
	private ArrayList<String> getLobbySidebarRows() {
		ArrayList<String> rows = new ArrayList<String>();
		
		totalWins_row = ChatColor.GRAY + "Voittoja: " + ChatColor.GOLD + wins;
		currentWinStreak_row = ChatColor.GRAY + "Nykyinen voittoputki: " + ChatColor.GOLD + currentWinStreak;
		bestWinStreak_row = ChatColor.GRAY + "Parhain voittoputki: " + ChatColor.GOLD + bestWinStreak;
		
		rows.add(totalWins_row);
		rows.add("");
		rows.add(currentWinStreak_row);
		rows.add(bestWinStreak_row);
		rows.add("");
		rows.add(netAddress_row);
		return rows;
	}
	
}
