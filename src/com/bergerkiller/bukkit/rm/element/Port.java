package com.bergerkiller.bukkit.rm.element;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.bukkit.block.Block;

import com.bergerkiller.bukkit.rm.Position;

public class Port extends Redstone {

	public static Port get(Block at) {
		PhysicalPort pp = PhysicalPort.get(at);
		if (pp == null) return null;
		return pp.port;
	}
	
	public String name;
	public HashSet<PhysicalPort> locations = new HashSet<PhysicalPort>();
	private boolean leverpowered = false;
	public boolean ignoreNext = false; //prevents infinite loops because of levers
	
	/**
	 * Updates the leverpowered state
	 * @return If the powered state got changed
	 */
	public boolean updateLeverPower() {
		return this.updateLeverPower(null);
	}
	public boolean updateLeverPower(PhysicalPort ignore) {
		if (this.isPowered()) {
			this.leverpowered = false;
		} else {
			if (this.leverpowered) {
				for (PhysicalPort p : locations) {
					if (p.isLeverPowered()) return false;
				}
				this.setLeverPowered(false);
				this.onPowerChange(ignore);
				return true;
			} else {
				for (PhysicalPort p : locations) {
					if (p.isLeverPowered()) {
						this.setLeverPowered(true);
						this.onPowerChange(ignore);
						return true;
					}
				}
			}
		}
		return false;
	}
		
	@Override
	public boolean hasPower() {
		return this.leverpowered || super.hasPower();
	}
	
	public void onPowerChange(PhysicalPort ignore) {
		if (ignore == null) {
			this.onPowerChange();
		} else {
			for (PhysicalPort p : locations) {
				if (p != ignore) p.setLevers();
			}
			super.onPowerChange();
		}
	}
	@Override
	public void onPowerChange() {
		if (!this.ignoreNext) {
			this.ignoreNext = true;
			for (PhysicalPort p : locations) {
				p.setLevers();
			}
		}
		super.onPowerChange();
		this.ignoreNext = false;
	}
	
	public boolean isLeverPowered() {
		return this.leverpowered;
	}
	public void setLeverPowered(boolean powered) {
		this.leverpowered = powered;
	}
	public byte getType() {
		return 3;
	}
	
	public PhysicalPort addPhysical(Block at) {
		return this.addPhysical(new Position(at));
	}
	public PhysicalPort addPhysical(Position at) {
		return new PhysicalPort(this, at);
	}
	
	public void loadInstance(DataInputStream stream) throws IOException {
		super.loadInstance(stream);
		int loccount = stream.readShort();
		for (int pi = 0; pi < loccount; pi++) {
			Position at = new Position(stream.readUTF(), stream.readInt(), stream.readByte(), stream.readInt());
			this.addPhysical(at).setLeverPowered(stream.readBoolean());
		}
	}
	public void saveInstance(DataOutputStream stream) throws IOException {
		super.saveInstance(stream);
		stream.writeShort(this.locations.size());
		for (PhysicalPort pp : this.locations) {
			stream.writeUTF(pp.position.worldname);
			stream.writeInt(pp.position.x);
			stream.writeByte(pp.position.y);
			stream.writeInt(pp.position.z);
			stream.writeBoolean(pp.isLeverPowered());
		}
	}
	
}
