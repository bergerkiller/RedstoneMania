package com.bergerkiller.bukkit.rm.element;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;
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
			if (BlockUtil.equals(b, block)) return true;
		}
		return false;
	}
	
	public SolidComponent(Block block) {
		//initialize the blocks
		//Is the block below a redstone torch or wire?
		Block below = block.getRelative(BlockFace.DOWN);
		Material belowtype = below.getType();
		if (MaterialUtil.ISREDSTONETORCH.get(belowtype)) {
			inputs.add(below);
		} else if (belowtype == Material.REDSTONE_WIRE) {
			outputs.add(below);
		}
		//Check all sides and up for torches
		for (BlockFace face : FaceUtil.ATTACHEDFACES) {
			Block b = block.getRelative(face);
			Material type = b.getType();
			if (MaterialUtil.ISREDSTONETORCH.get(type)) {
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
			} else if (MaterialUtil.ISDIODE.get(type)) {
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
		if (MaterialUtil.ISPOWERSOURCE.get(type)) {
			return true;
		} else if (type == Material.AIR) {
			//check level below
			if (MaterialUtil.ISPOWERSOURCE.get(side.getRelative(BlockFace.DOWN))) {
				return true;
			}
		} else if (MaterialUtil.ISDIODE.get(type)) {
			//powered by repeater?
			BlockFace facing = BlockUtil.getFacing(side);
			return facing == face;
		}
		if (main.getRelative(BlockFace.UP).getType() == Material.AIR) {
			//check level on top
			return MaterialUtil.ISPOWERSOURCE.get(side.getRelative(BlockFace.UP));
		} else {
			return false;
		}
	}
	private static boolean isDistracted(Block wire, BlockFace face) {
		return isDistractingColumn(wire, FaceUtil.rotate(face, -2)) || isDistractingColumn(wire, FaceUtil.rotate(face, 2));
	}
}
