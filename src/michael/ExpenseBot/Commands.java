package michael.ExpenseBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");

		if (args.length == 1 && args[0].equals("expense")) {
			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage("Please enter a valid command.").queue();
		}  else if (args[0].equals("expense") && args[1].equals("help")) {
			EmbedBuilder help = new EmbedBuilder(); 
			help.setTitle("💰 Expense Bot Help");
			help.setDescription("Please pay monies :)");
			help.setColor(0x805ff5);
			help.addField("General", "Add or remove expenses with \"expense add\" or \"expense remove\"."
					+ " Enter amount as **total paid** amount and names as **other** people (excluding self)." +
					" Check to whom and how much you owe with \"expense view\".", false);
			help.addField("Commands:", 
					"• expense help \n" +
							"• expense add (amount) (item) (names ...) \n" +
							"• expense remove (amount) (item) (names ...) \n" +
							"• expense view (name)", false);

			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage(help.build()).queue();
			help.clear();
		} else if (args[0].equals("expense") && args[1].equals("add") && args.length==2) {
			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage("Add must be in format \"expense add amount Name ...\" ").queue();
		} else if (args[0].equals("expense") && args[1].equals("remove") && args.length==2) {
			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage("Remove must be in format \"expense remove amount Name ...\" ").queue();
		} else if (args[0].equals("expense") && args[1].equals("remove") && args.length==2) {
			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage("Remove must be in format \"expense remove amount Name ...\" ").queue();
		}

	}
}
