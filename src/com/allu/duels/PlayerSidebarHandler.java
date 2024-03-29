package com.allu.duels;

import com.allu.minigameapi.player.SidebarHandler;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class PlayerSidebarHandler {
	
	private String header = ChatColor.BLUE + "" + ChatColor.BOLD + " Duels ";
	private String netAddress_row = ChatColor.GOLD + "www.slinkoncraft.net";
	
	private SidebarHandler gameSidebar = new SidebarHandler(header, getGameSidebarRows("", "", new ArrayList<>()));
	private SidebarHandler lobbySidebar = new SidebarHandler(header, getLobbySidebarRows(0, 0, 0, 0, 0));
	private SidebarHandler spectatorSidebar = new SidebarHandler(header, getLobbySidebarRows(0, 0, 0, 0, 0));
	
	public PlayerSidebarHandler() {
		getGameBoard().registerNewTeam("own").setPrefix(ChatColor.GREEN + "");
		getGameBoard().registerNewTeam("opponent").setPrefix(ChatColor.DARK_PURPLE + "");
	}
	
	public Scoreboard getGameBoard() {
		return gameSidebar.getBoard();
	}
	
	public Scoreboard getLobbyBoard() {
		return lobbySidebar.getBoard();
	}
	
	public Scoreboard getSpectatorBoard() {
		return spectatorSidebar.getBoard();
	}

	public void updateGameSidebar(String gameType, String kitName, List<String> opponentNames) {
		gameSidebar.updateSidebar(getGameSidebarRows(gameType, kitName, opponentNames));
	}
	
	public void updateLobbySidebarWinsAndWinStreaks(int wins, int currentWinStreak, int bestWinStreak, int playedGames, int eloScore) {
		lobbySidebar.updateSidebar(getLobbySidebarRows(wins, currentWinStreak, bestWinStreak, playedGames, eloScore));
	}
	
	public void updateSpectatorSidebar(String gameType, String kitName, String...playerNames) {
		spectatorSidebar.updateSidebar(getSpectatorSidebarRows(gameType, kitName, playerNames));
	}
	
	private ArrayList<String> getGameSidebarRows(String gameType, String kitName, List<String> opponentNames) {
		ArrayList<String> rows = new ArrayList<String>();
		
		rows.add("§f§lTyyppi:");
		rows.add(ChatColor.GRAY + gameType);
		rows.add("");
		rows.add("§a§lKit:");
		rows.add(ChatColor.GRAY + kitName);
		rows.add("");
		rows.add("§d§lVastustaja:");
		for (String name : opponentNames) {
			rows.add(ChatColor.GRAY + name);
		}
		rows.add("");
		rows.add(netAddress_row);
		return rows;
	}
	
	private ArrayList<String> getLobbySidebarRows(int wins, int currentWinStreak, int bestWinStreak, int playedGames, int eloScore) {
		ArrayList<String> rows = new ArrayList<String>();
		
		rows.add("");
		rows.add(ChatColor.GRAY + "Voittoja: " + ChatColor.GOLD + wins);
		rows.add(ChatColor.GRAY + "Rankingpisteet: " + ChatColor.GOLD + eloScore);
		rows.add("");
		rows.add(ChatColor.GRAY + "Parhain voittoputki: " + ChatColor.GOLD + bestWinStreak);
		rows.add(ChatColor.GRAY + "Nykyinen voittoputki: " + ChatColor.GOLD + currentWinStreak);
		rows.add("");
		rows.add(ChatColor.GRAY + "Pelatut pelit: " + ChatColor.GOLD + playedGames);
		rows.add("");
		rows.add(netAddress_row);
		return rows;
	}
	
	private ArrayList<String> getSpectatorSidebarRows(String gameType, String kitName, String... playerNames) {
		ArrayList<String> rows = new ArrayList<String>();
		
		rows.add("§f§lTyyppi:");
		rows.add(ChatColor.GRAY + gameType);
		rows.add("");
		rows.add("§a§lKit:");
		rows.add(ChatColor.GRAY + kitName);
		rows.add("");
		rows.add("§d§lPelaajat:");
		for (int i = 0; i < playerNames.length; i++) {
			rows.add(ChatColor.GRAY + playerNames[i]);
		}
		rows.add("");
		rows.add(netAddress_row);
		return rows;
	}
}
