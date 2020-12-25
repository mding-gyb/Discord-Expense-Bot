package michael.ExpenseBot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");

		try {
			if (args[0].equals("expense") && args[1].equals("help")) {
				EmbedBuilder help = new EmbedBuilder(); 
				help.setTitle("💰  Expense Bot Help 💰");
				help.setDescription("Please pay monies :)");
				help.setColor(0x805ff5);
				help.addField("General", "Add or remove expenses with \"expense add\" or \"expense remove\"."
						+ " Enter amount as **total paid** amount and names as **other** people (excluding self)." +
						" Check to whom and how much you owe with \"expense view\".", false);
				help.addField("Commands:", 
						"• expense help \n" +
								"• expense add (item) (names ...) (amounts ...) \n" +
								"• expense remove (item) (names ...) (amounts ...) \n" +
								"• expense view (name)", false);

				event.getChannel().sendTyping().queue();
				event.getChannel().sendMessage(help.build()).queue();
				help.clear();
			} else if (args[0].equals("expense") && args[1].equals("add") && args.length<5) {
				EmbedBuilder invalidAdd = new EmbedBuilder();
				invalidAdd.setDescription("Add must be in format \"expense add (item) (names ...) (amounts ...)\".");

				event.getChannel().sendTyping().queue();
				event.getChannel().sendMessage(invalidAdd.build()).queue();
			} else if (args[0].equals("expense") && args[1].equals("remove") && args.length<5) {
				EmbedBuilder invalidRemove = new EmbedBuilder();
				invalidRemove.setDescription("Remove must be in format \"expense remove (item) (names ...) (amounts ...)\".");

				event.getChannel().sendTyping().queue();
				event.getChannel().sendMessage(invalidRemove.build()).queue();
			} else if (args[0].equals("expense") && args[1].equals("remove") && args.length>=5) {
				// Set variables for logging payer names/balances before operation
				String [] names = new String [namesLength(args)];
				int [] oldVal = new int [namesLength(args)];
				int [] amounts = new int [args.length-(3+names.length)];

				// Check for if inputs valid
				if (names.length!=amounts.length) {
					EmbedBuilder invalidRemove = new EmbedBuilder();
					invalidRemove.setDescription("Remove must be in format \"expense remove (item) (names ...) (amounts ...)\".");

					event.getChannel().sendTyping().queue();
					event.getChannel().sendMessage(invalidRemove.build()).queue();
				} else {
					// Set payee info (event invoker user)
					String payee = event.getMember().getNickname();
					String payeeID = event.getMember().getId();
					// Build output embed
					EmbedBuilder remove = new EmbedBuilder();
					remove.setColor(0xeb6e60);
					remove.setTitle("🥳 Expense Remove Summary");
					remove.setDescription("Removal of expenses payable to " + payee +  ".");
					// Set up pre-remove and post-remove messages that will be appended to later
					MessageBuilder preremove = new MessageBuilder();
					MessageBuilder postremove = new MessageBuilder();

					// Pre-removal
					for (int i = 3; i < names.length+3; i ++) {
						names[i-3] = args[i];
						amounts[i-3] = Integer.valueOf(args[i+names.length]);
					}
					for (int i = 0; i < names.length; i ++) {
						Pair temp = new Pair(payeeID, names[i]);
						if(keyPresent(temp)) {
							oldVal[i] = getValue(temp);
							preremove.append(names[i]+": " + getValue(temp) + "\n");
						} else {
							oldVal[i] = 0;
							preremove.append(names[i]+": 0 \n");
							Main.expenses.put(temp, 0);
						}
					}

					// Post-removal
					for (int i = 0; i < amounts.length; i ++) {
						Pair temp = new Pair(payeeID, names[i]);
						minusValue(temp, amounts[i]); // adjust amount
						postremove.append(names[i]+": " + getValue(temp) + "\n");
					}

					// Adjust embed output for before and after operation.
					String[] arrOfStr = preremove.build().toString().split("\\(");
					remove.addField("Pre-Removal Owing Balances", arrOfStr[1].substring(0,arrOfStr[1].length()-1), false);
					String[] arrOfStr2 = postremove.build().toString().split("\\(");
					remove.addField("Post-Removal Owing Balances", arrOfStr2[1].substring(0,arrOfStr2[1].length()-1), false);

					// Send Message
					event.getChannel().sendTyping().queue();
					event.getChannel().sendMessage(remove.build()).queue();

					// Log transaction into text file
					// Log balances into text file
				}
			} else if (args[0].equals("expense") && args[1].equals("add") && args.length>=5) {
				// Set variables for logging payer names/balances before operation
				String [] names = new String [namesLength(args)];
				int [] oldVal = new int [namesLength(args)];
				int [] amounts = new int [args.length-(3+names.length)];
				
				// Check for if inputs valid
				if (names.length!=amounts.length) {
					EmbedBuilder invalidAdd = new EmbedBuilder();
					System.out.println(names.length);
					System.out.println(amounts.length);
					invalidAdd.setDescription("Add must be in format \"expense add (item) (names ...) (amounts ...)\".");

					event.getChannel().sendTyping().queue();
					event.getChannel().sendMessage(invalidAdd.build()).queue();
				} else {
					// Set payee info (event invoker user)
					String payee = event.getMember().getNickname();
					String payeeID = event.getMember().getId();
					// Build output embed
					EmbedBuilder add = new EmbedBuilder();
					add.setColor(0x26d90b);
					add.setTitle("🥳 Expense Add Summary");
					add.setDescription("Addition of expenses payable to " + payee +  ".");
					// Set up pre-add and post-add messages that will be appended to later
					MessageBuilder preadd = new MessageBuilder();
					MessageBuilder postadd = new MessageBuilder();

					// Pre-add
					for (int i = 3; i < names.length+3; i ++) {
						names[i-3] = args[i];
						amounts[i-3] = Integer.valueOf(args[i+names.length]);
					}
					for (int i = 0; i < names.length; i ++) {
						Pair temp = new Pair(payeeID, names[i]);
						if(keyPresent(temp)) {
							oldVal[i] = getValue(temp);
							preadd.append(names[i]+": " + getValue(temp) + "\n");
						} else {
							oldVal[i] = 0;
							preadd.append(names[i]+": 0 \n");
							Main.expenses.put(temp, 0);
						}
					}

					// Post-add
					for (int i = 0; i < amounts.length; i ++) {
						Pair temp = new Pair(payeeID, names[i]);
						addValue(temp, amounts[i]); // adjust amount
						postadd.append(names[i]+": " + getValue(temp) + "\n");
					}
					
					// Adjust embed output for before and after operation.
					String[] arrOfStr = preadd.build().toString().split("\\("); 
					add.addField("Pre-Addition Owing Balances", arrOfStr[1].substring(0,arrOfStr[1].length()-1), false);
					String[] arrOfStr2 = postadd.build().toString().split("\\("); 
					add.addField("Post-Addition Owing Balances", arrOfStr2[1].substring(0,arrOfStr2[1].length()-1), false);

					// Send Message
					event.getChannel().sendTyping().queue();
					event.getChannel().sendMessage(add.build()).queue();

					// Log transaction into text file
					// Log balances into text file
				}
			} else if (args[0].equals("expense")) {
				EmbedBuilder expense = new EmbedBuilder();
				expense.setDescription("Please enter a valid command. Try \"expense help\".");

				event.getChannel().sendTyping().queue();
				event.getChannel().sendMessage(expense.build()).queue();
			}

		} catch (Exception e) {
			System.out.println(e);
			EmbedBuilder expense = new EmbedBuilder();
			expense.setDescription("Please enter a valid command. Try \"expense help\".");

			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage(expense.build()).queue();
		}
	}
	private int namesLength(String[] arguments) {
		int length = 0;
		for(int i = 3; i < arguments.length; i ++) {
			try {
				int a = Integer.valueOf(arguments[i]);
				break;
			} catch (Exception e) {
				length++;
			}
		}
		return length;
	}
	private boolean keyPresent(Pair a) {
		for (Pair key : Main.expenses.keySet()) {
			if(a.equals(key)) {
				return true;
			}
		}
		return false;
	}
	private int getValue(Pair a) {
		if (keyPresent(a)) {
			for (Pair key : Main.expenses.keySet()) {
				if(a.equals(key)) {
					return Main.expenses.get(key);
				}
			}
		} 
		throw new IndexOutOfBoundsException("getValue for pair error");
		
	}
	private void addValue(Pair a, int amount) {
		if (keyPresent(a)) {
			for (Pair key : Main.expenses.keySet()) {
				if(a.equals(key)) {
					Main.expenses.put(key, Main.expenses.get(key)+amount);
				}
			}
		} else {
			throw new IndexOutOfBoundsException("setValue for pair error");
		}
	}
	private void minusValue(Pair a, int amount) {
		if (keyPresent(a)) {
			for (Pair key : Main.expenses.keySet()) {
				if(a.equals(key)) {
					System.out.println(Main.expenses.get(key));
					Main.expenses.put(key, Main.expenses.get(key)-amount);
					System.out.println(Main.expenses.get(key));
				}
			}
		} else {
			throw new IndexOutOfBoundsException("minusValue for pair error");
		}
	}

}
