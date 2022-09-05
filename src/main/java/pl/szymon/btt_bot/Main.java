package pl.szymon.btt_bot;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import pl.szymon.btt_bot.bot.BotEventListener;
import pl.szymon.btt_bot.bot.TranslatableText;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@Log4j2
public class Main {
	public static void main(String[] args) throws LoginException, IOException {
		if(args.length < 2) {
			log.error("No token, or klass name provided!");
			System.exit(1);
		}

		TranslatableText.setLanguage("pl_pl");

		JDA bot = JDABuilder.createDefault(args[0]).addEventListeners(
				new BotEventListener(args[1], args.length >= 4 ? args[2]: null, args.length >= 4 ? args[3]: null, "Europe/Warsaw")
		).build();
	}
}
