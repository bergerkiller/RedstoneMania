package com.bergerkiller.bukkit.rm;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;

public class Util {

	public static void setPiston(Block piston, boolean stretched) {
		// TODO GET PISTON WORKING
		// PistonBaseMaterial mat = (PistonBaseMaterial)
		// Material.PISTON_BASE.getNewData(piston.getData());
		// mat.setPowered(stretched);
		// piston.setData(mat.getData(), true);
		// net.minecraft.server.World world = ((CraftWorld)
		// piston.getWorld()).getHandle();
		// try {
		// Method m = BlockPiston.class.getMethod("g",
		// net.minecraft.server.World.class, Integer.class, Integer.class,
		// Integer.class);
		// m.setAccessible(true);
		// m.invoke(null, world, piston.getX(), piston.getY(), piston.getZ());
		// } catch (SecurityException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (NoSuchMethodException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public static boolean isDoor(Material type) {
		return type == Material.WOODEN_DOOR || type == Material.IRON_DOOR;
	}

	public static boolean isAttached(Block block, Block to) {
		Block c = BlockUtil.getAttachedBlock(block);
		if (c.getX() == to.getX()) {
			if (c.getY() == to.getY()) {
				if (c.getZ() == to.getZ()) {
					return true;
				}
			}
		}
		return false;
	}

	public static String fixName(String name) {
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '>' || c == '<' || c == '|' || c == '^' || c == '/' || c == '\\' || c == ':' || c == '?' || c == '"' || c == '*') {
				name = name.substring(0, i) + " " + name.substring(i + 1);
			}
		}
		return name;
	}

	public static boolean isSolid(Material type) {
		switch (type) {
			case STONE:
				return true;
			case COBBLESTONE:
				return true;
			case GRASS:
				return true;
			case DIRT:
				return true;
			case WOOD:
				return true;
			case LOG:
				return true;
			case NETHERRACK:
				return true;
			case IRON_BLOCK:
				return true;
			case GOLD_BLOCK:
				return true;
			case DIAMOND_BLOCK:
				return true;
			case SAND:
				return true;
			case SANDSTONE:
				return true;
			case BEDROCK:
				return true;
			case REDSTONE_ORE:
				return true;
			case COAL_ORE:
				return true;
			case DIAMOND_ORE:
				return true;
			case IRON_ORE:
				return true;
			case GOLD_ORE:
				return true;
			case SMOOTH_BRICK:
				return true;
			case BRICK:
				return true;
			case CLAY:
				return true;
			case DOUBLE_STEP:
				return true;
			case LAPIS_BLOCK:
				return true;
			case LAPIS_ORE:
				return true;
			case SPONGE:
				return true;
			case SNOW:
				return true;
			case HUGE_MUSHROOM_1:
				return true;
			case HUGE_MUSHROOM_2:
				return true;
		}
		return false;
	}

	public static byte combine(boolean... bools) {
		if (bools.length > 8) {
			return 0;
		}

		byte rval = 0;
		for (int i = 0; i < bools.length; i++) {
			if (bools[i]) {
				if (i == 0)
					rval |= 0x0;
			}
		}
		rval += 0x7;
		return rval;
	}

	public static void setBlock(Block mainblock, boolean toggled) {
		if (mainblock != null) {
			for (BlockFace face : FaceUtil.attachedFacesDown) {
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
					Util.setPiston(b, toggled);
				} else if (Util.isDoor(type)) {
					Door door = (Door) type.getNewData(b.getData());
					if (toggled != door.isOpen()) {
						door.setOpen(toggled);
						Block above = b.getRelative(BlockFace.UP);
						Block below = b.getRelative(BlockFace.DOWN);
						if (Util.isDoor(above.getType())) {
							b.setData(door.getData(), true);
							door.setTopHalf(true);
							above.setData(door.getData(), true);
						} else if (Util.isDoor(below.getType())) {
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

	public static boolean isBitSet(byte value, int n) {
		return (value & (1 << n)) != 0;
	}
}