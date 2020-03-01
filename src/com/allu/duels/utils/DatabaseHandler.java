package com.allu.duels.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;

import com.allu.duels.DuelsPlayer;
import com.allu.minigameapi.ranking.RankedPlayer;

public class DatabaseHandler {
	
	public Connection connection;
	private JavaPlugin plugin;
	
	public DatabaseHandler(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	public synchronized void openConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			connection = DriverManager.getConnection(plugin.getConfig().getString("database.url"),
					plugin.getConfig().getString("database.username"),
					plugin.getConfig().getString("database.password"));
			
	    } catch (SQLException e) {
	        System.out.println("DatabaseHandler error - openConnetion()");
	    } catch (ClassNotFoundException e) {
	    	System.out.println("DatabaseHandler error - openConnetion()");
		}
	}
	
	
	
	
	
	public synchronized void closeConnection() {
		try {
    		if (connection != null && !connection.isClosed())
    			connection.close();
    	} catch (SQLException e) {
    		System.out.println("DatabaseHandler error - CloseConnetion()");
    	}
	}
	
	
	
	
	
	public synchronized boolean saveStatsToDatabaseSQL(DuelsPlayer dp) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement(
					"INSERT INTO duels (uuid, name, wins, playedGames, currentWinStreak, highestWinStreak, eloScore) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
					+ "name=?, wins=?, playedGames=?, currentWinStreak=?, highestWinStreak=?, eloScore=?");
			
			sql.setString(1, dp.getPlayer().getUniqueId().toString());
			sql.setString(2, dp.getPlayer().getName());
			sql.setInt(3, dp.getWins());
			sql.setInt(4, dp.getPlayedGames());
			sql.setInt(5, dp.getCurrentWinStreak());
			sql.setInt(6, dp.getBestWinStreak());
			sql.setInt(7, dp.getEloScore());
			
			sql.setString(8, dp.getPlayer().getName());
			sql.setInt(9, dp.getWins());
			sql.setInt(10, dp.getPlayedGames());
			sql.setInt(11, dp.getCurrentWinStreak());
			sql.setInt(12, dp.getBestWinStreak());
			sql.setInt(13, dp.getEloScore());
			
			sql.executeUpdate();
			sql.close();
			
			closeConnection();
			return true;
			
		} catch (SQLException e) {
			System.out.println("DatabaseHandler error - saveStatsToDatabaseSQL()");
			closeConnection();
			return false;
		} catch (NullPointerException e) {
			System.out.println("DatabaseHandler nullPointer - saveStatsToDatabaseSQL()");
			return false;
		}
	}
	
	public synchronized boolean loadStatsSQL(DuelsPlayer dp) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement(
					"SELECT wins, playedGames, currentWinStreak, highestWinStreak, eloScore FROM duels WHERE uuid=?");
			
			sql.setString(1, dp.getPlayer().getUniqueId().toString());
			ResultSet result = sql.executeQuery();
			
			while (result.next()) {
				dp.setWins(result.getInt(1));
				dp.setPlayedGames(result.getInt(2));
				dp.setCurrentWinStreak(result.getInt(3));
				dp.setBestWinStreak(result.getInt(4));
				dp.setEloScore(result.getInt(5));
			}
			
			result.close();
			sql.close();
			closeConnection();
			return true;
			
		} catch (SQLException e) {
			System.out.println("DatabaseHandler error - loadStatsSQL()");
			closeConnection();
			return false;
		} catch (NullPointerException e) {
			System.out.println("DatabaseHandler nullPointer - loadStatsSQL()");
			return false;
		}
	}
	
	public synchronized ArrayList<RankedPlayer> loadTop10PlayersToWinsScoreboard() {
		openConnection();
		ArrayList<RankedPlayer> players = new ArrayList<RankedPlayer>();
		try {
			PreparedStatement sql = connection.prepareStatement("SELECT uuid, name, wins FROM duels ORDER BY wins DESC LIMIT 10");
			
			ResultSet result = sql.executeQuery();

			while (result.next()) {
				players.add(new RankedPlayer(result.getString(1), result.getString(2), result.getInt(3)));
			}
			
			result.close();
			sql.close();
			closeConnection();
			
			return players;
			
		} catch (SQLException e) {
			System.out.println("DatabaseHandler error - loadTop10PlayersToWinsScoreboard()");
			closeConnection();
			return players;
		} catch (NullPointerException e) {
			System.out.println("DatabaseHandler nullPointer - loadTop10PlayersToWinsScoreboard()");
			return players;
		}
	}
	
	
	public synchronized ArrayList<RankedPlayer> loadTop10PlayersToEloScoreScoreboard() {
		openConnection();
		ArrayList<RankedPlayer> players = new ArrayList<RankedPlayer>();
		try {
			PreparedStatement sql = connection.prepareStatement("SELECT uuid, name, eloScore FROM duels ORDER BY eloScore DESC LIMIT 10");
			
			ResultSet result = sql.executeQuery();

			while (result.next()) {
				players.add(new RankedPlayer(result.getString(1), result.getString(2), result.getInt(3)));
			}
			
			result.close();
			sql.close();
			closeConnection();
			
			return players;
			
		} catch (SQLException e) {
			System.out.println("DatabaseHandler error - loadTop10PlayersToEloScoreScoreboard()");
			closeConnection();
			return players;
		} catch (NullPointerException e) {
			System.out.println("DatabaseHandler nullPointer - loadTop10PlayersToEloScoreScoreboard()");
			return players;
		}
	}
}
