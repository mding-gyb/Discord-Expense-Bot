package michael.ExpenseBot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;

public class Main {
	public static JDA jda;
	// public static Map<Pair,Integer> expenses = new HashMap<Pair,Integer>();
	
	public static void main(String[]args) throws LoginException{
		jda = new JDABuilder(AccountType.BOT).setToken("filler").build();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		jda.getPresence().setActivity(Activity.streaming("Live View", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
		
		jda.addEventListener(new Commands());
	}
}
