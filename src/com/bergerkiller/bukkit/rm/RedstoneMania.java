package com.bergerkiller.bukkit.rm;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.bergerkiller.bukkit.rm.circuit.Circuit;
import com.bergerkiller.bukkit.rm.circuit.CircuitCreator;
import com.bergerkiller.bukkit.rm.circuit.CircuitInstance;


public class RedstoneMania extends JavaPlugin {

	public static RedstoneMania plugin;
	
	private final RMPlayerListener playerListener = new RMPlayerListener();
	private final RMBlockListener blockListener = new RMBlockListener();
	private final RMWorldListener worldListener = new RMWorldListener();
	
	private static Logger logger = Logger.getLogger("Minecraft");
	public static void log(Level level, String message) {
		logger.log(level, "[Redstone Mania] " + message);
	}
	
	private int updatetask = -1;
	public void onEnable() {
		plugin = this;
		
		//General registering
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.CHUNK_LOAD, worldListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.CHUNK_UNLOAD, worldListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.WORLD_UNLOAD, worldListener, Priority.Monitor, this);
		
		//Load
		Circuit.loadAll();
		
		//Start scheduler
		this.updatetask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Circuit c : Circuit.all()) {
					for (CircuitInstance ci : c.getInstances()) {
						ci.doPhysics();
					}
				}
			}
		}, 1, 1);
		
		this.getCommand("circuit").setExecutor(this);
		
        //final msg
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}
	
	public void onDisable() {
		getServer().getScheduler().cancelTask(this.updatetask);
		for (Circuit c : Circuit.all()) {
			for (CircuitInstance ci : c.getInstances()) {
				ci.save();
			}
		}
		System.out.println("Redstone Mania disabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			PlayerSelect sel = PlayerSelect.get(player);
			if (args.length > 0) {
				cmdLabel = args[0].toLowerCase();
				args = Util.remove(args, 0);
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
				} else if (cmdLabel.equals("setdelay") || cmdLabel.equals("delay")) {
					if (sel.isDelayable()) {
						if (args.length > 0) {
							int delay = 0;
							try {
								delay = Integer.parseInt(args[0]);
							} catch (Exception ex) {}
							sel.setDelay(delay);
							sender.sendMessage("This element now has a delay of " + delay + " ticks!");
						} else {
							sender.sendMessage("Please enter a delay for this element too!");
						}
					} else {
						sender.sendMessage("Please select a wire to set a port to first!");
					}
				} else if (cmdLabel.equals("delete")) {
					if (args.length > 0) {
						if (Circuit.delete(args[0])) {
							sender.sendMessage("Circuit has been deleted!");
						} else {
							sender.sendMessage("Circuit not found!");
						}
					} else {
						sender.sendMessage("Please enter a circuit name to delete too!");
					}
				} else if (cmdLabel.equals("list")) {
					Util.listElements(player, " | ", Circuit.getNames());
				} else if (cmdLabel.equals("save")) {
					if (args.length > 0) {
						if (sel.portnames.size() > 0) {
							String name = "";
							for (String arg : args) {
								if (name != "") name += " ";
								name += arg;
							}
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
				}
			} else {
				sender.sendMessage("Invalid arg count");
			}
		} else {
			sender.sendMessage("This command is only for players!");
		}
		return true;
	}
	
	
}
