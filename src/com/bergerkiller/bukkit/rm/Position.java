package com.bergerkiller.bukkit.rm;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Position {
	
	public String worldname;
	public int x, y, z;
	
	public int cx() {
		return this.x >> 4;
	}
	public int cz() {
		return this.z >> 4;
	}
	public World getWorld() {
		return Bukkit.getServer().getWorld(this.worldname);
	}
	public Block getBlock() {
		World w = this.getWorld();
		if (w == null) return null;
		return w.getBlockAt(this.x, this.y, this.z);
	}
	
	public boolean isIn(World world) {
		return this.worldname.equalsIgnoreCase(world.getName());
	}
	public boolean isIn(Chunk chunk) {
		return this.isIn(chunk.getWorld()) && this.cx() == chunk.getX() && this.cz() == chunk.getZ();
	}
	public boolean isVisible() {
		World w = this.getWorld();
		if (w == null) return false;
		return w.isChunkLoaded(this.cx(), this.cz());
	}
	
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + ((this.worldname != null) ? this.worldname.hashCode() : 0);
        hash = 53 * hash + (this.x ^ (this.x >> 16));
        hash = 53 * hash + (this.y ^ (this.y >> 16));
        hash = 53 * hash + (this.z ^ (this.z >> 16));
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
    	if (object == this) return true;
    	if (object instanceof Position) {
    		Position pos = (Position) object;
    		return pos.x == this.x && pos.y == this.y && pos.z == this.z && pos.worldname.equals(this.worldname);
    	} else if (object instanceof Block) {
    		Block b = (Block) object;
    		return b.getX() == this.x && b.getY() == this.y && b.getZ() == this.z && b.getWorld().getName().equals(this.worldname);
    	}
    	return false;
    }

    public Position() {}
    public Position(Block b) {
    	this.x = b.getX();
    	this.y = b.getY();
    	this.z = b.getZ();
    	this.worldname = b.getWorld().getName();
    }
	public Position(String worldname, int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.worldname = worldname;
	}
    
}
