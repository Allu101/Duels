package com.allu.duels;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class Arena {

	private int arenaXWidth = 50;
	private int arenaZWidth = 80;
	private Location arenaCenterLoc, spawn1, spawn2;
	
	private String arenaName;
	
	private List<Location> buildedBlocks = new ArrayList<Location>();
	
	
	public Arena(String arenaName, Location arenaCenterLoc, int spawnDistance) {
		this.arenaCenterLoc = arenaCenterLoc;
		
		this.spawn1 = arenaCenterLoc.clone().add(0, 0, -spawnDistance);
		this.spawn2 = arenaCenterLoc.clone().add(0, 0, spawnDistance);

		spawn2.setYaw(-180);
		this.arenaName = arenaName;
	}
	
	public void clearArrows() {
		Bukkit.getScheduler().runTaskLater(Duels.plugin, new Runnable() {
			@Override
			public void run() {
				List<Entity> entities = arenaCenterLoc.getWorld().getEntities();
				for (Entity entity : entities) {
					EntityType eType = entity.getType();
					if(eType.equals(EntityType.ARROW) && isWithinArena(entity.getLocation())) {
						entity.remove();
					}
				}
			}
		}, 1);
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
	
	public boolean isNearArenaFloorLevel(double y) {
		return Math.abs(y - this.arenaCenterLoc.getY()) < 2.5;
	}
	
	public boolean isUnderArena(Location loc) {
		if (!loc.getWorld().equals(this.arenaCenterLoc.getWorld())) {
			return false;
		}
		return loc.getBlockY() < this.arenaCenterLoc.getBlockY() - 4;
	}
	
	public String getArenaName() {
		return this.arenaName;
	}
	
	public List<Location> getPlacedBlocks() {
		return buildedBlocks;
	}
	
	public Location getSpawn(int i) {
		if (i % 2 == 0) return spawn1;
		return spawn2;
	}
}
