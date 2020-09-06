package pl.szymon.btt_bot.bot;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Log4j2
public class LocalTimeArgumentType implements ArgumentType<LocalTime> {
    public static LocalTime getLocalTime(final CommandContext<?> context, final String name) {
        return context.getArgument(name, LocalTime.class);
    }

    @Override
    public LocalTime parse(StringReader reader) throws CommandSyntaxException {
        LocalTime result;

        int start = reader.getCursor();

        String val = reader.getString().substring(start, start + 5);

        reader.setCursor(start + 5);

        try {
            result = LocalTime.parse(val);
        } catch (DateTimeParseException e) {
            String builder = "Error at: " +
                    e.getParsedString() +
                    '\n' +
                    " ".repeat(Math.max(0, 10 + e.getErrorIndex())) +
                    "^";
            throw new SimpleCommandExceptionType(new LiteralMessage(builder)).create();
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return null;
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("11:23", "08:43", "23:59");
    }
}
