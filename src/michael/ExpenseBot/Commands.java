package michael.ExpenseBot;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {

	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String logMessage = event.getMessage().getContentRaw();
		String[] args = logMessage.split(" ");


		try {
			if (args[0].equals("expense") && args[1].equals("help")) {
				requestHelp(event);

			} else if (args[0].equals("expense") && args[1].equals("add") && args.length<5) {
				addError(event);

			} else if (args[0].equals("expense") && args[1].equals("remove") && args.length<5) {
				removeError(event);

			} else if (args[0].equals("expense") && args[1].equals("remove") && args.length>=5) {
				expenseRemove(event, args);

			} else if (args[0].equals("expense") && args[1].equals("add") && args.length>=5) {
				expenseAdd(event, args);

			} else if (args[0].equals("expense") && args[1].equals("view") && args.length == 2) {
				viewError(event);

			} else if (args[0].equals("expense") && args[1].equals("view") && args.length == 3) {
				expenseView(event, args);

			} else if (args[0].equals("expense")) {
				expenseError(event);

			}

		} catch (Exception e) {
			System.out.println(e);
			expenseError(event);
		}
	}

	// Helper Methods:
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
					Main.expenses.put(key, Main.expenses.get(key)-amount);
				}
			}
		} else {
			throw new IndexOutOfBoundsException("minusValue for pair error");
		}
	}

	private void viewError(GuildMessageReceivedEvent e) {
		EmbedBuilder invalidAdd = new EmbedBuilder();
		invalidAdd.setDescription("View must be in format \"expense view (name)\".");

		e.getChannel().sendTyping().queue();
		e.getChannel().sendMessage(invalidAdd.build()).queue();
	}

	private void expenseError(GuildMessageReceivedEvent e) {
		EmbedBuilder expense = new EmbedBuilder();
		expense.setDescription("Please enter a valid command. Try \"expense help\".");

		e.getChannel().sendTyping().queue();
		e.getChannel().sendMessage(expense.build()).queue();
	}

	private void removeError(GuildMessageReceivedEvent e) {
		EmbedBuilder invalidRemove = new EmbedBuilder();
		invalidRemove.setDescription("Remove must be in format \"expense remove (item) (names ...) (amounts ...)\".");

		e.getChannel().sendTyping().queue();
		e.getChannel().sendMessage(invalidRemove.build()).queue();
	}

	private void requestHelp(GuildMessageReceivedEvent e) {
		EmbedBuilder help = new EmbedBuilder(); 
		help.setTitle("💰  Expense Bot Help 💰");
		help.setDescription("Please pay monies :)");
		help.setColor(0x805ff5);
		help.setThumbnail(Main.jda.getSelfUser().getAvatarUrl());
		help.addField("General", "Add or remove expenses with \"expense add\" or \"expense remove\"."
				+ " Enter amount as **total paid** amount and names as **other** people (excluding self)." +
				" Check to whom and how much you owe with \"expense view\".", false);
		help.addField("Commands:", 
				"• expense help \n" +
						"• expense add (item) (names ...) (amounts ...) \n" +
						"• expense remove (item) (names ...) (amounts ...) \n" +
						"• expense view (name)", false);

		e.getChannel().sendTyping().queue();
		e.getChannel().sendMessage(help.build()).queue();
	}

	private void addError(GuildMessageReceivedEvent e) {
		EmbedBuilder invalidAdd = new EmbedBuilder();
		invalidAdd.setDescription("Add must be in format \"expense add (item) (names ...) (amounts ...)\".");

		e.getChannel().sendTyping().queue();
		e.getChannel().sendMessage(invalidAdd.build()).queue();
	}

	private void expenseRemove (GuildMessageReceivedEvent event, String[] args) {
		// Set variables for logging payer names/balances before operation
		String [] names = new String [namesLength(args)];
		int [] oldVal = new int [namesLength(args)];
		int [] amounts = new int [args.length-(3+names.length)];

		// Check for if inputs valid
		if (names.length!=amounts.length) {
			removeError(event);

		} else {
			// Set payee info (event invoker user)
			String payee = event.getMember().getNickname();
			String payeeID = event.getMember().getId();
			// Build output embed
			EmbedBuilder remove = new EmbedBuilder();
			remove.setColor(0xeb6e60);
			remove.setTitle("🤑 Expense Remove Summary");
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

			// Log command into text file
			try {
				FileWriter myWriter = new FileWriter("command-logs.txt", true);
				String logMessage = event.getMessage().getContentRaw();
				myWriter.write(getDate()+" "+payee+": "+logMessage.substring(8,logMessage.length()));
				myWriter.write(System.getProperty("line.separator"));
				myWriter.close();
				System.out.println("Successfully wrote to the file.");
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}

			// Log balances into text file
			try {
				FileWriter myWriter = new FileWriter("owing-balances.txt");
				String title = String.format("%-15s %-15s %s", "Owing to:", "Ower", "Amount");
				myWriter.write(title);
				myWriter.write(System.getProperty("line.separator"));
				for (Pair pairs: Main.expenses.keySet()){
					String owing = Main.jda.getUserById(pairs.x).getName();
					String ower = pairs.y;
					String value = Main.expenses.get(pairs).toString();
					myWriter.write(String.format("%-15s %-15s %s", owing, ower, value)); 
					myWriter.write(System.getProperty("line.separator"));
				} 
				myWriter.close();
				System.out.println("Successfully wrote to the file.");
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}
	}

	private void expenseAdd(GuildMessageReceivedEvent event, String[] args) {
		// Set variables for logging payer names/balances before operation
		String [] names = new String [namesLength(args)];
		int [] oldVal = new int [namesLength(args)];
		int [] amounts = new int [args.length-(3+names.length)];

		// Check for if inputs valid
		if (names.length!=amounts.length) {
			addError(event);

		} else {
			// Set payee info (event invoker user)
			String payee = event.getMember().getNickname();
			String payeeID = event.getMember().getId();
			// Build output embed
			EmbedBuilder add = new EmbedBuilder();
			add.setColor(0x26d90b);
			add.setTitle("🤑 Expense Add Summary");
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

			// Log command into text file
			try {
				FileWriter myWriter = new FileWriter("command-logs.txt", true);
				String logMessage = event.getMessage().getContentRaw();
				myWriter.write(getDate()+" "+payee+": "+logMessage.substring(8,logMessage.length()));
				myWriter.write(System.getProperty("line.separator"));
				myWriter.close();
				System.out.println("Successfully wrote to the file.");
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}

			// Log balances into text file
			try {
				FileWriter myWriter = new FileWriter("owing-balances.txt");
				String title = String.format("%-15s %-15s %s", "Owing to:", "Ower", "Amount");
				myWriter.write(title);
				myWriter.write(System.getProperty("line.separator"));
				for (Pair pairs: Main.expenses.keySet()){
					String owing = Main.jda.getUserById(pairs.x).getName();
					String ower = pairs.y;
					String value = Main.expenses.get(pairs).toString();
					myWriter.write(String.format("%-15s %-15s %s", owing, ower, value)); 
					myWriter.write(System.getProperty("line.separator"));
				} 
				myWriter.close();
				System.out.println("Successfully wrote to the file.");
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}
	}

	private void expenseView(GuildMessageReceivedEvent event, String[] args) {
		String query = args[2];
		MessageBuilder output = new MessageBuilder();
		EmbedBuilder eView = new EmbedBuilder();
		String payee = event.getMember().getNickname();
		eView.setColor(0xe6bd19);
		eView.setTitle("👀 Expenses Payable Summary");
		eView.setDescription("Expenses owing for user " + query +  ".");
		for (Pair key : Main.expenses.keySet()) {
			if(query.equals(key.y)) {
				output.append(Main.jda.retrieveUserById(key.x).complete().getName()+": " + getValue(key) + "\n");
			}
		}
		if (output.length() == 0) {
			output.append("No outstanding balances owing.");
		}
		String[] arrOfStr = output.build().toString().split("\\("); 
		eView.addField("Balances Outstanding", arrOfStr[1].substring(0,arrOfStr[1].length()-1), false);
		event.getChannel().sendTyping().queue();
		event.getChannel().sendMessage(eView.build()).queue();

	}

	private static String getDate() { 
		Date today = new Date(); 
		Calendar cal = Calendar.getInstance();
		cal.setTime(today); 
		int day = cal.get(Calendar.DAY_OF_MONTH); 
		int month = cal.get(Calendar.MONTH);

		return month+"/"+day;
	} 


}
