package com.bergerkiller.bukkit.rm.element;

import java.util.HashSet;
import java.util.logging.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.circuit.CircuitBase;

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
		this.resetBurnout();
	}
	
	private void resetBurnout() {
		//alter the maximum allowed updates/tick here
		this.burnoutCounter = 3;
	}
	
	private boolean powered = false;
	private boolean inputpower = false;
	private int id = -1;
	private int delay = 0;
	private CircuitBase circuit;
	public HashSet<Redstone> inputs = new HashSet<Redstone>();
	public HashSet<Redstone> outputs = new HashSet<Redstone>();
	private int burnoutCounter = 3;
	
	public void setCircuit(CircuitBase circuit) {
		this.circuit = circuit;
	}
	public CircuitBase getCircuit() {
		return this.circuit;
	}
	
	private boolean hasInput(HashSet<Redstone> ignore) {
		if (this.delay == 0) ignore.add(this);
		for (Redstone input : this.inputs) {
			if (!ignore.contains(input)) {
				if (this.outputs.contains(input)) {
					//direct connection - check this element
					if (input.hasInput(ignore)) {
						return true;
					}
				} else if (input.hasPower()) {
					return true;
				}
			}
		}
		return false;
	}
	public final void update() {
		//check if the opposite is the new result
		boolean hasinput = this.hasInput(new HashSet<Redstone>());
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
		if (usedelay && this.delay > 0) {
			if (powered) {
				this.pulse = true;
				this.setdelay = this.delay;
			} else if (this.setdelay == 0) {
				this.pulse = false;
				this.setdelay = this.delay;
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
				this.setdelay = this.delay;
			}
		}
	}
	
	public void onPowerChange() {
		for (Redstone output : outputs) {
			output.update();
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
		rval.powered = stream.readBoolean();
		rval.delay = stream.readInt();
		if (rval instanceof Port) {
			((Port) rval).name = stream.readUTF();
		}
		return rval;
	}
	
}
