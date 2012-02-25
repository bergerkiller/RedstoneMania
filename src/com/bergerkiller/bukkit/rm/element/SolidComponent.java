package com.bergerkiller.bukkit.rm.element;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.rm.Util;

/**
 * Used during circuit creation to get the input and outputs of a solid block
 * IS NOT USED IN THE ACTUAL CIRCUITS!
 */
public class SolidComponent extends Redstone {
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
		for (BlockFace face : FaceUtil.attachedFaces) {
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
				BlockFace facing = BlockUtil.getFacing(b);
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
	
	private static boolean isDistractingColumn(Block main, BlockFace face) {
		Block side = main.getRelative(face);
		Material type = side.getType();
		if (Util.canDistractWire(type)) {
			return true;
		} else if (type == Material.AIR) {
			//check level below
			if (Util.canDistractWire(side.getRelative(BlockFace.DOWN).getType())) {
				return true;
			}
		} else if (type == Material.DIODE_BLOCK_ON || type == Material.DIODE_BLOCK_OFF) {
			//powered by repeater?
			BlockFace facing = BlockUtil.getFacing(side);
			return facing == face;
		}
		if (main.getRelative(BlockFace.UP).getType() == Material.AIR) {
			//check level on top
			return Util.canDistractWire(side.getRelative(BlockFace.UP).getType());
		} else {
			return false;
		}
	}
	private static boolean isDistracted(Block wire, BlockFace face) {
		if (face == BlockFace.NORTH) face = BlockFace.SOUTH;
		if (face == BlockFace.EAST) face = BlockFace.WEST; 
		BlockFace f1 = (face == BlockFace.SOUTH) ? BlockFace.WEST : BlockFace.SOUTH;
		BlockFace f2 = (face == BlockFace.SOUTH) ? BlockFace.EAST : BlockFace.NORTH;
		return isDistractingColumn(wire, f1) || isDistractingColumn(wire, f2);
	}
}
