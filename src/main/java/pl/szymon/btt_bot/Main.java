package pl.szymon.btt_bot;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import pl.szymon.btt_bot.bot.BotEventListener;
import pl.szymon.btt_bot.bot.TranslatableText;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Calendar;

@Log4j2
public class Main {
	@Argument(alias = "t", description = "discord app token")
	private static String token = null;
	@Argument(description = "timetable login")
	private static String login = null;
	@Argument(description = "timetable login")
	private static String password = null;

	@Argument(alias="k", required = true, description = "timetable login")
	private static String klass = null;

	@Argument(description = "Bot language")
	private static String lang = "pl_pl";

	public static void main(String[] args) throws LoginException, IOException {
		Args.parseOrExit(Main.class, args);
		TranslatableText.setLanguage(lang);

		JDA bot = JDABuilder.createDefault(args[0]).addEventListeners(
				new BotEventListener(token, password, login, Calendar.getInstance().getTimeZone().getID())
		).build();
	}
}
