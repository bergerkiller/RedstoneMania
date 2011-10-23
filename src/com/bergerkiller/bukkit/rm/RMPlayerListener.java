package com.bergerkiller.bukkit.rm;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.bergerkiller.bukkit.rm.element.PhysicalPort;


public class RMPlayerListener extends PlayerListener {
	
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block b = event.getClickedBlock();
				PlayerSelect.get(event.getPlayer()).set(event.getClickedBlock());
				if (b.getType() == Material.LEVER) {
					PhysicalPort pp = PhysicalPort.get(b);
					if (pp != null) {
						boolean down = !Util.isLeverDown(b);
						pp.setLeverPowered(down);
						pp.port.updateLeverPower(pp);
					}
				}
			}
		}
	}

}
