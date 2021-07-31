package com.allu.duels;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.allu.duels.utils.Kit;

public final class Challenge {
	
	static long nextChallengeID = 1000;
	
	private DuelsPlayer challenger;
	private List<DuelsPlayer> challengedPlayers;
	private List<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private Map<String, Boolean> acceptPlayers = new HashMap<>();

	private long challengeSendTime;
	private long challengeID;
	
	private Kit kit;
	
	public Challenge(DuelsPlayer challenger, List<DuelsPlayer> challengedPlayers, Kit kit) {
		this.challenger = challenger;
		this.challengedPlayers = challengedPlayers;
		this.players.add(challenger);
		this.players.addAll(challengedPlayers);
		this.kit = kit;
		
		this.challengeSendTime = System.currentTimeMillis();
		
		Challenge.nextChallengeID ++;
		this.challengeID = Challenge.nextChallengeID;

		challengedPlayers.forEach(cdp -> acceptPlayers.put(cdp.getPlayer().getUniqueId().toString(), false));
	}

	public void acceptChallenge(String uuid) {
		acceptPlayers.put(uuid, true);
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
	
	public List<DuelsPlayer> getChallengedPlayers() {
		return challengedPlayers;
	}

	public boolean hasAllAccept() {
		return !acceptPlayers.containsValue(false);
	}

	public boolean hasPlayer(DuelsPlayer player) {
		return this.players.contains(player);
	}
	
	public long getChallengeSendTime() {
		return this.challengeSendTime;
	}
	
	public long getChallengeID() {
		return this.challengeID;
	}
}
