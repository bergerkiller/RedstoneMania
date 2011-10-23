package com.bergerkiller.bukkit.rm;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.bergerkiller.bukkit.rm.element.PhysicalPort;

public class RMWorldListener extends WorldListener {
	
	public void onChunkLoad(ChunkLoadEvent event) {
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.isIn(event.getChunk())) {
				p.setActive(true);
			}
		}
	}
	
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled()) return;
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.isIn(event.getChunk())) {
				p.setActive(false);
			}
		}
	}
	
	public void onWorldLoad(WorldLoadEvent event) {
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.isIn(event.getWorld())) {
				p.updateActive();
			}
		}
	}
	
	public void onWorldUnload(WorldUnloadEvent event) {
		if (event.isCancelled()) return;
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.isIn(event.getWorld())) {
				p.setActive(false);
			}
		}
	}
	
}
