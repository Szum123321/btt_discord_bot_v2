package pl.szymon.btt_bot.bot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.ToString;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

@ToString
public class TranslatableText {
    private static final MessageFactory messageFactory = new ParameterizedMessageFactory();
    private static Language language;

    private final String translationKey;
    private final Object[] args;

    public static void setLanguage(String lang) throws IOException {
        language = new Language(lang);
    }

    public TranslatableText(String key, Object ...args) {
        this.translationKey = key;
        this.args = args;
    }

    public String getString() {
        String patter = language.getTranslationPatter(translationKey);

        if(patter != null) {
            return messageFactory.newMessage(patter, args).getFormattedMessage();
        } else {
            return translationKey;
        }
    }

    public static class Language {
        private final Map<String, String> dataMap;

        public Language(String lang) throws IOException {
            Gson gson = new Gson();

            try(InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("lang/" + lang + ".json")))){
                dataMap = gson.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            }
        }

        public String getTranslationPatter(String key) {
            return dataMap.get(key);
        }
    }
}
