package com.bergerkiller.bukkit.rm.circuit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import com.bergerkiller.bukkit.rm.element.Port;
import com.bergerkiller.bukkit.rm.element.Redstone;

public class CircuitInstance extends CircuitBase {
	
	public Circuit source;
	public boolean isMain = false;
	
	public CircuitInstance(Circuit source, String name) {
		this.source = source;
		this.name = name;
	}
	
	public boolean updateAlive() {
		for (Port p : this.getPorts()) {
			if (p.locations.size() > 0) {
				return false;
			}
		}
		//no physical ports - dead
		this.source.removeInstance(this.name);
		return true;
	}
	
	public File getFile() {
		return new File(this.source.getInstanceFolder() + File.separator + this.name + ".instance");
	}
	public String getFullName() {
		return this.source.name + "." + this.name;
	}
	
	public void load(DataInputStream dis) throws Exception {
		for (Redstone r : this.elements) {
			r.loadInstance(dis);
		}
		for (CircuitInstance c : this.subcircuits) {
			c.load(dis);
		}
		this.initialize();
	}
	public void save(DataOutputStream dos) throws Exception {
		for (Redstone r : this.elements) {
			r.saveInstance(dos);
		}
		for (CircuitInstance c : this.subcircuits) {
			c.save(dos);
		}
	}
	
}
