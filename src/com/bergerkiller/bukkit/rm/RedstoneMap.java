package com.bergerkiller.bukkit.rm;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.bergerkiller.bukkit.rm.element.Port;
import com.bergerkiller.bukkit.rm.element.Redstone;
import com.bergerkiller.bukkit.rm.element.SolidComponent;

public class RedstoneMap {
	
	public IntMap get(Block block) {
		return get(block.getWorld(), block.getX(), block.getY(), block.getZ());
	}
	public IntMap get(World world, int x, int y, int z) {
		return get(world).get(x).get(y).get(z);
	}
	
	private IntMap get(World world) {
		IntMap m = worlds.get(world);
		if (m == null) {
			m = new IntMap();
			worlds.put(world, m);
		}
		return m;
	}
	
	private HashMap<World, IntMap> worlds = new HashMap<World, IntMap>();
	private HashMap<Redstone, HashSet<IntMap>> maps = new HashMap<Redstone, HashSet<IntMap>>();
	public HashSet<IntMap> getMaps(Redstone redstone) {
		HashSet<IntMap> map = maps.get(redstone);
		if (map == null) {
			map = new HashSet<IntMap>();
			maps.put(redstone, map);
		}
		return map;
	}
	public void merge(Redstone from, Redstone to) {
		HashSet<IntMap> rmaps = getMaps(from);
		for (IntMap map : rmaps) {
			setValue(map, to);
		}
		maps.remove(from);
	}
	
	
	public void setValue(IntMap map, Redstone value) {
		map.value = value;
		getMaps(value).add(map);
	}
	
	public class IntMap {
		public Redstone value;
		public Port port() {
			return (Port) value;
		}
		public SolidComponent comp() {
			return (SolidComponent) value;
		}
		private HashMap<Integer, IntMap> coords = new HashMap<Integer, IntMap>();
		public IntMap get(int value) {
			IntMap m = coords.get(value);
			if (m == null) {
				m = new IntMap();
				coords.put(value, m);
			}
			return m;
		}
		
	}

}
