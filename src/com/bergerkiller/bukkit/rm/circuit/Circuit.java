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
	private static HashMap<String, Circuit> circuits = new HashMap<String, Circuit>();
	public static Circuit get(String name) {
		Circuit c = circuits.get(name);
		if (c != null) {
			return c;
		}

		//possible to load?
		File source = getCircuitFile(name);
		if (source.exists()) {
			c = load(name);
			if (c != null) {
				try {
					// Load circuit instances if available
					File instanceFolder = c.getInstanceFolder();
					for (String instanceName : instanceFolder.list()) {
						if (instanceName.toLowerCase().endsWith(".instance")) {
							instanceName = instanceName.substring(0, instanceName.length() - 9);
							CircuitInstance ci = c.createInstance(instanceName);
							if (ci != null && ci.load()) {
								ci.update();
								ci.updateAlive();
							} else {
								// Delete this instance
								File instanceFile = new File(instanceFolder, instanceName);
								instanceFile.delete();
							}
						}
					}
					// Add the circuit
					add(c);
					return c;
				} catch (Throwable t) {
					RedstoneMania.plugin.log(Level.SEVERE, "An error occurred while loading the instances of circuit '" + name + "':");
					t.printStackTrace();
				}
			}
		}

		// Do instances of this circuit name exist?
		// If so, delete these instances
		File instanceFolder = getInstancesFolder(name);
		if (instanceFolder.exists()) {
			RedstoneMania.plugin.log(Level.WARNING, "Circuit instances of '" + name + "' will be deleted, because the circuit no longer exists!");
			// Delete instance files
			try {
				for (File instanceFile : instanceFolder.listFiles()) {
					instanceFile.delete();
				}
				instanceFolder.delete();
			} catch (SecurityException ex) {
				RedstoneMania.plugin.log(Level.WARNING, "Could not completely remove broken circuit instances of circuit '" + name + "':");
				ex.printStackTrace();
			}
		}
		return null;
	}
	public static void add(Circuit circuit) {
		if (circuit != null) {
			if (!circuit.isSaved()) circuit.save();
			circuits.put(circuit.name, circuit);
		}
	}
	public static void add(Circuit circuit, String name) {
		if (circuit != null) {
			circuit.name = name;
			add(circuit);
		}
	}
	public static Collection<Circuit> all() {
		return circuits.values();
	}
	public static boolean unload(String name) {
		Circuit c = circuits.remove(name);
		if (c != null) {
			
			return true;
		} else {
			return false;
		}
	}
	public static boolean delete(String name) {
		unload(name);
		File file = getCircuitFile(name);
		if (file.exists()) return file.delete();
		return false;
	}
	public static Circuit load(String name) {
		Circuit c = new Circuit();
		c.name = name;
		if (c.load()) return c;
		return null;
	}
	public static String[] getNames() {
		String[] names = getCircuitsFolder().list();
		for (int i = 0; i < names.length; i++) {
			if (names[i].toLowerCase().endsWith(".circuit")) {
				names[i] = names[i].substring(0, names[i].length() - 8);
			}
		}
		return names;
	}
	public static void loadAll() {
		for (String circuitname : getInstancesFolder().list()) {
			get(circuitname);
		}
	}
	public static void clearAll() {
		circuits.clear();
		PhysicalPort.clearAll();
	}
	
	public static File getCircuitsFolder() {
		File file = new File(RedstoneMania.plugin.getDataFolder(), "circuits");
		file.mkdirs();
		return file;
	}
	public static File getInstancesFolder() {
		File file = new File(RedstoneMania.plugin.getDataFolder(), "instances");
		file.mkdirs();
		return file;
	}
	public static File getInstancesFolder(String circuitName) {
		return new File(getInstancesFolder(), circuitName);
	}
	public static File getCircuitFile(String name) {
		return new File(getCircuitsFolder(), name + ".circuit");
	}

	public File getFile() {
		return getCircuitFile(this.name);
	}
	public File getInstanceFolder() {
		File file = new File(getInstancesFolder() + File.separator + this.name);
		file.mkdirs();
		return file;
	}
	
	private HashMap<String, CircuitInstance> instances = new HashMap<String, CircuitInstance>();
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
	
	public void load(DataInputStream dis) throws IOException {
		//Read the sub-circuit dependencies
		this.subcircuits = new CircuitInstance[dis.readShort()];
		for (int i = 0; i < this.subcircuits.length; i++) {
			String cname = dis.readUTF();
			Circuit c = get(cname);
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
