package com.allu.duels;


import java.util.ArrayList;
import java.util.List;
import com.allu.duels.utils.Kit;

public final class Challenge {
	
	static long nextChallengeID = 1000;
	
	private DuelsPlayer challenger;
	private DuelsPlayer challenged;
	private List<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private long challengeSendTime;
	
	private long challengeID;
	
	private Kit kit;
	
	public Challenge(DuelsPlayer challenger, DuelsPlayer challenged, Kit kit) {
		this.challenger = challenger;
		this.challenged = challenged;
		this.players.add(challenger);
		this.players.add(challenged);
		this.kit = kit;
		
		this.challengeSendTime = System.currentTimeMillis();
		
		Challenge.nextChallengeID ++;
		this.challengeID = Challenge.nextChallengeID;
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
