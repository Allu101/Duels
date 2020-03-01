package com.allu.duels;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.allu.duels.utils.Gamemode;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.CountDownTimer;
import com.allu.minigameapi.CountDownTimerListener;
import com.allu.minigameapi.MessageHandler;
import com.allu.minigameapi.ranking.SimpleRanking;

public class DuelsGame implements CountDownTimerListener {

	private enum GameState {
		FREE, STARTING, PLAYING, GAME_FINISH
	}

	private GameState currentGameState = GameState.FREE;
	private Gamemode gameMode;

	private ArrayList<Location> buildedBlocks = new ArrayList<Location>();
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private Location arenaCenterLoc, spawn1, spawn2;

	private Lobby lobby;
	private MessageHandler messages;
	private CountDownTimer timer;
	
	private int arenaXWidth = 50;
	private int arenaZWidth = 80;
	
	private SimpleRanking winsRanking;
	private SimpleRanking eloRanking;

	public DuelsGame(Lobby lobby, Location arenaCenterLoc, Gamemode gameMode, MessageHandler messages, SimpleRanking winsRanking, SimpleRanking eloRanking) {
		this.lobby = lobby;
		this.arenaCenterLoc = arenaCenterLoc;
		this.spawn1 = arenaCenterLoc.clone().add(0, 0, -26);
		this.spawn2 = arenaCenterLoc.clone().add(0, 0, 26);
		spawn2.setYaw(-180);
		this.gameMode = gameMode;
		this.timer = new CountDownTimer(this);
		this.messages = messages;
		this.winsRanking = winsRanking;
		this.eloRanking = eloRanking;
	}

	@Override
	public void onCountDownChange(int time) {

	}

	@Override
	public void onCountDownFinish() {
		Bukkit.getScheduler().runTask(Duels.plugin, new Runnable() {

			@Override
			public void run() {
				if (currentGameState == GameState.STARTING) {
					currentGameState = GameState.PLAYING;
					for (DuelsPlayer dp : players) {
						Player p = dp.getPlayer();
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
						p.sendMessage(ChatColor.GREEN + "Duels alkaa!");
						sendTitle(p, "Duels alkaa", 0, 40, 20, ChatColor.GREEN);
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
								"title " + p.getName() + " times 0 20 10");
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
								"title " + p.getName() + " title {\"text\":\"Duels alkaa!\",\"bold\":true,\"color\":\"blue\"}");
					}
				} else if (currentGameState == GameState.GAME_FINISH) {
					
					for (DuelsPlayer dp : players) {
						lobby.sendPlayerToLobby(dp);
					}
					
					timer.clearPlayers();
					players.clear();
					currentGameState = GameState.FREE;
				}
			}
		});
	}
	
	public void onPlayerDie(Player deadPlayer) {	
		deadPlayer.setGameMode(GameMode.SPECTATOR);
		deadPlayer.getWorld().strikeLightningEffect(deadPlayer.getLocation());
		lobby.clearPlayerInventoryAndEquipment(deadPlayer);
		
		for (DuelsPlayer dp : players) {
			if (!dp.getPlayer().equals(deadPlayer)) {
				gameEnd(dp);
				return;
			}
		}
	}

	public void gameEnd(DuelsPlayer winner) {	
		currentGameState = GameState.GAME_FINISH;
		
		for (DuelsPlayer dp : players) {
			Player p = dp.getPlayer();
			lobby.clearPotionEffect(p);
			
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			p.sendMessage("");
			p.sendMessage(
					messages.getCenteredMessage(ChatColor.GREEN + "Voittaja: " + ChatColor.GOLD + winner.getPlayer().getName()));
			p.sendMessage("");
			p.sendMessage(messages.getCenteredMessage(lobby.LINE));
			
			if (dp.equals(winner)) {
				dp.addWin();
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " times 0 80 20");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " title {\"text\":\"VOITTO\",\"bold\":true,\"color\":\"green\"}");
			} else {
				dp.addLose();
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " times 0 80 20");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
						"title " + p.getName() + " title {\"text\":\"TAPPIO\",\"bold\":true,\"color\":\"red\"}");
			}
			
			for (DuelsPlayer dp2 : players) {
				if (dp.equals(dp2))
					continue;
				
				double result = 0;
				if (dp.equals(winner))
					result = 1;
				
				double expectedScore = getExpectedScore(dp.getEloScore(), dp2.getEloScore());
				double eloChange = (result - expectedScore) * 32;
				int finalEloChange = Math.max((int)Math.round(eloChange), 0);
				
				dp.setEloScore(dp.getEloScore() + finalEloChange);
			}
			
			
			Duels.plugin.dbHandler.saveStatsToDatabaseSQL(dp);
		}
		
		winsRanking.updateRankingWithPlayers(Duels.plugin.dbHandler.loadTop10PlayersToWinsScoreboard());
		eloRanking.updateRankingWithPlayers(Duels.plugin.dbHandler.loadTop10PlayersToEloScoreScoreboard());
		
		timer.start(5, "");
	}
	
	
	
	private double getExpectedScore(int eloOwn, int eloOther) {
		return 1.0 / (1.0 + Math.pow(10, (eloOther - eloOwn) / 400.0));
	}

	
	
	public Gamemode getGamemode() {
		return gameMode;
	}

	public ArrayList<Location> getPlacedBlocks() {
		return buildedBlocks;
	}

	public boolean isFree() {
		return currentGameState == GameState.FREE;
	}

	public boolean isGameOn() {
		return currentGameState == GameState.PLAYING;
	}

	public void leaveGame(DuelsPlayer dp) {
		players.remove(dp);
		if(players.size() > 0) {
			gameEnd(players.get(0));
		}
	}

	public void startGame(List<DuelsPlayer> dplayers, Kit kit) {
		for (Entity entity : spawn1.getWorld().getEntities()) {
			EntityType eType = entity.getType();
			if (!eType.equals(EntityType.ARMOR_STAND)) {
				entity.remove();
			}
			if(eType.equals(EntityType.ARROW) && isWithinArena(entity.getLocation())) {
				entity.remove();
			}
		}
		
		currentGameState = GameState.STARTING;
		players.clear();
		for (DuelsPlayer dp : dplayers) {
			players.add(dp);
			Player p = dp.getPlayer();
			dp.setGameWhereJoined(this);
			timer.addPlayer(p);
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 0f);
			getSpawn(p);
			lobby.setKitItems(p, kit.getItems());
			lobby.clearPotionEffect(p);
			p.setScoreboard(dp.getSidebarHandler().getGameBoard());
			dp.getSidebarHandler().updateGameSidebar("1 vs 1");
		}
		timer.start(5, "Duelsin alkuun");
	}

	private void getSpawn(Player p) {
		for (int i = 0; i < players.size(); i++) {
			if (i % 2 == 0) {
				Bukkit.getScheduler().runTask(Duels.plugin, new Runnable() {
					@Override
					public void run() {
						p.teleport(spawn1, TeleportCause.PLUGIN);
					}
				});
			} else {
				Bukkit.getScheduler().runTask(Duels.plugin, new Runnable() {
					@Override
					public void run() {
						p.teleport(spawn2, TeleportCause.PLUGIN);
					}
				});
			}
		}
	}
	
	public boolean isWithinArena(Location loc) {
		if (!loc.getWorld().equals(this.arenaCenterLoc.getWorld())) {
			return false;
		}
		long halfArenaXWidht = this.arenaXWidth / 2;
		long halfArenaZWidht = this.arenaZWidth / 2;
		if (loc.getBlockX() < this.arenaCenterLoc.getBlockX() - halfArenaXWidht
				|| loc.getBlockX() > this.arenaCenterLoc.getBlockX() + halfArenaXWidht
				|| loc.getBlockZ() < this.arenaCenterLoc.getBlockZ() - halfArenaZWidht
				|| loc.getBlockZ() > this.arenaCenterLoc.getBlockZ() + halfArenaZWidht) {
			return false;
		}
		return true;
	}
	
	public boolean isUnderArena(Location loc) {
		if (!loc.getWorld().equals(this.arenaCenterLoc.getWorld())) {
			return false;
		}
		return loc.getBlockY() < this.arenaCenterLoc.getBlockY() - 4;
	}
	
	
	
	
	
	
	
	
	
	 /**
	    * Send a title to player
	    * @param player Player to send the title to
	    * @param text The text displayed in the title
	    * @param fadeInTime The time the title takes to fade in
	    * @param showTime The time the title is displayed
	    * @param fadeOutTime The time the title takes to fade out
	    * @param color The color of the title
	    */
	    public void sendTitle(Player player, String text, int fadeInTime, int showTime, int fadeOutTime, ChatColor color)
	    {
	        try
	        {
	            Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + text + "\",color:" + color.name().toLowerCase() + "}");

	            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
	            Object packet = titleConstructor.newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle, fadeInTime, showTime, fadeOutTime);

	            sendPacket(player, packet);
	        }

	        catch (Exception ex)
	        {
	            //Do something
	        }
	    }

	    private void sendPacket(Player player, Object packet)
	    {
	        try
	        {
	            Object handle = player.getClass().getMethod("getHandle").invoke(player);
	            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
	            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
	        }
	        catch(Exception ex)
	        {
	            //Do something
	        }
	    }

	    /**
	    * Get NMS class using reflection
	    * @param name Name of the class
	    * @return Class
	    */
	    private Class<?> getNMSClass(String name)
	    {
	        try
	        {
	            return Class.forName("net.minecraft.server" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	        }
	        catch(ClassNotFoundException ex)
	        {
	            //Do something
	        }
	        return null;
	    }
	
}
