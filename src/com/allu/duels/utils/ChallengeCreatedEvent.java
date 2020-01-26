package com.allu.duels.utils;


import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class ChallengeCreatedEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	private Player opponent;
	private Kit kit;
	
	public ChallengeCreatedEvent(Player challenger, Player opponent, Kit kit) {
		this.player = challenger;
		this.opponent = opponent;
		this.kit = kit;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Player getOpponent() {
		return opponent;
	}
	
	public Kit getKit() {
		return kit;
	}
	
	public HandlerList getHandlers() {
        return handlers;
    }
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

}
