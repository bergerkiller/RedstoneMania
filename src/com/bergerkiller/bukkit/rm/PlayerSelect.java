package com.bergerkiller.bukkit.rm;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.BlockMap;


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
	
	public BlockLocation selectedblock;
	public BlockMap<Integer> delays = new BlockMap<Integer>();
	public HashMap<String, BlockLocation> portnames = new HashMap<String, BlockLocation>();
	public int clickdelay = -1;
	
	public void setDelay(int delay) {
		this.delays.put(this.selectedblock, delay);
	}
	public void setPort(String name) {
		this.portnames.put(name, this.selectedblock);
	}
	public void clearPorts() {
		this.portnames.clear();
	}
	public void clearDelays() {
		this.delays.clear();
	}

	public void set(Location l) {
		this.set(l.getBlock());
	}
	public void set(Block b) {
		this.selectedblock = new BlockLocation(b);
	}
	public boolean setDelay() {
		if (this.clickdelay >= 0 && this.isDelayable()) {
			this.setDelay(this.clickdelay);
			return true;
		} else {
			return false;
		}
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
