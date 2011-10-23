package com.bergerkiller.bukkit.rm.element;

public class Inverter extends Redstone {
	
	public boolean hasPower() {
		return !this.isPowered();
	}
	
	public byte getType() {
		return 1;
	}

}
