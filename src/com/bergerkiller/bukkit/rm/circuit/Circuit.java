package com.bergerkiller.bukkit.rm.circuit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.element.PhysicalPort;
import com.bergerkiller.bukkit.rm.element.Port;
import com.bergerkiller.bukkit.rm.element.Redstone;

public class Circuit extends CircuitBase {
	private HashMap<String, CircuitInstance> instances = new HashMap<String, CircuitInstance>();

	@Override
	public File getFile() {
		return CircuitProvider.getCircuitFile(this.name);
	}

	public File getInstanceFolder() {
		File file = new File(CircuitProvider.getInstancesFolder() + File.separator + this.name);
		file.mkdirs();
		return file;
	}

	public CircuitInstance getInstance(String name) {
		return this.instances.get(name);
	}

	public Collection<CircuitInstance> getInstances() {
		return this.instances.values();
	}

	private CircuitInstance createInstance(boolean main) {
		CircuitInstance c = new CircuitInstance(this, "");
		//Set dependencies
		c.subcircuits = new CircuitInstance[this.subcircuits.length];
		for (int i = 0; i < c.subcircuits.length; i++) {
			c.subcircuits[i] = this.subcircuits[i].source.createInstance();
		}
		//Clone the data
		c.elements = new Redstone[this.elements.length];
		for (int i = 0; i < this.elements.length; i++) {
			c.elements[i] = this.elements[i].clone();
		}
		//Perform some ID generation to match the element ID's
		c.initialize();
		//Link the elements
		for (int i = 0; i < c.elements.length; i++) {
			Redstone from = this.elements[i];
			Redstone to = c.elements[i];
			if (from.getId() != to.getId()) {
				RedstoneMania.plugin.log(Level.SEVERE, "Failed to make a new instance of '" + this.name + "': ID out of sync!");
				return null;
			} else {
				for (Redstone input : from.inputs) {
					Redstone element = c.getElement(input);
					if (element == null) {
						RedstoneMania.plugin.log(Level.SEVERE, "Failed to create a new instance of '" + this.name + "': input element ID mismatch!");
						return null;
					} else {
						element.connectTo(to);
					}
				}
				for (Redstone output : from.outputs) {
					Redstone element = c.getElement(output);
					if (element == null) {
						RedstoneMania.plugin.log(Level.SEVERE, "Failed to create a new instance of '" + this.name + "': output element ID mismatch!");
						return null;
					} else {
						to.connectTo(element);
					}
				}
			}
		}
		//Fix direct connections
		c.fixDirectConnections();
		return c;
	}
	public String findNewInstanceName() {
		StringBuilder nameBuilder = new StringBuilder(2);
		String newName = "";
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			nameBuilder.setLength(0);
			nameBuilder.append(i);
			newName = nameBuilder.toString();
			if (!this.instances.containsKey(newName)) {
				break;
			}
		}
		return newName;
	}
	public CircuitInstance createInstance() {
		return this.createInstance(false);
	}
	public CircuitInstance createInstance(String name) {
		CircuitInstance c = this.getInstance(name);
		if (c == null) {
			c = this.createInstance(true);
			c.name = name;
			this.instances.put(name,  c);
		}
		return c;
	}
	public CircuitInstance removeInstance(String name) {
		CircuitInstance ci = this.instances.remove(name);
		if (ci != null) {
			for (Port p : ci.getPorts()) {
				for (PhysicalPort pp : p.locations) {
					PhysicalPort.remove(pp);
				}
			}
		}
		File sourcefile = ci.getFile();
		if (sourcefile.exists()) sourcefile.delete();
		return ci;
	}

	@Override
	public void load(DataInputStream dis) throws IOException {
		//Read the sub-circuit dependencies
		this.subcircuits = new CircuitInstance[dis.readShort()];
		for (int i = 0; i < this.subcircuits.length; i++) {
			String cname = dis.readUTF();
			Circuit c = CircuitProvider.get(cname);
			if (c == null) {
				throw new RuntimeException("Circuit dependency not found: " + cname);
			} else {
				this.subcircuits[i] = c.createInstance();
			}
		}
		//Read the circuit data
		this.elements = new Redstone[dis.readShort()];
		for (int i = 0; i < this.elements.length; i++) {
			this.elements[i] = Redstone.loadFrom(dis);
		}
		this.initialize();
		//Connect elements
		for (Redstone r : this.elements) {
			int inputcount = dis.readShort();
			for (int i = 0; i < inputcount; i++) {
				Redstone input = this.getElement(dis.readInt());
				if (input == null) {
					throw new IOException("Redstone element has a missing input!");
				} else {
					input.connectTo(r);
				}
			}
			int outputcount = dis.readShort();
			for (int i = 0; i < outputcount; i++) {
				Redstone output = this.getElement(dis.readInt());
				if (output == null) {
					throw new IOException("Redstone element has a missing output!");
				} else {
					r.connectTo(output);
				}
			}
		}
	}

	@Override
	public void save(DataOutputStream dos) throws IOException {
		//Write circuit dependencies
		dos.writeShort(this.subcircuits.length);
		for (CircuitInstance c : this.subcircuits) {
			dos.writeUTF(c.source.name);
		}
		//Write the circuit data
		dos.writeShort(this.elements.length);
		for (Redstone r : this.elements) {
			r.saveTo(dos);
		}
		//Write connections
		for (Redstone r : this.elements) {
			dos.writeShort(r.inputs.size());
			for (Redstone input : r.inputs) {
				dos.writeInt(input.getId());
			}
			dos.writeShort(r.outputs.size());
			for (Redstone output : r.outputs) {
				dos.writeInt(output.getId());
			}
		}
	}

	public String getNewInstanceName() {
		int index = this.instances.size();
		String name = String.valueOf(index);
		while (getInstance(name) != null) {
			index++;
			name = String.valueOf(index);
		}
		return name;
	}
}
