package com.bergerkiller.bukkit.rm.element;

import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.BlockMap;
import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;
import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.Util;
import com.bergerkiller.bukkit.rm.circuit.CircuitInstance;

public class PhysicalPort {
	private static BlockMap<PhysicalPort> ports = new BlockMap<PhysicalPort>();
	private static BlockLocation getPostion(Block at) {
		Material type = at.getType();
		if (type == Material.LEVER || MaterialUtil.ISSIGN.get(type)) {
			at = BlockUtil.getAttachedBlock(at);
		}
		return new BlockLocation(at);
	}
	public static PhysicalPort get(Block at) {
		return ports.get(getPostion(at));
	}
	private static PhysicalPort remove(BlockLocation at) {
		PhysicalPort p = ports.remove(at);
		if (p == null) return null;
		if (p.port.locations.remove(p)) {
			if (p.port.locations.size() == 0) {
				((CircuitInstance) p.port.getCircuit()).updateAlive();
			}
			p.port.updateLeverPower();
		}
		return p;
	}
	public static PhysicalPort remove(Block at) {
		return remove(getPostion(at));
	}
	public static boolean remove(PhysicalPort port) {
		if (port == null) return false;
		return remove(port.position) != null;
	}
	public static Collection<PhysicalPort> getAll() {
		return ports.values();
	}
	public static PhysicalPort add(Port port, BlockLocation at) {
		return new PhysicalPort(port, at);
	}
	public static void clearAll() {
		ports.clear();
	}
	
	private static class Update implements Runnable {
		public Update(PhysicalPort pp) {
			this.pp = pp;
		}
		public Update(Block at) {
			this.b = at;
		}
		private Block b;
		private PhysicalPort pp;
		public void run() {
			if (this.pp == null) {
				this.pp = get(this.b);
			}
			if (this.pp != null) {
				this.pp.updateLeverPower();
				this.pp.setLevers();
			}
		}
	}
	
	public static void updateLevers(Block at, boolean delayed) {
		Update u = new Update(at);
		if (delayed) {
			RedstoneMania.plugin.getServer().getScheduler().scheduleSyncDelayedTask(RedstoneMania.plugin, u, 0);
		} else {
			u.run();
		}
	}
	public void updateLevers(boolean delayed) {
		Update u = new Update(this);
		if (delayed) {
			RedstoneMania.plugin.getServer().getScheduler().scheduleSyncDelayedTask(RedstoneMania.plugin, u, 0);
		} else {
			u.run();
		}
	}
	
	public BlockLocation position;
	public Port port;
	private Block mainblock = null;
		
	public PhysicalPort(Port port, BlockLocation at) {
		this.port = port;
		this.position = at;
		ports.put(this.position, this);
		this.port.locations.add(this);
		this.updateActive();
		this.updateLevers(false);
	}
	
	private class ExistenceCheck implements Runnable {
		public ExistenceCheck(PhysicalPort port) {
			this.p = port;
		}
		private PhysicalPort p;
		public void run() {
			//is this main block even a valid block?!
			for (BlockFace face : FaceUtil.attachedFaces) {
				Block b = p.mainblock.getRelative(face);
				Material type = b.getType();
				if (type == Material.SIGN_POST || type == Material.WALL_SIGN) {
					if (Util.isAttached(b, p.mainblock)) {
						return;
					}
				}
			}
			//Not found - remove it
			StringBuilder builder = new StringBuilder();
			builder.append("Auto-removed physical port [");
			builder.append(p.mainblock.getX() + "/" + p.mainblock.getY() + "/" + p.mainblock.getZ());
			builder.append("] for port '" + p.port.name + "' in circuit instance '" + p.port.getCircuit().name);
			RedstoneMania.plugin.log(Level.WARNING, builder.toString());
			remove(p.mainblock);
		}
	}
	
	public void updateActive() {
		this.setActive(this.position.isLoaded());
	}
	public void setActive(boolean active) {
		if ((this.mainblock != null) != active) {
			if (active) {
				this.mainblock = this.position.getBlock();
				if (this.mainblock == null || this.mainblock.getWorld() == null) return;
				this.setLevers();
				RedstoneMania.plugin.getServer().getScheduler().scheduleSyncDelayedTask(RedstoneMania.plugin, new ExistenceCheck(this), 0);
			} else {
				this.mainblock = null;
			}
		}
	}
	
	public void updateLeverPower() {
		this.updateLeverPower(true);
	}
	public void updateLeverPower(boolean setport) {
		if (this.mainblock == null) return;
		for (BlockFace face : FaceUtil.attachedFaces) {
			Block lever = this.mainblock.getRelative(face);
			if (lever.getType() == Material.LEVER) {
				BlockUtil.setLever(lever, false);
				for (BlockFace leverside : FaceUtil.attachedFacesDown) {
					Block side = lever.getRelative(leverside);
					Material type = side.getType();
					if (type == Material.REDSTONE_WIRE || type == Material.REDSTONE_TORCH_ON || type == Material.DIODE_BLOCK_ON) {
						if (side.isBlockIndirectlyPowered()) {
							//this physical port is powered
							if (!this.leverpowered) {
								this.leverpowered = true;
								if (setport) this.port.updateLeverPower();
							}
							return;
						}
					}
				}
			}
		}
		//this physical port is not powered
		if (this.leverpowered) {
			this.leverpowered = false;
			if (setport) {
				//we went from on to off - did any other ports get power in the meantime?
				for (PhysicalPort p : this.port.locations) {
					if (p != this) {
						p.updateLeverPower(false);
					}
				}
				if (!this.port.updateLeverPower()) {
					//update all levers
					for (PhysicalPort p : this.port.locations) {
						p.setLevers();
					}
				}
			}
		}
	}
		
	private boolean leverpowered = false;

	public boolean isLeverPowered() {
		return this.leverpowered;
	}
	public void setLeverPowered(boolean powered) {
		this.leverpowered = powered;
	}
	
	public void setLevers() {
		this.setLevers(this.port.hasPower() && !this.leverpowered);
	}
	public void setLevers(boolean down) {
		if (this.mainblock == null) return;
		Util.setBlock(this.mainblock, down);
		if (!down) {
			//power change?
			this.updateLeverPower();
		}
	}
	
	
}
