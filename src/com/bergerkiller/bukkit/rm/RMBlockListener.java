package com.bergerkiller.bukkit.rm;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.bergerkiller.bukkit.rm.circuit.Circuit;
import com.bergerkiller.bukkit.rm.circuit.CircuitInstance;
import com.bergerkiller.bukkit.rm.element.PhysicalPort;
import com.bergerkiller.bukkit.rm.element.Port;

public class RMBlockListener extends BlockListener {
	
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (!event.isCancelled()) {
			Material type = event.getBlock().getType();
			if (type == Material.LEVER) {
				if (!Util.isLeverDown(event.getBlock())) {
					//a disable lever - attached block assigned to a port?
					PhysicalPort p = PhysicalPort.get(event.getBlock());
					if (p != null) {
						p.updateLeverPower();
					}
				}
			}
		}
	}
		
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			if (event.getBlock().getType() == Material.LEVER) {
				PhysicalPort.updateLevers(event.getBlock(), true);
			}
		}
	}

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
	
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled()) return;
		if (event.getLine(0).equalsIgnoreCase("[port]")) {
			String circuitname = event.getLine(1);
			Circuit c = Circuit.get(circuitname);
			if (c != null) {
				event.setLine(2, Util.fixName(event.getLine(2)));
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
					}
				}
				Block a = Util.getAttachedBlock(event.getBlock());
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
