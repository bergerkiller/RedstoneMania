package com.bergerkiller.bukkit.rm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.bergerkiller.bukkit.rm.element.Port;
import com.bergerkiller.bukkit.rm.element.Redstone;

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
	
	private static boolean isDistracting(Material type) {
		return type == Material.REDSTONE_WIRE || type == Material.REDSTONE_TORCH_ON || type == Material.REDSTONE_TORCH_OFF;
	}
	private static boolean isDistractingColumn(Block main, BlockFace face) {
		Block side = main.getRelative(face);
		Material type = side.getType();
		if (isDistracting(type)) {
			return true;
		} else if (type == Material.AIR) {
			//check level below
			if (isDistracting(side.getRelative(BlockFace.DOWN).getType())) {
				return true;
			}
		} else if (type == Material.DIODE_BLOCK_ON || type == Material.DIODE_BLOCK_OFF) {
			//powered by repeater?
			BlockFace facing = Util.getFacing(side, Material.DIODE_BLOCK_ON);
			return facing == face;
		}
		//check level on top
		return isDistracting(side.getRelative(BlockFace.UP).getType());
	}
	private static boolean isDistracted(Block wire, BlockFace face) {
		if (face == BlockFace.NORTH) face = BlockFace.SOUTH;
		if (face == BlockFace.EAST) face = BlockFace.WEST; 
		BlockFace f1 = (face == BlockFace.SOUTH) ? BlockFace.WEST : BlockFace.SOUTH;
		BlockFace f2 = (face == BlockFace.SOUTH) ? BlockFace.EAST : BlockFace.NORTH;
		return isDistractingColumn(wire, f1) || isDistractingColumn(wire, f2);
	}
	
	public static class SolidComponent extends Redstone {
		/*
		 * inputs: torches, wires and repeaters supplying power to outputs and wires
		 * outputs: torches, wires and repeaters receiving input from this solid block
		 */
		public ArrayList<Block> inputs = new ArrayList<Block>();
		public ArrayList<Block> outputs = new ArrayList<Block>();
		
		public boolean hasInput(Block block) {
			for (Block b : inputs) {
				if (Util.equals(b, block)) return true;
			}
			return false;
		}
		
		public SolidComponent(Block block) {
			//initialize the blocks
			//Is the block below a redstone torch or wire?
			Block below = block.getRelative(BlockFace.DOWN);
			Material belowtype = below.getType();
			if (belowtype == Material.REDSTONE_TORCH_ON || belowtype == Material.REDSTONE_TORCH_OFF) {
				inputs.add(below);
			} else if (belowtype == Material.REDSTONE_WIRE) {
				outputs.add(below);
			}
			//Check all sides and up for torches
			for (BlockFace face : Util.dome) {
				Block b = block.getRelative(face);
				Material type = b.getType();
				if (type == Material.REDSTONE_TORCH_ON || type == Material.REDSTONE_TORCH_OFF) {
					if (Util.isAttached(b, block)) {
						//we found an attached torch
					    outputs.add(b);
					}
				} else if (type == Material.REDSTONE_WIRE) {
					if (face == BlockFace.UP) {
						//wire ontop - acts as input
						inputs.add(b);
						outputs.add(b);
					} else {
						//pointing towards the block or not?
						outputs.add(b);
						if (!isDistracted(b, face)) {
							inputs.add(b);
						}
					}
				} else if (type == Material.DIODE_BLOCK_ON || type == Material.DIODE_BLOCK_OFF) {
					BlockFace facing = Util.getFacing(b, type);
					//supplying or receiving, or none?
					if (facing == face) {
						//receiving
						outputs.add(b);
					} else if (facing.getOppositeFace() == face) {
						//supplying
						inputs.add(b);
					}
				}
			}
		}
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
