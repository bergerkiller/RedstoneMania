package com.bergerkiller.bukkit.rm.circuit;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.element.PhysicalPort;

public class CircuitProvider {
	private static HashMap<String, Circuit> circuits = new HashMap<String, Circuit>();

	/**
	 * Gets or loads a Circuit
	 * 
	 * @param name of the Circuit to get
	 * @return the Circuit, or null if it could not be found
	 */
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
}
