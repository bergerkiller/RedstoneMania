package com.bergerkiller.bukkit.rm.element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.Util;
import com.bergerkiller.bukkit.rm.circuit.Circuit;
import com.bergerkiller.bukkit.rm.circuit.CircuitBase;
import com.bergerkiller.bukkit.rm.circuit.CircuitInstance;

public class Redstone {
	
	//public ArrayList<Update> updates = new ArrayList<Update>();
	private int setdelay = 0;
	private boolean pulse = false;
		
	public void updateTick() {
		if (setdelay > 0) {
			if (--setdelay == 0) {
				this.setPowered(pulse || this.inputpower, false);
			}
		}
		this.burnoutCounter = burnoutValue;
	}
	
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
	
	public final void update() {
		//we don't have to update inactive elements!
		if (this.inputs.size() == 0 && this.outputs.size() == 0) {
			if (this.getType() != 3) {
				return;
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
		}
	}
		
	public final void setPowered(boolean powered) {
		this.powered = powered;
		this.inputpower = powered;
	}
	private void setPowered(boolean powered, boolean usedelay) {
		int delay = this.getDelay();
		if (usedelay && delay > 0) {
			if (powered) {
				this.pulse = true;
				this.setdelay = delay;
			} else if (this.setdelay == 0) {
				this.pulse = false;
				this.setdelay = delay;
			}
		} else if (this.burnoutCounter == 0) {
			return;
		} else {
			--this.burnoutCounter;
			if (this.powered != powered) {
				this.powered = powered;
				this.onPowerChange();
			}
			if (this.inputpower != powered) {
				this.pulse = false;
				this.setdelay = delay;
			}
		}
	}
	
	public void onPowerChange() {
		for (Redstone output : outputs) {
			output.update();
		}
	}
	
	public String toString() {
		String cname = "unknown";
		if (this.circuit != null) cname = this.circuit.getFullName();
		if (this instanceof Inverter) {
			return cname + "[Inverter " + this.id + "]";
		} else if (this instanceof Repeater) {
			return cname + "[Repeater " + this.id + "]";
		} else if (this instanceof Port) {
			return cname + "[Port '" + ((Port) this).name + "' " + this.id + "]";
		} else {
			return cname + "[Wire " + this.id + "]";
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
		r.id = this.id;
		r.delay = this.delay;
		r.powered = this.powered;
		return r;
	}
	
	/**
	 * Merges all elements into this redstone element.
	 * Inputs and outputs of the elements get cleared.
	 * @param elements
	 */
	public final void transferConnections(Redstone... elements) {
		for (Redstone r : elements) {
			if (r == this) continue;
			for (Redstone input : r.inputs) {
				input.outputs.remove(r);
				if (this != input) {
					input.outputs.add(this);
					this.inputs.add(input);
				}
			}
			for (Redstone output : r.outputs) {
				output.inputs.remove(r);
				if (this != output) {
					output.inputs.add(this);
					this.outputs.add(output);
				}
			}
		}
	}
	
	public final void transfer(Redstone to) {
		to.transferConnections(this);
		this.inputs.clear();
		this.outputs.clear();
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
		return this.outputs.contains(redstone) && redstone.inputs.contains(this);
	}

	public void loadInstance(DataInputStream stream) throws IOException {
		this.powered = stream.readBoolean();
		if (this.delay > 0) {
			this.setdelay = stream.readInt();
			if (this.setdelay > 0) {
				this.pulse = stream.readBoolean();
			}
		}
	}
	public void saveInstance(DataOutputStream stream) throws IOException {
		stream.writeBoolean(this.powered);
		if (this.delay > 0) {
			stream.writeInt(this.setdelay);
            if (this.setdelay > 0) {
            	stream.writeBoolean(this.pulse);
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
			RedstoneMania.log(Level.SEVERE, "Unknown redstone type: " + type);
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
