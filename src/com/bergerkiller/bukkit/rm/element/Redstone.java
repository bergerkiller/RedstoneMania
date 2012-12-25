package com.bergerkiller.bukkit.rm.element;

import java.util.HashSet;
import java.util.logging.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.block.Block;

import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.circuit.CircuitBase;

public class Redstone {
	private int setdelay = 0;
	private boolean setpowered = false;	
	private boolean powered = false;
	private boolean inputpower = false;
	private int id = -1;
	private int delay = 0;
	private CircuitBase circuit;
	public HashSet<Redstone> inputs = new HashSet<Redstone>();
	public HashSet<Redstone> outputs = new HashSet<Redstone>();
	private final int burnoutValue = 5; //Sets the maximum allowed updates/tick
	private int burnoutCounter = burnoutValue;
	private short x, z;

	public void onTick() {
		if (setdelay > 0) {
			if (--setdelay == 0) {
				this.setPowered(this.setpowered, false);
			}
		}
		this.burnoutCounter = burnoutValue;
	}

	public void setPosition(Block block) {
		this.setPosition(block.getX(), block.getZ());
	}

	public void setPosition(int x, int z) {
		this.x = (short) x;
		this.z = (short) z;
	}
	
	public void setCircuit(CircuitBase circuit) {
		this.circuit = circuit;
	}
	public CircuitBase getCircuit() {
		return this.circuit;
	}
	
	/**
	 * Updates the outputs using the inputs, returns if this element changed
	 * @return
	 */
	public final boolean update() {
		//we don't have to update inactive elements!
		if (this.isDisabled()) {
			if (this.getType() != 3) {
				return false;
			}
		}
		//check if the opposite is the new result
		boolean hasinput = false;
		for (Redstone input : this.inputs) {
			if (input.hasPower())  {
				hasinput = true;
				break;
			}
		}
		if (this.inputpower && !hasinput) {
			inputpower = false;
			this.setPowered(false, true);
		} else if (!this.inputpower && hasinput) {
            this.inputpower = true;
			this.setPowered(true, true);
		} else {
			return false;
		}
		return true;
	}
		
	public final void setPowered(boolean powered) {
		this.powered = powered;
		this.inputpower = powered;
	}
	private void setPowered(boolean powered, boolean usedelay) {
		int delay = this.getDelay();
		if (usedelay && delay > 0) {
			if (this.setdelay == 0) {
				this.setpowered = powered;
				this.setdelay = delay;
			}
		} else if (this.burnoutCounter > 0) {
			--this.burnoutCounter;
			if (powered) {
				if (!this.powered) {
					this.powered = true;
					this.onPowerChange();
				}
				if (!this.inputpower) {
					this.setPowered(false, true);
				}
			} else if (!this.inputpower && this.powered) {
				this.powered = false;
				this.onPowerChange();
			}
		}
	}
	
	public void onPowerChange() {
		for (Redstone output : outputs) {
			output.update();
		}
	}
	
	public String toString() {
		if (this instanceof Inverter) {
			return "[Inverter " + this.id + "]";
		} else if (this instanceof Repeater) {
			return "[Repeater " + this.id + "]";
		} else if (this instanceof Port) {
			return "[Port '" + ((Port) this).name + "' " + this.id + "]";
		} else {
			return "[Wire " + this.id + "]";
		}
	}
	public final boolean isPowered() {
		return this.powered;
	}
	public boolean hasPower() {
		return this.isPowered();
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public int getDelay() {
		return this.delay;
	}
	
	public final short getX() {
		return this.x;
	}
	public final short getZ() {
		return this.z;
	}
	
	public final int getId() {
		return this.id;
	}
	public final void setId(int id) {
		this.id = id;
	}
	public final boolean isType(int... types) {
		for (int type : types) {
			if (this.getType() == type) return true;
		}
		return false;
	}
	public byte getType() {
		return 0;
	}
	
	public void setData(Redstone source) {
		this.id = source.id;
		this.delay = source.delay;
		this.powered = source.powered;
		this.setdelay = source.setdelay;
		this.setpowered = source.setpowered;
	}
	public Redstone clone() {
		Redstone r;
		if (this instanceof Inverter) {
			r = new Inverter();
		} else if (this instanceof Repeater) {
			r = new Repeater();
		} else if (this instanceof Port) {
			r = new Port();
			((Port) r).name = ((Port) this).name;
		} else {
			r = new Redstone();
		}
		r.setData(this);
		return r;
	}
	
	public final void transfer(Redstone to) {
		for (Redstone input : this.inputs) {
			input.outputs.remove(this);
			if (to != input) {
				input.outputs.add(to);
				to.inputs.add(input);
			}
		}
		for (Redstone output : this.outputs) {
			output.inputs.remove(this);
			if (to != output) {
				output.inputs.add(to);
				to.outputs.add(output);
			}
		}
		this.inputs.clear();
		this.outputs.clear();
	}
	
	/**
	 * Disables this element: inputs are instantly redirected to the outputs
	 * Result: this element has no role. All inputs and outputs are cleared
	 */
	public final void disable() {
		for (Redstone input : this.inputs) {
			input.outputs.remove(this);
			for (Redstone output : this.outputs) {
				input.connectTo(output);
			}
		}
		for (Redstone output : this.outputs) {
			output.inputs.remove(this);
			for (Redstone input : this.inputs) {
				input.connectTo(output);
			}
		}
		this.inputs.clear();
		this.outputs.clear();
	}
	
	public final Redstone findDirectConnection() {
		if (this.getDelay() == 0 && this.inputs.size() > 0 && this.outputs.size() > 0) {
			for (Redstone input : this.inputs) {
				if (input.getDelay() == 0 && this.outputs.contains(input)) {
					return input;
				}
			}
		}
		return null;
	}
	public final void connect(Redstone redstone) {
		this.connectTo(redstone);
		redstone.connectTo(this);
	}
	public final void connectTo(Redstone redstone) {
		if (redstone == this) return;
		this.outputs.add(redstone);
		redstone.inputs.add(this);
	}
	public final void disconnect(Redstone redstone) {
		this.inputs.remove(redstone);
		this.outputs.remove(redstone);
		redstone.inputs.remove(this);
		redstone.outputs.remove(this);
	}
	public final void disconnect() {
		for (Redstone r : this.inputs) {
			r.outputs.remove(this);
		}
		for (Redstone r : this.outputs) {
			r.inputs.remove(this);
		}
		this.inputs.clear();
		this.outputs.clear();
	}
	public final boolean isConnectedTo(Redstone redstone) {
		return this.outputs.contains(redstone);
	}
	public final boolean isConnected(Redstone redstone) {
		return this.isConnectedTo(redstone) && redstone.isConnectedTo(this);
	}
	public final boolean isDisabled() {
		return this.inputs.size() == 0 && this.outputs.size() == 0;
	}
	
	public void loadInstance(DataInputStream stream) throws IOException {
		this.powered = stream.readBoolean();
		if (this.delay > 0) {
			this.setdelay = stream.readInt();
			if (this.setdelay > 0) {
				this.setpowered = stream.readBoolean();
			}
		}
	}
	public void saveInstance(DataOutputStream stream) throws IOException {
		stream.writeBoolean(this.powered);
		if (this.delay > 0) {
			stream.writeInt(this.setdelay);
            if (this.setdelay > 0) {
            	stream.writeBoolean(this.setpowered);
            }
		}
	}
	
	public void saveTo(DataOutputStream stream) throws IOException {
		stream.write(this.getType());
		//init
		stream.writeShort(this.x);
		stream.writeShort(this.z);
	    stream.writeBoolean(this.powered);
	    stream.writeInt(this.delay);
	    if (this instanceof Port) {
	    	stream.writeUTF(((Port) this).name);
	    }
	}
	public static Redstone loadFrom(DataInputStream stream) throws IOException {
		byte type = stream.readByte();
		Redstone rval;
		if (type == 0) {
			rval = new Redstone();
		} else if (type == 1) {
			rval = new Inverter();
		} else if (type == 2) {
			rval = new Repeater();
		} else if (type == 3) {
			rval = new Port();
		} else {
			rval = new Redstone();
			RedstoneMania.plugin.log(Level.SEVERE, "Unknown redstone type: " + type);
		}
		//init
		rval.x = stream.readShort();
		rval.z = stream.readShort();
		rval.powered = stream.readBoolean();
		rval.delay = stream.readInt();
		if (rval instanceof Port) {
			((Port) rval).name = stream.readUTF();
		}
		return rval;
	}
	
}
