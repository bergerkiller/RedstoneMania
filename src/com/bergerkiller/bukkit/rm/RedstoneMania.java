package com.bergerkiller.bukkit.rm;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.bergerkiller.bukkit.common.Common;
import com.bergerkiller.bukkit.common.MessageBuilder;
import com.bergerkiller.bukkit.common.PluginBase;
import com.bergerkiller.bukkit.common.Task;
import com.bergerkiller.bukkit.common.permissions.NoPermissionException;
import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.common.utils.StringUtil;
import com.bergerkiller.bukkit.rm.circuit.Circuit;
import com.bergerkiller.bukkit.rm.circuit.CircuitCreator;
import com.bergerkiller.bukkit.rm.circuit.CircuitInstance;
import com.bergerkiller.bukkit.rm.circuit.CircuitProvider;
import com.bergerkiller.bukkit.rm.element.Port;

public class RedstoneMania extends PluginBase {
	public static RedstoneMania plugin;
	private Task updatetask;

	@Override
	public void enable() {
		plugin = this;

		// General registering
		this.register(RMListener.class);
		this.register("circuit");

		// Load
		CircuitProvider.loadAll();

		// Start scheduler
		this.updatetask = new Task(this) {
			public void run() {
				for (Circuit c : CircuitProvider.all()) {
					for (CircuitInstance ci : c.getInstances()) {
						ci.onTick();
					}
				}
			}
		}.start(1, 1);
	}

	@Override
	public int getMinimumLibVersion() {
		return Common.VERSION;
	}

	@Override
	public void disable() {
		Task.stop(this.updatetask);
		for (Circuit c : CircuitProvider.all()) {
			for (CircuitInstance ci : c.getInstances()) {
				ci.save();
			}
		}
		CircuitProvider.clearAll();
	}

	@Override
	public boolean command(CommandSender sender, String cmdLabel, String[] args) {
		if (!(sender instanceof Player) || !sender.hasPermission("redstonemania.use")) {
			throw new NoPermissionException();
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Not enough arguments for this command!");
			return true;
		}
		Player player = (Player) sender;
		PlayerSelect sel = PlayerSelect.get(player);
		cmdLabel = args[0].toLowerCase();
		args = StringUtil.remove(args, 0);

		// Handle commands
		if (cmdLabel.equals("setport") || cmdLabel.equals("port")) {
			if (sel.getType() == Material.REDSTONE_WIRE) {
				if (args.length > 0) {
					final String name = StringUtil.combine(" ", args);
					sel.setPort(name);
					sender.sendMessage(ChatColor.GREEN + "You added port " + ChatColor.YELLOW + "'" + name + "'" + ChatColor.GREEN + "!");
				} else {
					sender.sendMessage(ChatColor.RED + "Please enter a port name too!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Please select a wire to set a port to first!");
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
				sel.clickdelay = ParseUtil.parseInt(args[0], -1);
				if (sel.clickdelay < 0) {
					sender.sendMessage(ChatColor.YELLOW + "Click-set delay is now disabled!");
				} else {
					sender.sendMessage(ChatColor.GREEN + "Click-set delay is now set at " + sel.clickdelay + " ticks!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Please specify the delay to apply when clicking (or use none)");
			}
		} else if (cmdLabel.equals("setdelay") || cmdLabel.equals("delay")) {
			if (sel.isDelayable()) {
				if (args.length > 0) {
					int delay = ParseUtil.parseInt(args[0], 0);
					sel.setDelay(delay);
					sender.sendMessage(ChatColor.YELLOW + "This element now has a delay of " + delay + " ticks!");
				} else {
					sender.sendMessage(ChatColor.RED + "Please enter a delay for this element too!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Please select a block that can have delay first!");
			}
		} else if (cmdLabel.equals("toggle")) {
			if (args.length >= 3) {
				String circuitName = args[0];
				String instanceName = args[1];
				String portName = args[2];
				// Try to find the circuit and instance
				Circuit c = CircuitProvider.get(circuitName);
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
				if (CircuitProvider.delete(args[0])) {
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
				for (String name : CircuitProvider.getNames()) {
					builder.yellow(name);
				}
			} else {
				Circuit c = CircuitProvider.get(args[0]);
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
			CircuitProvider.loadAll();
			sender.sendMessage(ChatColor.YELLOW + "All circuits have been reloaded!");
		} else if (cmdLabel.equals("save")) {
			if (args.length > 0) {
				if (!sel.getPorts().isEmpty()) {
					String name = Util.fixName(StringUtil.combine(" ", args));
					File path = new File(getDataFolder() + File.separator + "circuits" + File.separator + name + ".circuit");
					if (!path.exists()) {
						CircuitProvider.add(new CircuitCreator(player, sel).create(), name);
						sender.sendMessage(ChatColor.GREEN + "You created circuit '" + name + "'!");
					} else {
						sender.sendMessage(ChatColor.RED + "A circuit with this name already exists!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Please define the ports!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Please enter a circuit name too!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown sub command '" + cmdLabel + "'!");
		}
		return true;
	}

	@Override
	public void permissions() {
		this.loadPermission("redstonemania.use", PermissionDefault.OP, "If the player can use redstone mania's commands and ports");
	}
}
