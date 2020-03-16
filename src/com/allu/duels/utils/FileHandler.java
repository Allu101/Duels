package com.allu.duels.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

public class FileHandler {
	
	public static String MainFilePath = "plugins//Duels//";
	private static File file = new File(MainFilePath + "kitStats.yml");
	
	public FileHandler() {
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void increaseKitPlayedCount(String kitName) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		int playedCount;
		if(cfg.contains(kitName)) {
			playedCount = cfg.getInt(kitName);
			playedCount++;
		} else {
			playedCount = 1;
		}
		cfg.set(kitName, playedCount);
		try {
			cfg.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
