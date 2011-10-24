package com.bergerkiller.bukkit.rm;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

public class Util {
	
	public static final BlockFace[] bowl = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN};
	public static final BlockFace[] dome = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP};
	public static final BlockFace[] radial = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
	public static final BlockFace[] diamond = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP};
	
	public static BlockFace getFacing(Block b) {
		return getFacing(b, b.getType());
	}
	public static BlockFace getFacing(Block b, Material type) {
		MaterialData data = type.getNewData(b.getData());
		if (data != null && data instanceof Directional) {
			return ((Directional) data).getFacing();
		} else {
			return BlockFace.NORTH;
		}
	}
    public static Block getAttachedBlock(Block b) {
    	if (b == null) return null;
    	MaterialData m = b.getState().getData();
    	BlockFace face = BlockFace.DOWN;
    	if (m instanceof Attachable) {
    		face = ((Attachable) m).getAttachedFace();
    	}
    	return b.getRelative(face);
    }
    public static void setLever(Block lever, boolean down) {
		byte data = lever.getData();
        int newData;
        if (down) {
        	newData = data | 0x8;
        } else {
        	newData = data & 0x7;
        }
        if (newData != data) {
            lever.setData((byte) newData, true);
        }
    }
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
    	Block c = getAttachedBlock(block);
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
    
    public static String[] remove(String[] input, int index) {
    	String[] rval = new String[input.length - 1];
    	int i = 0;
    	for (int ii = 0; ii < input.length; ii++) {
    		if (ii != index) {
    			rval[i] = input[ii];
    			i++;
    		}
    	}
    	return rval;
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
    
    public static void msg(String msg) {
    	for (Player p : Bukkit.getServer().getOnlinePlayers()) {
    		p.sendMessage(msg);
    	}
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
    		RedstoneMania.log(Level.INFO, line);
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