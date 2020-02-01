package com.allu.duels.utils;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.allu.duels.DuelsPlayer;

public final class ChallengeCreatedEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	private DuelsPlayer challengerDp;
	private List<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	
	private Kit kit;
	
	public ChallengeCreatedEvent(DuelsPlayer challenger, DuelsPlayer opponent, Kit kit) {
		this.challengerDp = challenger;
		this.players.add(challenger);
		this.players.add(opponent);
		this.kit = kit;
	}
	
	public Kit getKit() {
		return kit;
	}
	
	public DuelsPlayer getChallengerDp() {
		return challengerDp;
	}
	
	public List<DuelsPlayer> getDuelsPlayers() {
		return players;
	}
	
	public HandlerList getHandlers() {
        return handlers;
    }
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

}
