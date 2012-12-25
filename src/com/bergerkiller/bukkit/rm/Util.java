package com.bergerkiller.bukkit.rm;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

import com.bergerkiller.bukkit.common.MaterialTypeProperty;
import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;

@SuppressWarnings("deprecation")
public class Util {
	public static final MaterialTypeProperty ISSOLID  = new MaterialTypeProperty(Material.STONE, Material.COBBLESTONE,  Material.GRASS, 
				Material.DIRT, Material.WOOD, Material.LOG, Material.NETHERRACK, Material.IRON_BLOCK, Material.GOLD_BLOCK, 
				Material.DIAMOND_BLOCK, Material.SAND, Material.SANDSTONE, Material.BEDROCK, Material.REDSTONE_ORE, Material.COAL_ORE,
				Material.DIAMOND_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.SMOOTH_BRICK, Material.BRICK, Material.CLAY, 
				Material.DOUBLE_STEP, Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.SPONGE, Material.SNOW, Material.HUGE_MUSHROOM_1, 
				Material.HUGE_MUSHROOM_2);

	/**
	 * Checks whether one block is attached to another
	 * 
	 * @param block that is attachable
	 * @param to block to check against
	 * @return True if the block is attached to the to block, False if not
	 */
	public static boolean isAttached(Block block, Block to) {
		return BlockUtil.equals(BlockUtil.getAttachedBlock(block), to);
	}

	/**
	 * Strips a name from all file-path unsupported characters
	 * @param name to fix
	 * @return Fixed name
	 */
	public static String fixName(String name) {
		StringBuilder newName = new StringBuilder(name.length());
		for (char c : name.toCharArray()) {
			if (LogicUtil.containsChar(c, '>', '<', '|', '^', '/', '\\', ':', '?', '"', '*')) {
				newName.append(' ');
			} else {
				newName.append(c);
			}
		}
		return newName.toString();
	}

	/**
	 * Toggles the powered state of a block to perform a certain action
	 * 
	 * @param mainblock to toggle
	 * @param toggled state to set to
	 */
	public static void setBlock(Block mainblock, boolean toggled) {
		if (mainblock != null) {
			for (BlockFace face : FaceUtil.ATTACHEDFACESDOWN) {
				Block b = mainblock.getRelative(face);
				Material type = b.getType();
				if (type == Material.LEVER) {
					if (Util.isAttached(b, mainblock)) {
						BlockUtil.setLever(b, toggled);
					}
				} else if (type == Material.NOTE_BLOCK) {
					if (toggled) {
						((NoteBlock) b.getState()).play();
					}
				} else if (MaterialUtil.ISPISTONBASE.get(type)) {
					//Util.setPiston(b, toggled);
				} else if (MaterialUtil.ISDOOR.get(type)) {
					Door door = (Door) type.getNewData(b.getData());
					if (toggled != door.isOpen()) {
						door.setOpen(toggled);
						Block above = b.getRelative(BlockFace.UP);
						Block below = b.getRelative(BlockFace.DOWN);
						if (MaterialUtil.ISDOOR.get(above)) {
							b.setData(door.getData(), true);
							door.setTopHalf(true);
							above.setData(door.getData(), true);
						} else if (MaterialUtil.ISDOOR.get(below)) {
							door.setTopHalf(false);
							below.setData(door.getData(), true);
							door.setTopHalf(true);
							b.setData(door.getData(), true);
						}
						b.getWorld().playEffect(b.getLocation(), Effect.DOOR_TOGGLE, 0);
					}
				} else if (type == Material.TRAP_DOOR) {
					TrapDoor td = (TrapDoor) type.getNewData(b.getData());
					if (td.isOpen() != toggled) {
						byte data = (byte) (td.getData() ^ 4);
						b.setData(data);
						b.getWorld().playEffect(b.getLocation(), Effect.DOOR_TOGGLE, 0);
					}
				}
			}
		}
	}
}