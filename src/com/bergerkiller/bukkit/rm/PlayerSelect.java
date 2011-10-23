package com.bergerkiller.bukkit.rm;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;


public class PlayerSelect {
	private static HashMap<String, PlayerSelect> selections = new HashMap<String, PlayerSelect>();
	public static PlayerSelect get(Player player) {
		PlayerSelect ps = selections.get(player.getName());
		if (ps == null) {
			ps = new PlayerSelect();
			selections.put(player.getName(), ps);
		}
		return ps;
	}
	
	public Position selectedblock;
	public HashMap<Position, Integer> delays = new HashMap<Position, Integer>();
	public HashMap<Position, String> portnames = new HashMap<Position, String>();
	
	public void setDelay(int delay) {
		delays.put(selectedblock, delay);
	}
	public void setPort(String name) {
		portnames.put(selectedblock, name);
	}
	public void clear() {
		delays.clear();
	}

	public void set(Location l) {
		this.set(l.getBlock());
	}
	public void set(Block b) {
		this.selectedblock = new Position();
		this.selectedblock.worldname = b.getWorld().getName();
		this.selectedblock.x = b.getX();
		this.selectedblock.y = b.getY();
		this.selectedblock.z = b.getZ();
	}
	
	public Block getBlock() {
		if (this.selectedblock == null) return null;
		return this.selectedblock.getBlock();
	}
	public Material getType() {
		Block b = getBlock();
		if (b == null) return Material.AIR;
		return b.getType();
	}
	public boolean isType(Material... types) {
		Material type = getType();
		for (Material t : types) {
			if (type == t) return true;
		}
		return false;
	}
	public boolean isRedstone() {
		return isType(Material.REDSTONE_WIRE);
	}
	public boolean isTorch() {
		return isType(Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON);
	}
	public boolean isRepeater() {
		return isType(Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON);
	}
	public boolean isDelayable() {
		return isTorch() || isRepeater();
	}
	
}
