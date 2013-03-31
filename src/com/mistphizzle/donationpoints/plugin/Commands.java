package com.mistphizzle.donationpoints.plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class Commands {

	DonationPoints plugin;

	public Commands(DonationPoints instance) {
		this.plugin = instance;
		init();
	}

	private void init() {
		PluginCommand donationpoints = plugin.getCommand("donationpoints");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length < 1) {
					// Base Command
					s.sendMessage("-----�4DonationPoints Commands�f-----");
					s.sendMessage("�3/dp basic�f - Show basic DonationPoints commands.");
					s.sendMessage("�3/dp packages�f - Show the DonationPoints packages commands.");
					s.sendMessage("�3/dp admin�f - Show the DonationPoints Admin Commands.");
					return true;
					// Packages Commands
				} else if (args[0].equalsIgnoreCase("packages")) {
					s.sendMessage("-----�4DonationPoints Package Commands�f-----");
					if (s.hasPermission("donationpoints.package.info")) {
						s.sendMessage("�3/dp package info <packageName>�f - Shows package information.");
					} else {
						s.sendMessage("�cYou don't have permission to use any of the packages commands.");
					}
					// Admin Commands
				} else if (args[0].equalsIgnoreCase("admin")) {
					s.sendMessage("-----�4DonationPoints Admin Commands�f-----");
					if (s.hasPermission("donationpoints.give")) {
						s.sendMessage("�3/dp give <player> <amount>�f - Give points to a player.");
					} if (s.hasPermission("donationpoints.take")) {
						s.sendMessage("�3/dp take <player> <amount>�f - Take points from a player.");
					} if (s.hasPermission("donationpoints.set")) {
						s.sendMessage("�3/dp set <player> <amount>�f - Set a player's balance.");
					} if (s.hasPermission("donationpoints.version")) {
						s.sendMessage("�3/dp version�f - Shows the version of the plugin you're running.");
					} if (s.hasPermission("donationpoints.update")) {
						s.sendMessage("�3/dp update�f - Checks if there is an update available.");
					} if (s.hasPermission("donationpoints.reload")) {
						s.sendMessage("�3/dp reload�f - Reloads the Configuration / Packages.");
					} else {
						s.sendMessage("�cYou don't have any permission for ANY DonationPoints Admin Commands.");
					}
				} else if (args[0].equalsIgnoreCase("basic")) {
					s.sendMessage("-----�4DonationPoints Basic Commands�f-----");
					if (s.hasPermission("donationpoints.create")) {
						s.sendMessage("�3/dp create�f - Creates a points account for you.");
					}
					if (s.hasPermission("donationpoints.balance")) {
						s.sendMessage("�3/dp balance�f - Checks your points balance.");
					} if (s.hasPermission("donationpoints.transfer")) {
						s.sendMessage("�3/dp transfer <player> <amount>�f - Transfer Points.");
					} if (s.hasPermission("donationpoints.purchase")) {
						s.sendMessage("�3/dp purchase <package>�f - Purchase a package.");
					} else {
						s.sendMessage("�cYou don't have permission for any DonationPoints Basic Commands.");
					}
				} else if (args[0].equalsIgnoreCase("transfer")) {
					if (!plugin.getConfig().getBoolean("General.Transferrable")) {
						s.sendMessage("�cThis server does not allow DonationPoints to be transferred.");
						return true;
					}
					if (!s.hasPermission("donationpoints.transfer")) {
						s.sendMessage("�cYou don't have permission to do that!");
						return true;
					}
					if (!(s instanceof Player)) {
						s.sendMessage("�cThis command can only be performed by players.");
						return true;
					}
					if (args.length < 3) {
						s.sendMessage("�cNot enough arguments.");
						return true;
					}
					else {
						// Double transferamount = Double.parseDouble(args[2]);
						//        0        1     2
						// /dp transfer player amount
						// final Player target = Bukkit.getPlayer(args[1].toLowerCase());
						String sender = s.getName();
						String target = args[1];
						Double transferamount = Double.parseDouble(args[2]);
						if (!Methods.hasAccount(sender)) {
							s.sendMessage("�cYou don't have a DonationPoints account.");
							return true;
						}
						if (!Methods.hasAccount(target)) {
							s.sendMessage("�cThat player does not have a DonationPoints account.");
							return true;
						} if (target.equalsIgnoreCase(sender)) {
							s.sendMessage("�cYou can't transfer points to yourself.");
							return true;
						} else {
							if (transferamount > Methods.getBalance(sender)) {
								s.sendMessage("�cYou don't have enough points to transfer.");
								return true;
							} if (transferamount == 0) {
								s.sendMessage("�cYou can't transfer 0 points.");
								return true;
							}
						}
						Methods.addPoints(transferamount, target);
						Methods.removePoints(transferamount, sender);
						s.sendMessage("�aYou have sent �3" + transferamount + " points �ato �3" + target.toLowerCase() + ".");
						for (Player player: Bukkit.getOnlinePlayers()) {
							if (player.getName().equalsIgnoreCase(args[1])) {
								player.sendMessage("�aYou have received �3" + transferamount + " points �afrom �3" + sender.toLowerCase() + ".");
							}
						}
					}
				} else if (args[0].equalsIgnoreCase("reload") && s.hasPermission("donationpoints.reload")) {
					plugin.reloadConfig();
					try {
						plugin.firstRun();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					s.sendMessage("�aConfig / Packages reloaded.");
				} else if (args[0].equalsIgnoreCase("balance") && s.hasPermission("donationpoints.balance")) {
					if (args.length == 1) {
						if (!Methods.hasAccount(s.getName())) {
							s.sendMessage("�cYou don't have an account.");
						} else {
							Double balance = Methods.getBalance(s.getName());
							s.sendMessage("�aYou currently have: �3" + balance + " points.");
						}
					} else if (args.length == 2 && s.hasPermission("donationpoints.balance.others")) {
						String string = args[1];
						if (!Methods.hasAccount(string)) {
							s.sendMessage("�3" + string + "�c does not have an account.");
						} else {
							Double balance = Methods.getBalance(string);
							s.sendMessage("�3" + string + "�a has a balance of: �3" + balance + " points.");
						}
					}
				} else if (args[0].equalsIgnoreCase("create") && s.hasPermission("donationpoints.create")) {
					if (args.length == 1) {
						String string = s.getName();
						if (!Methods.hasAccount(string)) {
							Methods.createAccount(string);
							s.sendMessage("�aAn account has been created.");
						} else {
							s.sendMessage("�cYou already have an account.");
						}
					} if (args.length == 2 && s.hasPermission("donationpoints.create.others")) {
						//ResultSet rs2 = DBConnection.sql.readQuery("SELECT player FROM points_players WHERE player = '" + args[1].toLowerCase() + "';");
						String string = args[1];
						if (!Methods.hasAccount(string)) {
							Methods.createAccount(string);
							s.sendMessage("�aAn account has been created for: �3" + string.toLowerCase());
						} else if (Methods.hasAccount(string)) {
							s.sendMessage("�3" + string + "�c already has an account.");
						}
					}
				} else if (args[0].equalsIgnoreCase("give") && args.length == 3 && s.hasPermission("donationpoints.give")) {
					Double addamount = Double.parseDouble(args[2]);
					String target = args[1].toLowerCase();
					Methods.addPoints(addamount, target);
					s.sendMessage("�aYou have given �3" + target + " �aa total of �3" + addamount + " �apoints.");
				} else if (args[0].equalsIgnoreCase("take") && args.length == 3 && s.hasPermission("donationpoints.take")) {
					Double takeamount = Double.parseDouble(args[2]);
					String target = args[1].toLowerCase();
					Methods.removePoints(takeamount, target);
					s.sendMessage("�aYou have taken �3" + takeamount + "�a points from �3" + target);
				} else if (args[0].equalsIgnoreCase("confirm") && s.hasPermission("donationpoints.confirm")) {
					String sender = s.getName().toLowerCase();
					if (PlayerListener.purchases.containsKey(sender)) {
						String pack2 = PlayerListener.purchases.get(s.getName().toLowerCase());
						Double price2 = plugin.getConfig().getDouble("packages." + pack2 + ".price");
						int limit = plugin.getConfig().getInt("packages." + pack2 + ".limit");
						ResultSet numberpurchased = DBConnection.sql.readQuery("SELECT COUNT(*) AS size FROM points_transactions WHERE player = '" + s.getName().toLowerCase() + "' AND PACKAGE = '" + pack2 + "';");
//						ResultSet numberpurchased = DBConnection.sql.readQuery("SELECT * FROM points_transactions WHERE player = '" + s.getName().toLowerCase() + "' AND package = '" + pack2 + "';");
						if (plugin.getConfig().getBoolean("General.UseLimits")) {
							try {
								// numberpurchased.last();
								int size = numberpurchased.getInt("size");
								s.sendMessage("Size: " + size);
								// numberpurchased.beforeFirst();
								if (size >= limit) {
									s.sendMessage("�cYou can't purchase �3" + pack2 + "�c because you have reached the limit of �3" + limit);
								} else if (size < limit) {
									List<String> commands = plugin.getConfig().getStringList("packages." + pack2 + ".commands");
									for (String cmd : commands) {
										plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", sender.toLowerCase()));
									}
									Methods.removePoints(price2, sender);
									s.sendMessage("�aYou have just purchased: �3" + pack2 + "�a for �3" + price2 + " points");
									s.sendMessage("�aYour new balance is: " + Methods.getBalance(sender));
									PlayerListener.purchases.remove(s.getName().toLowerCase());
									if (plugin.getConfig().getBoolean("General.LogTransactions", true)) {
										Methods.logTransaction(sender, price2, pack2);
										DonationPoints.log.info(s.getName().toLowerCase() + " has made a purchase. It has been logged to the points_transactions table.");
									} else {
										DonationPoints.log.info(s.getName().toLowerCase() + " has purchased " + pack2);
									}
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else {
							List<String> commands = plugin.getConfig().getStringList("packages." + pack2 + ".commands");
							for (String cmd : commands) {
								plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("%player", sender.toLowerCase()));
							}
							Methods.removePoints(price2, sender);
							s.sendMessage("�aYou have just purchased: �3" + pack2 + "�a for �3" + price2 + " points.");
							s.sendMessage("�aYour new balance is: " + Methods.getBalance(sender));
							PlayerListener.purchases.remove(s.getName().toLowerCase());
							if (plugin.getConfig().getBoolean("General.LogTransactions", true)) {
								Methods.logTransaction(sender, price2, pack2);
								DonationPoints.log.info(s.getName().toLowerCase() + " has made a purchase. It has been logged to the points_transactions table.");
							} else {
								DonationPoints.log.info(s.getName().toLowerCase() + " has purchased " + pack2);
							}
						}
					} else {
						s.sendMessage("�cIt doesn't look like you've started a transaction.");
					}
				} else if (args[0].equalsIgnoreCase("set") && s.hasPermission("donationpoints.set")) {
					String target = args[1].toLowerCase();
					Double amount = Double.parseDouble(args[2]);
					Methods.setPoints(amount, target);
					s.sendMessage("�aYou have set �3" + target + "'s �abalance to �3" + amount + " points.");
				} else if (args[0].equalsIgnoreCase("package")) {
					if (args.length != 3) {
						s.sendMessage("�cYou have supplied an improper number of arguments.");
					if (args[1].equalsIgnoreCase("info")) {
						if (!s.hasPermission("donationpoints.package.info")) {
							s.sendMessage("�cYou don't have permission to do that!");
							return true;
						}
						String packName = args[2];
						Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
						String description = plugin.getConfig().getString("packages." + packName + ".description");
						s.sendMessage("-----�e" + packName + " Info�f-----");
						s.sendMessage("�aPackage Name:�3 " + packName);
						s.sendMessage("�aPrice:�3 " + price + "0");
						s.sendMessage("�aDescription:�3 " + description);
					}
				}
					
				} else if (args[0].equalsIgnoreCase("purchase")) {
					if (args.length < 2) {
						s.sendMessage("�cNot Enough Arguments.");
						s.sendMessage("�cCorrect Usage: �a/dp purchase <package>");
						return true;
					} if (!s.hasPermission("donationpoints.purchase")) {
						s.sendMessage("�cYou don't have permission to do that");
						return true;
					} else {
						String packName = args[1];
						Double price = plugin.getConfig().getDouble("packages." + packName + ".price");
						if (price == null) {
							s.sendMessage("�cPackage not found.");
							return true;
						} else {
							String username = s.getName().toLowerCase();
							Double balance = Methods.getBalance(username);
							if (!(balance >= price)) {
								s.sendMessage("�cYou do not have enough points to purchase that package.");
							} else if (balance >= price) {
								PlayerListener.purchases.put(username, packName);
								if (PlayerListener.purchases.containsKey(username)) {
									s.sendMessage("�aType �3/dp confirm �ato purchase �3" + packName + "�a for �3" + price + " points�a.");
								}
							}
						}
					}
				} else if (args[0].equalsIgnoreCase("version") && s.hasPermission("donationpoints.version")) {
					s.sendMessage("�aThis server is running �eDonationPoints �aversion �3" + plugin.getDescription().getVersion());
				} else {
					s.sendMessage("Not a valid DonationPoints command / Not Enough Permissions.");
				} return true;
			}
		}; donationpoints.setExecutor(exe);
	}

}
