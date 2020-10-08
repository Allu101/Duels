package com.allu.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiCheatHandler {

	private Map<String, List<Long>> hitTimes = new HashMap<>();

	public boolean isTooHighCps(String damagerName) {
		List<Long> times = hitTimes.get(damagerName);
		times.add(System.currentTimeMillis());
		times.removeIf(time -> System.currentTimeMillis() - time > 1000);
		if (times.size() > 13) {
			return true;
		}
		return false;
	}

	public void onPlayerJoin(String playerName) {
		hitTimes.put(playerName, new ArrayList<>());
	}

	public void onPlayerLeave(String playerName) {
		hitTimes.remove(playerName);
	}

}
