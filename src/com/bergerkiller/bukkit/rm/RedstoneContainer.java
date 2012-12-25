package com.bergerkiller.bukkit.rm;

import com.bergerkiller.bukkit.rm.element.Redstone;

public class RedstoneContainer {
	public Redstone value;
	private RedstoneMap map;

	public RedstoneContainer(RedstoneMap map) {
		this.map = map;
	}

	/**
	 * Sets the Redstone value
	 * 
	 * @param value to set to
	 * @return The input Value
	 */
	public <T extends Redstone> T setValue(T value) {
		this.value = value;
		this.map.setValue(this, value);
		return value;
	}
}
