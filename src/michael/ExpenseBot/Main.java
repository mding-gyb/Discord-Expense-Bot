package michael.ExpenseBot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Main {
	public static JDA jda;
	
	public static void main(String[]args) throws LoginException{
		jda = new JDABuilder(AccountType.BOT).setToken("NzkxODAwMDYzMjgxMzMyMjM0.X-UbRg.a4BS2k-Q50VNDrHC5NCZEe2MFOw").build();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		jda.getPresence().setActivity(Activity.streaming("Live View", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
		
		jda.addEventListener(new Commands());
	}
}
