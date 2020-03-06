package com.allu.duels;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.allu.duels.utils.Kit;

public final class Challenge {
	
	private DuelsPlayer challenger;
	private DuelsPlayer challenged;
	private List<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	
	private Kit kit;
	
	public Challenge(DuelsPlayer challenger, DuelsPlayer challenged, Kit kit) {
		this.challenger = challenger;
		this.challenged = challenged;
		this.players.add(challenger);
		this.players.add(challenged);
		this.kit = kit;
	}

	public Kit getKit() {
		return kit;
	}
	
	public DuelsPlayer getChallenger() {
		return challenger;
	}
	
	public List<DuelsPlayer> getDuelsPlayers() {
		return players;
	}
	
	public DuelsPlayer getChallenged() {
		return challenged;
	}
}
