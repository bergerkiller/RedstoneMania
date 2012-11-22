package com.bergerkiller.bukkit.rm;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.bergerkiller.bukkit.common.MessageBuilder;
import com.bergerkiller.bukkit.common.PluginBase;
import com.bergerkiller.bukkit.common.Task;
import com.bergerkiller.bukkit.common.permissions.NoPermissionException;
import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.common.utils.StringUtil;
import com.bergerkiller.bukkit.rm.circuit.Circuit;
import com.bergerkiller.bukkit.rm.circuit.CircuitCreator;
import com.bergerkiller.bukkit.rm.circuit.CircuitInstance;
import com.bergerkiller.bukkit.rm.element.Port;

public class RedstoneMania extends PluginBase {
	public static RedstoneMania plugin;

	private Task updatetask;
	public void enable() {
		plugin = this;
		
		//General registering
		this.register(RMListener.class);
		this.register("circuit");
		
		//Load
		load();
		
		//Start scheduler
		this.updatetask = new Task(this) {
			public void run() {
				for (Circuit c : Circuit.all()) {
					for (CircuitInstance ci : c.getInstances()) {
						ci.doPhysics();
					}
				}
			}
		}.start(1, 1);
	}

	public void load() {
		Circuit.loadAll();
	}

	@Override
	public int getMinimumLibVersion() {
		return 1;
	}

	public void disable() {
		Task.stop(this.updatetask);
		for (Circuit c : Circuit.all()) {
			for (CircuitInstance ci : c.getInstances()) {
				ci.save();
			}
		}
		Circuit.clearAll();
	}

	@Override
	public boolean command(CommandSender sender, String cmdLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!player.hasPermission("redstonemania.use")) {
				throw new NoPermissionException();
			}
			PlayerSelect sel = PlayerSelect.get(player);
			if (args.length > 0) {
				cmdLabel = args[0].toLowerCase();
				args = StringUtil.remove(args, 0);
				if (cmdLabel.equals("setport") || cmdLabel.equals("port")) {
					if (sel.isRedstone()) {
						if (args.length > 0) {
							String name = "";
							for (String arg : args) {
								if (name != "") name += " ";
								name += arg;
							}
							sel.setPort(name);
							sender.sendMessage("You added port '" + name + "'!");
						} else {
							sender.sendMessage("Please enter a port name too!");
						}
					} else {
						sender.sendMessage("Please select a wire to set a port to first!");
					}
				} else if (cmdLabel.equals("clearports")) {
					sel.clearPorts();
					sender.sendMessage(ChatColor.YELLOW + "You cleared all set ports!");
				} else if (cmdLabel.equals("cleardelays")) {
					sel.clearDelays();
					sender.sendMessage(ChatColor.YELLOW + "You cleared all set delays!");
				} else if (cmdLabel.equals("clear")) {
					sel.clearPorts();
					sel.clearDelays();
					sender.sendMessage(ChatColor.YELLOW + "You cleared all set ports and delays!");
				} else if (cmdLabel.equals("clickdelay")) {
					if (args.length > 0) {
						try {
							sel.clickdelay = Integer.parseInt(args[0]);
						} catch (Exception ex) {
							sel.clickdelay = -1;
						}
						if (sel.clickdelay < 0) {
							sender.sendMessage(ChatColor.YELLOW + "Click-set delay is now disabled!");
						} else {
							sender.sendMessage(ChatColor.GREEN + "Click-set delay is now set at " + sel.clickdelay +  " ticks!");
						}
					}
				} else if (cmdLabel.equals("setdelay") || cmdLabel.equals("delay")) {
					if (sel.isDelayable()) {
						if (args.length > 0) {
							int delay = 0;
							try {
								delay = Integer.parseInt(args[0]);
							} catch (Exception ex) {}
							sel.setDelay(delay);
							sender.sendMessage(ChatColor.YELLOW + "This element now has a delay of " + delay + " ticks!");
						} else {
							sender.sendMessage(ChatColor.RED + "Please enter a delay for this element too!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Please select a wire to set a port to first!");
					}
				} else if (cmdLabel.equals("toggle")) {
					if (args.length >= 3) {
						String circuitName = args[0];
						String instanceName = args[1];
						String portName = args[2];
						// Try to find the circuit and instance
						Circuit c = Circuit.get(circuitName);
						if (c != null) {
							CircuitInstance ci = c.getInstance(instanceName);
							if (ci != null) {
								// Obtain the given port
								Port port = ci.getPort(portName);
								if (port != null) {
									// Toggle this port, or set to state specified
									final boolean newState = args.length >= 4 ? ParseUtil.parseBool(args[3]) : !port.isPowered();
									if (newState != port.isLeverPowered()) {
										port.setLeverPowered(newState);
										port.onPowerChange();
									}
									sender.sendMessage(ChatColor.GREEN + "Port has been toggled to the power '" + (newState ? "on" : "off") + "' state!");
								} else {
									sender.sendMessage(ChatColor.RED + "Port '" + portName + "' does not exist in circuit '" + circuitName + "'!");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Circuit instance '" + instanceName + "' was not found in circuit '" + circuitName + "'!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Circuit '" + circuitName + "' was not found!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Invalid amount of arguments for the toggle sub-command");
						sender.sendMessage(ChatColor.WHITE + "/redstone toggle [circuitname] [instancename] [port] ([state])");
					}
				} else if (cmdLabel.equals("delete")) {
					if (args.length > 0) {
						if (Circuit.delete(args[0])) {
							sender.sendMessage(ChatColor.YELLOW + "Circuit has been deleted!");
						} else {
							sender.sendMessage(ChatColor.RED + "Circuit not found!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Please enter a circuit name to delete too!");
					}
				} else if (cmdLabel.equals("list")) {
					MessageBuilder builder = new MessageBuilder();
					if (args.length == 0) {
						builder.yellow("Available circuits:").newLine();
						builder.setIndent(2).setSeparator(ChatColor.WHITE, " / ");
						for (String name : Circuit.getNames()) {
							builder.yellow(name);
						}
					} else {
						Circuit c = Circuit.get(args[0]);
						if (c == null) {
							builder.red("Circuit not found!");
						} else {
							builder.yellow("Available ports of ").white("'", c.name, "'").yellow(":").newLine();
							builder.setIndent(2).setSeparator(ChatColor.WHITE, " / ");
							for (Port p : c.getPorts()) {
								builder.yellow(p.name);
							}
						}
					}
					builder.send(sender);
				} else if (cmdLabel.equals("reload")) {
					disable();
					load();
					sender.sendMessage("All circuits have been reloaded!");
				} else if (cmdLabel.equals("save")) {
					if (args.length > 0) {
						if (sel.portnames.size() > 0) {
							String name = "";
							for (String arg : args) {
								if (name != "") name += " ";
								name += arg;
							}
							name = Util.fixName(name);
							File path = new File(getDataFolder() + File.separator + "circuits" + File.separator + name + ".circuit");
							if (!path.exists()) {
								Circuit.add(new CircuitCreator(player, sel).create(), name);
								sender.sendMessage("You created circuit '" + name + "'!");
							} else {
								sender.sendMessage("A circuit with this name already exists!");
							}
						} else {
							sender.sendMessage("Please define the ports!");
						}
					} else {
						sender.sendMessage("Please enter a circuit name too!");
					}
				} else {
					sender.sendMessage("Unknown sub command '" + cmdLabel + "'!");
				}
			} else {
				sender.sendMessage("Invalid arg count");
			}
		} else {
			sender.sendMessage("This command is only for players!");
		}
		return true;
	}

	@Override
	public void permissions() {
		this.loadPermission("redstonemania.use", PermissionDefault.OP, "If the player can use redstone mania's commands and ports");
	}
}
