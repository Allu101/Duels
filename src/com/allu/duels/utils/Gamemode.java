package com.allu.duels.utils;

public enum Gamemode {
    DUELS_1V1("1v1", 1),
    DUELS_2V2("2v2", 2),
    DUELS_4V4("4v4", 4),
    DEFAULT("1v1", 1);
	
    private int teamSize;
    private String value;
   
    Gamemode(String value, int teamSize) {
        this.value = value;
        this.teamSize = teamSize;
    }
   
    public String getString() {
        return this.value;
    }
   
    public int getTeamSize() {
        return this.teamSize;
    }

}
