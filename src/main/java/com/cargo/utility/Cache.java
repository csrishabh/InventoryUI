package com.cargo.utility;

import java.util.HashMap;
import java.util.Map;

public class Cache {

	private static Cache cache;
	private Map<String, String> map = new HashMap<>();

	private Cache() {

	}

	public static synchronized Cache getCache() {

		if (cache == null) {
			cache = new Cache();
		}
		return cache;

	}

	public String get(String key) {

		return map.get(key);
	}

	public void add(String key, String value) {

		map.put(key, value);
	}

}
