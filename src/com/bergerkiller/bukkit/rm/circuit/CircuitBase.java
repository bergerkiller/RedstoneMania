package com.bergerkiller.bukkit.rm.circuit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.bergerkiller.bukkit.common.config.DataReader;
import com.bergerkiller.bukkit.common.config.DataWriter;
import com.bergerkiller.bukkit.rm.element.Port;
import com.bergerkiller.bukkit.rm.element.Redstone;

public class CircuitBase {
		
	public String name;
	public Redstone[] elements;
	public CircuitInstance[] subcircuits;
	private HashMap<String, Port> ports = new HashMap<String, Port>();
	private boolean initialized = false;
	private boolean loaded = false;
	private boolean saved = false;

	public void doPhysics() {
		for (Redstone r : this.elements) {
			r.updateTick();
		}
		for (CircuitInstance ci : this.subcircuits) {
			ci.doPhysics();
		}
	}
	
	public void initialize() {
		this.initialize(true);
	}
	private void initialize(boolean generateIds) {
		this.ports.clear();
		for (Redstone r : this.elements) {
			r.setCircuit(this);
			if (r instanceof Port) {
				this.ports.put(((Port) r).name, (Port) r);
			}
		}
		for (CircuitBase c : this.subcircuits) {
			c.initialize(false);
		}
		if (generateIds) {
			this.generateIds(0);
		}
		this.initialized = true;
	}
	public boolean isInitialized() {
		return this.initialized;
	}
	
	public Port getPort(String name) {
		return this.ports.get(name);
	}
	public Collection<Port> getPorts() {
		return this.ports.values();
	}
	
	public void fixDirectConnections() {
		this.fixDirectConnections(true);
	}
	private boolean fixDirectConnections(boolean checksub) {
		boolean changed = false;
		boolean hasDirectConnections = true;
		while (hasDirectConnections) {
			hasDirectConnections = false;
			for (Redstone r : this.elements) {
				Redstone direct = r.findDirectConnection();
				if (direct == null) continue;
				hasDirectConnections = true;
				changed = true;
				//remove which: direct or r?
				if (direct instanceof Port) {
					if (r instanceof Port) {
						//which port is better?
						if (direct.getCircuit() == this) {
							//direct is top-level, this one is most important
							if (r.getCircuit() == this) {
								//r is also top level?! Oh oh! Let's just de-link them for good grace!
								r.disconnect(direct);
							} else {
								r.disable();
							}
						} else {
							direct.disable();
						}
					} else {
						r.disable();
					}
				} else {
					direct.disable();
				}
			}
		}
		if (checksub) {
			for (CircuitBase c : this.subcircuits) {
				if (c.fixDirectConnections(false)) changed = true;
			}
		}
		if (changed) {
			return this.fixDirectConnections(checksub);
		} else {
			return false;
		}
	}
	
	public Redstone getElement(int id) {
		if (id >= this.elements.length) {
			for (CircuitBase sub : this.subcircuits) {
				for (Redstone r : sub.elements) {
					if (r.getId() == id) return r;
				}
			}
			return null;
		} else {
			return this.elements[id];
		}
	}
	public Redstone getElement(Redstone guide) {
		return this.getElement(guide.getId());
	}
	public void removeElement(Redstone element) {
		for (int i = 0; i < this.elements.length; i++) {
			if (this.elements[i] == element) {
				this.removeElement(i);
				return;
			}
		}
	}
	private void removeElement(int index) {
		Redstone[] newElements = new Redstone[this.elements.length - 1];
		for (int i = 0; i < index; i++) {
			newElements[i] = this.elements[i];
		}
		for (int i = index; i < newElements.length; i++) {
			newElements[i] = this.elements[i + 1];
		}
		this.elements = newElements;
	}
	
	/**
	 * Generates a list of elements which do not require another circuit to exist
	 * Removes all dependencies and internal ports at the cost of a large schematic
	 * @return
	 */
	public Redstone[] getIndependentElements() {
		return this.getIndependentElements(true);
	}
	private Redstone[] getIndependentElements(boolean main) {
		ArrayList<Redstone> elements = new ArrayList<Redstone>();
		for (Redstone r : this.elements) {
			if (r != null && !r.isDisabled()) {
				if (!main && r instanceof Port) {
					Redstone old = r;
					r = new Redstone();
					r.setData(old);
				}
				elements.add(r);
			}
		}
		for (CircuitBase cb : this.subcircuits) {
		    for (Redstone r : cb.getIndependentElements()) {
		    	elements.add(r);
		    }
		}
		return elements.toArray(new Redstone[0]);
	}
	public Circuit getIndependentCircuit() {
		Circuit c = new Circuit();
		c.elements = this.getIndependentElements();
		c.subcircuits = new CircuitInstance[0];
		c.initialize();
		return c;
	}
	
	private int generateIds(int startindex) {
		for (Redstone r : this.elements) {
			r.setId(startindex);
			startindex++;
		}
		for (CircuitBase cb : this.subcircuits) {
			startindex = cb.generateIds(startindex);
		}
		return startindex;
	}
	
	public File getFile() {
		//to be overridden
		return null;
	}
	public String getFullName() {
		return this.name;
	}
	public final boolean isSaved() {
		return this.getFile().exists();
	}
	
	public final boolean load() {
		return this.load(this.getFile());
	}
	public final boolean save() {
		return this.save(this.getFile());
	}
	
	public final boolean load(File file) {
		final CircuitBase me = this;
		this.loaded = false;
	    new DataReader(file) {
	    	public void read(DataInputStream stream) throws IOException {
	    		me.load(stream);
	    		me.loaded = true;
	    	}
	    }.read();
		return this.loaded;
	}
	public final boolean save(File file) {
		final CircuitBase me = this;
		this.saved = false;
		new DataWriter(file) {
			public void write(DataOutputStream stream) throws IOException {
				me.save(stream);
				me.saved = true;
			}
		}.write();
		return this.saved;
	}
	
	public void load(DataInputStream stream) throws IOException {
		//to be overridden
	}
	public void save(DataOutputStream stream) throws IOException {
		//to be overridden
	}

}
