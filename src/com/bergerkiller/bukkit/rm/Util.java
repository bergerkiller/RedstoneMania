package com.bergerkiller.bukkit.rm;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;

public class Util {
	
    public static void setPiston(Block piston, boolean stretched) {
    	//TODO GET PISTON WORKING
//    	PistonBaseMaterial mat = (PistonBaseMaterial) Material.PISTON_BASE.getNewData(piston.getData());
//    	mat.setPowered(stretched);
//    	piston.setData(mat.getData(), true);
//    	net.minecraft.server.World world = ((CraftWorld) piston.getWorld()).getHandle();
//    	try {
//			Method m = BlockPiston.class.getMethod("g", net.minecraft.server.World.class, Integer.class, Integer.class, Integer.class);
//            m.setAccessible(true);
//			m.invoke(null, world, piston.getX(), piston.getY(), piston.getZ());
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }
    
    public static boolean isDoor(Material type) {
    	return type == Material.WOODEN_DOOR || type == Material.IRON_DOOR;
    }
    public static boolean isLeverDown(Block lever) {
    	byte dat = lever.getData();
    	return dat == (dat | 0x8);
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
    public static boolean isRedstoneTorch(Block block) {
    	return isRedstoneTorch(block.getType());
    }
    public static boolean isRedstoneTorch(Material material) {
    	return material == Material.REDSTONE_TORCH_OFF || material == Material.REDSTONE_TORCH_ON;
    }
    public static boolean isDiode(Block block) {
    	return isDiode(block.getType());
    }
    public static boolean isDiode(Material material) {
    	return material == Material.DIODE_BLOCK_OFF || material == Material.DIODE_BLOCK_ON;
    }
    public static boolean canDistractWire(Material type) {
		switch (type) {
		case REDSTONE_WIRE : return true;
		case REDSTONE_TORCH_ON : return true;
		case REDSTONE_TORCH_OFF : return true;
		case LEVER : return true;
		case WOOD_PLATE : return true;
		case STONE_PLATE : return true;
		case STONE_BUTTON : return true;
		case DETECTOR_RAIL : return true;
		}
		return false;
    }

    public static String fixName(String name) {
    	for (int i = 0; i < name.length(); i++) {
    		char c = name.charAt(i);
    		if (c == '>' || c == '<' || c == '|' || c == '^' || c == '/'
    				|| c == '\\' || c == ':' || c == '?' || c == '"' || c == '*') {
    			name = name.substring(0, i) + " " + name.substring(i + 1);
    		}
    	}
    	return name;
    }
    
    public static boolean isSolid(Material type) {
    	switch(type) {
    	case STONE : return true;
    	case COBBLESTONE : return true;
    	case GRASS : return true;
    	case DIRT : return true;
    	case WOOD : return true;
    	case LOG : return true;
    	case NETHERRACK : return true;
    	case IRON_BLOCK : return true;
    	case GOLD_BLOCK : return true;
    	case DIAMOND_BLOCK : return true;
    	case SAND : return true;
    	case SANDSTONE : return true;
    	case BEDROCK : return true;
    	case REDSTONE_ORE : return true;
    	case COAL_ORE : return true;
    	case DIAMOND_ORE : return true;
    	case IRON_ORE : return true;
    	case GOLD_ORE : return true;
    	case SMOOTH_BRICK : return true;
    	case BRICK : return true;
    	case CLAY : return true;
    	case DOUBLE_STEP : return true;
    	case LAPIS_BLOCK : return true;
    	case LAPIS_ORE : return true;
      	case SPONGE : return true;
      	case SNOW : return true;
      	case HUGE_MUSHROOM_1 : return true;
      	case HUGE_MUSHROOM_2 : return true;
    	}
    	return false;
    }

    public static boolean equals(Block b1, Block b2) {
    	if (b1.getX() == b2.getX()) {
        	if (b1.getY() == b2.getY()) {
            	if (b1.getZ() == b2.getZ()) {
            		if (b1.getWorld() == b2.getWorld()) {
            			return true;
            		}
            	}
        	}
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
    			if (i == 0) rval |= 0x0;
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
		        } else if (type == Material.PISTON_STICKY_BASE || type == Material.PISTON_BASE) {
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
    	return (value & (1<<n)) != 0;
    }
    
    public static String getIndent(int n) {
    	String rval = "";
    	for (int i = 0; i < n; i++) rval += "    ";
    	return rval;
    }
    
    public static void logElements(String prestring, String delimiter, int maxlinelength, Object... elements) {
    	for (String line : listElements(prestring, delimiter, maxlinelength, elements)) {
    		RedstoneMania.plugin.log(Level.INFO, line);
    	}
    }
    public static void listElements(CommandSender player, String prestring, String delimiter, int maxlinelength, Object... elements) {
    	for (String line : listElements(prestring, delimiter, maxlinelength, elements)) {
    		player.sendMessage(line);
    	}
    }
    public static String[] listElements(String prestring, String delimiter, int maxlinelength, Object... elements) {
    	ArrayList<String> rval = new ArrayList<String>();
		String msgpart = prestring;
		for (Object element : elements) {
			//display it
			String name = ChatColor.YELLOW + element.toString();
			if (msgpart.length() + name.length() < maxlinelength) {
				if (msgpart.length() != prestring.length()) msgpart += ChatColor.WHITE + delimiter;
				msgpart += element;
			} else {
				rval.add(msgpart);
				msgpart = name;
			}
		}
		//possibly forgot one?
		rval.add(msgpart);
		return rval.toArray(new String[0]);
    }
}