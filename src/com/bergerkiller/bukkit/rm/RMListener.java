package com.bergerkiller.bukkit.rm;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.rm.circuit.Circuit;
import com.bergerkiller.bukkit.rm.circuit.CircuitInstance;
import com.bergerkiller.bukkit.rm.circuit.CircuitProvider;
import com.bergerkiller.bukkit.rm.element.PhysicalPort;
import com.bergerkiller.bukkit.rm.element.Port;

public class RMListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block b = event.getClickedBlock();
				PlayerSelect ps = PlayerSelect.get(event.getPlayer());
				ps.set(event.getClickedBlock());
				if (ps.setDelay()) {
					event.getPlayer().sendMessage(ChatColor.GREEN + "This element now has a delay of " + ps.clickdelay + " ticks!");
				}
				if (b.getType() == Material.LEVER) {
					PhysicalPort pp = PhysicalPort.get(b);
					if (pp != null) {
						boolean down = !BlockUtil.isLeverDown(b);
						pp.setLeverPowered(down);
						pp.port.updateLeverPower(pp);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.isIn(event.getChunk())) {
				p.setLoaded(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled()) return;
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.isIn(event.getChunk())) {
				p.setLoaded(false);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldLoad(WorldLoadEvent event) {
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.world.equals(event.getWorld().getName())) {
				p.updateLoaded();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldUnload(WorldUnloadEvent event) {
		if (event.isCancelled()) return;
		for (PhysicalPort p : PhysicalPort.getAll()) {
			if (p.position.world.equals(event.getWorld().getName())) {
				p.setLoaded(false);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (!event.isCancelled()) {
			Material type = event.getBlock().getType();
			if (type == Material.LEVER) {
				if (!BlockUtil.isLeverDown(event.getBlock())) {
					//a disable lever - attached block assigned to a port?
					PhysicalPort p = PhysicalPort.get(event.getBlock());
					if (p != null) {
						p.updateLeverPowered();
					}
				}
			}
		}
	}
		
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			if (event.getBlock().getType() == Material.LEVER) {
				PhysicalPort.updateLevers(event.getBlock());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		//is a port?
		PhysicalPort p = PhysicalPort.get(event.getBlock());
		if (p != null) {
			if (event.getBlock().getType() == Material.LEVER) {
				p.updateLevers(true);
			} else {
				PhysicalPort.remove(p);
				event.getPlayer().sendMessage("You removed a port to '" + p.port.name + "'!");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled()) return;
		if (event.getLine(0).equalsIgnoreCase("[port]")) {
			String circuitname = event.getLine(1);
			Circuit c = CircuitProvider.get(circuitname);
			if (c != null) {
				event.setLine(2, Util.fixName(event.getLine(2)));
				if (event.getLine(2).length() == 0) {
					event.setLine(2, c.findNewInstanceName());
				}
				String instance = event.getLine(2);
				CircuitInstance cc = c.getInstance(instance);
				if (cc == null) {
					cc = c.createInstance(instance);
					if (cc == null) {
						event.getPlayer().sendMessage(ChatColor.RED + "Failed to make a new instance of '" + circuitname + "'!");
						return;
					} else {
						cc.initialize();
						cc.update();
						event.getPlayer().sendMessage(ChatColor.GREEN + "A new instance of '" + circuitname + "' has been made!");
						event.getPlayer().sendMessage(ChatColor.GREEN + "You can add more ports to this instance by re-using the name on the third line of this sign");
					}
				}
				Block a = BlockUtil.getAttachedBlock(event.getBlock());
				String portname = event.getLine(3);
				Port p = cc.getPort(portname);
				if (p == null) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("This port does not exist!");
				} else {
					p.addPhysical(a);
					event.getPlayer().sendMessage("Port to '" + portname + "' added!");
				}
			} else {
				event.getPlayer().sendMessage("This circuit does not exist!");
			}
		}
	}
}
