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
	@Argument(alias="k", required = true, description = "klass name")
	private static String klass = null;
	@Argument(description = "timetable login [optional]")
	private static String login = null;
	@Argument(description = "timetable password [optional]")
	private static String password = null;

	public static void main(String[] args) throws LoginException, IOException {
		Args.parseOrExit(Main.class, args);
		String zoneid = Calendar.getInstance().getTimeZone().getID();
		log.info("Token: {}, Klass: {}, Zone name: {}", token, klass, zoneid);
		TranslatableText.setLanguage("pl_pl");

		JDA bot = JDABuilder.createDefault(token).addEventListeners(
				new BotEventListener(klass, login, password, zoneid)
		).build();
	}
}
