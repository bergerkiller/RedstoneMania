package com.bergerkiller.bukkit.rm.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Diode;

import com.bergerkiller.bukkit.rm.PlayerSelect;
import com.bergerkiller.bukkit.rm.Position;
import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.RedstoneMap;
import com.bergerkiller.bukkit.rm.Util;
import com.bergerkiller.bukkit.rm.RedstoneMap.IntMap;
import com.bergerkiller.bukkit.rm.RedstoneMap.SolidComponent;
import com.bergerkiller.bukkit.rm.element.Inverter;
import com.bergerkiller.bukkit.rm.element.Port;
import com.bergerkiller.bukkit.rm.element.Redstone;
import com.bergerkiller.bukkit.rm.element.Repeater;

public class CircuitCreator {
	
	private Player by;
	private RedstoneMap map = new RedstoneMap();
	private ArrayList<Redstone> items = new ArrayList<Redstone>();
	private HashMap<String, CircuitInstance> subcircuits = new HashMap<String, CircuitInstance>();
	private ArrayList<Block> ports = new ArrayList<Block>();
	private HashMap<Position, Integer> delays = new HashMap<Position, Integer>();
	private final int torchdelay = 2;
	
	public CircuitCreator(Player by, PlayerSelect from) {
		this.by = by;
		//prepare the ports, items and delays
		this.delays = from.delays;
		for (Map.Entry<Position, String> entry : from.portnames.entrySet()) {
			Port p = new Port();
			p.name = entry.getValue();
			Block b = entry.getKey().getBlock();
			ports.add(b);
			map.setValue(map.get(b), p);
			items.add(p);
		}
	}
	
	public Circuit create() {
		//generate circuit for ALL ports
		for (Block p : ports) {
			createWire(map.get(p).value, p, Material.REDSTONE_WIRE);
		}
		//save
		Circuit c = new Circuit();
		c.elements = items.toArray(new Redstone[0]);
		c.subcircuits = subcircuits.values().toArray(new CircuitInstance[0]);
		c.initialize();
		return c;
	}
	
	private Port convertPort(Port from) {
		CircuitInstance cb = (CircuitInstance) from.getCircuit();
		String fullname = cb.getFullName();
		CircuitInstance ci = subcircuits.get(fullname);
		if (ci == null) {
			ci = cb.source.createInstance();
			subcircuits.put(fullname, ci);
		}
		return cb.getPort(from.name);
	}
	
	private int getDelay(Block b, Material type) {
		int delay = 0;
		Position pos = new Position(b);
		if (delays.containsKey(pos)) {
			delay = delays.get(pos);
		} else if (Util.isRedstoneTorch(type)) {
			delay = 1;
		} else if (Util.isDiode(type)) {
			delay = ((Diode) type.getNewData(b.getData())).getDelay();
		}
		return delay * torchdelay;
	}
	private boolean setMap(IntMap m, Redstone replacement) {
		if (m.value == null) {
			map.setValue(m, replacement);
			return true;
		} else {
			return false;
		}
	}
	private boolean fixMap(IntMap m, Block b, Material type) {
		if (m.value == null) {
			if (type == Material.REDSTONE_TORCH_ON || type == Material.REDSTONE_TORCH_OFF) {
				map.setValue(m, new Inverter());
				m.value.setPowered(type == Material.REDSTONE_TORCH_ON);
				m.value.setDelay(getDelay(b, type));
			} else if (type == Material.DIODE_BLOCK_ON || type == Material.DIODE_BLOCK_OFF) {
				map.setValue(m, new Repeater());
				m.value.setPowered(type == Material.DIODE_BLOCK_ON);
				m.value.setDelay(getDelay(b, type));
			} else if (type == Material.REDSTONE_WIRE) {
				map.setValue(m, new Redstone());
			} else if (type == Material.LEVER) {
				//is it a port?
				Port p = Port.get(b);
				if (p != null) {
					p = convertPort(p);
					if (p != null) {
						map.setValue(m, p);
						return true;
					} else {
						RedstoneMania.log(Level.WARNING, "[Creation] Failed to convert port!");
						return false;
					}
				} else {
					return false;
				}
			} else if (Util.isSolid(type)) {
				map.setValue(m, new SolidComponent(b));
				return true;
			} else {
				return false;
			}
			items.add(m.value);
			return true;
		}
		return false;
	}
	private void transfer(Redstone from, Redstone to) {
		if (from == to) return;
		map.merge(from, to);
		//transfer the inputs and outputs
		for (Redstone input : from.inputs) {
			input.outputs.remove(from);
			input.outputs.add(to);
			if (input != from) to.inputs.add(input);
		}
		for (Redstone output : from.outputs) {
			output.inputs.remove(from);
			output.inputs.add(to);
			if (output != from) to.outputs.add(output);
		}
		from.inputs.clear();
		from.outputs.clear();
		items.remove(from);
	}	
	
	private IntMap create(Block block) {
		Material type = block.getType();
		IntMap m = map.get(block);
		if (fixMap(m, block, type)) {
			if (Util.isRedstoneTorch(type)) {
				createInverter((Inverter) m.value, block, type);
			} else if (Util.isDiode(type)) {
				createRepeater((Repeater) m.value, block, type);
			} else if (type == Material.REDSTONE_WIRE) {
				createWire(m.value, block, type);
			} else if (type == Material.LEVER) {
				createPort((Port) m.value, Util.getAttachedBlock(block), type);
			} else if (m.value instanceof SolidComponent) {
				createSolid((SolidComponent) m.value, block, type);
			}
		}
		return m;
	}
	
	private void createPort(Port redstone, Block port, Material type) {
		for (BlockFace face : Util.bowl) {
			
		}
		msg("PORT FOUND!");
	}
	
	private void createInverter(Inverter redstone, Block inverter, Material type) {
		for (BlockFace face : Util.bowl) {
			Block b = inverter.getRelative(face);
			Material btype = b.getType();
			if (btype == Material.REDSTONE_WIRE) {
				redstone.connectTo(create(b).value);
			} else if (btype == Material.DIODE_BLOCK_OFF || btype == Material.DIODE_BLOCK_ON) {
				if (face != BlockFace.DOWN) { 
					//connected to the input?
					BlockFace facing = Util.getFacing(b, btype);
					if (facing == face) {
						redstone.connectTo(create(b).value);
					}
				}
			}
		}
		Block above = inverter.getRelative(BlockFace.UP);
		Material abovetype = above.getType();
		if (Util.isSolid(abovetype)) {
			create(above);
		}
		create(Util.getAttachedBlock(inverter));
	}
	
	private void createRepeater(Repeater redstone, Block repeater, Material type) {
		BlockFace facing = Util.getFacing(repeater, type);
		Block output = repeater.getRelative(facing);
		Material outputtype = output.getType();
		if (outputtype == Material.REDSTONE_WIRE) {
			//connect this repeater to wire
			redstone.connectTo(create(output).value);
		} else if (Util.isDiode(outputtype)) {
			BlockFace oface = Util.getFacing(output, outputtype);
			if (facing == oface) {
				redstone.connectTo(create(output).value);
			}
		} else if (Util.isSolid(outputtype)) {
			create(output);
		}
		Block input = repeater.getRelative(facing.getOppositeFace());
		Material inputtype = repeater.getType();
		if (inputtype == Material.REDSTONE_WIRE) {
			//connect this repeater to wire
			create(input).value.connectTo(redstone);
		} else if (Util.isDiode(inputtype)) {
			BlockFace oface = Util.getFacing(input, inputtype);
			if (facing == oface) {
				create(input).value.connectTo(redstone);
			}
		} else if (Util.isSolid(inputtype)) {
			create(input);
		}
	}
	
	private Redstone connectWire(Block wire, Redstone redstone) {
		IntMap m = map.get(wire);
		if (m.value == redstone) return redstone;
		if (setMap(m, redstone)) {
			//added block to this wire
			createWire(redstone, wire, Material.REDSTONE_WIRE);
			return redstone;
		} else {
			//merge the two wires
			if (redstone instanceof Port) {
				if (m.value instanceof Port) {
					Port p1 = (Port) redstone;
					Port p2 = (Port) m.value;
					msg("Port '" + p1.name + "' merged with port '" + p2.name + "'!");
				}
				transfer(m.value, redstone);
				return redstone;
			} else {
				transfer(redstone, m.value);
				return m.value;
			}
		}
	}
	
	private void createWire(Redstone redstone, Block wire, Material type) {
		//wire - first find all nearby elements
		for (BlockFace face : Util.radial) {
			Block b = wire.getRelative(face);
			Material btype = b.getType();
			if (btype == Material.REDSTONE_WIRE) {
				//same wire
				redstone = connectWire(b, redstone);				
			} else if (btype == Material.AIR) {
				//wire below?
				Block below = b.getRelative(BlockFace.DOWN);
				if (below.getType() == Material.REDSTONE_WIRE) {
					redstone = connectWire(below, redstone);
				}
			} else if (Util.isRedstoneTorch(btype)) {
				//this wire receives input from this torch
				create(b); //we assume that the torch handles direct wire connection
			} else if (Util.isDiode(btype)) {
				//powering or receiving power
				BlockFace facing = Util.getFacing(b, btype);
				if (facing == face) {
					//wire powers repeater
					redstone.connectTo(create(b).value);
				} else if (facing.getOppositeFace() == face) {
					//repeater powers wire
					create(b); //we assume that the repeater handles direct wire connections
				}
			} else if (btype == Material.LEVER) {
				create(b);
			} else if (Util.isSolid(btype)) {
				//wire on top?
				Block above = b.getRelative(BlockFace.UP);
				if (above.getType() == Material.REDSTONE_WIRE) {
					redstone = connectWire(above, redstone);
				}
				create(b);
			}
		}
		//update the block this wire sits on
		create(wire.getRelative(BlockFace.DOWN));
		//a torch above this wire?
		Block above = wire.getRelative(BlockFace.UP);
		if (Util.isRedstoneTorch(above)) create(above);
	}
	
	private void createSolid(SolidComponent comp, Block block, Material type) {
		//create block data
		IntMap[] inputs = new IntMap[comp.inputs.size()];
		IntMap[] outputs = new IntMap[comp.outputs.size()];
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = create(comp.inputs.get(i));
		}
		for (int i = 0; i < outputs.length; i++) {
			outputs[i] = create(comp.outputs.get(i));
		}
		//connect inputs with outputs
		for (IntMap input : inputs) {
			for (IntMap output : outputs) {
				if (input.value.isType(0, 3)) {
					if (output.value.isType(0, 3)) {
						//a wire does NOT power other wires!
						continue;
					}
				}
				input.value.connectTo(output.value);
			}
		}
	}
	
	private void msg(String message) {
		this.by.sendMessage(ChatColor.YELLOW + message);
	}
	
}