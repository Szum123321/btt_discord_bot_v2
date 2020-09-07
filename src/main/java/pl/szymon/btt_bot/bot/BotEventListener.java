package pl.szymon.btt_bot.bot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pl.szymon.btt_bot.structures.LessonGroup;
import pl.szymon.btt_bot.structures.time.LessonTime;

import java.time.*;
import java.util.Optional;

@Log4j2
public class BotEventListener extends ListenerAdapter {
    private final CommandDispatcher<Message> dispatcher = new CommandDispatcher<>();

    private final BotDataHandler botDataHandler;
    private static ZoneId zoneId;

    public BotEventListener(String klassName, String zoneName) {
        registerCommands();

        botDataHandler = new BotDataHandler(klassName);

        zoneId = ZoneId.of(zoneName);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        botDataHandler.updateData(BotDataHandler.DEFAULT_PRINT_HANDLER).run();

        log.info("Bot Ready. Available {} guilds", event.getGuildAvailableCount());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();

        if(event.getAuthor().isBot())
            return;

        String content;

        if(message.getContentRaw().startsWith("!")) {
            content = message.getContentRaw().substring(1);
        } else {
            return;
        }

        try {
            dispatcher.execute(content, message);
        } catch (CommandSyntaxException e) {
            if(e.getType() != CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand()) {
                event.getMessage().getChannel().sendMessage(e.getMessage()).queue();
            }
        } catch (RuntimeException e) {
            log.error(e);
        }
    }

    private void registerCommands() {
        dispatcher.register(
                literal("ping")
                        .executes(ctx -> {
                            ctx.getSource().getChannel().sendMessage("Pong!").queue();
                            return 0;
                        })
        );

        dispatcher.register(
                literal("help")
                    .executes(ctx -> {
                        ctx.getSource().getChannel().sendMessage(new TranslatableText("generic_help").getString()).queue();
                        return 0;
                    })
        );

        dispatcher.register(
                literal("update")
                    .executes(ctx -> {
                        final MessageChannel channel = ctx.getSource().getChannel();
                        new Thread(botDataHandler.updateData((level, text) -> channel.sendMessage(text.getString()).queue())).start();
                        return 0;
                    })
        );

       dispatcher.register(
               literal("next")
                    .executes(ctx -> {
                        LocalDateTime localDateTime = toLocalDateTime(ctx.getSource().getTimeCreated());

                        Optional<LessonTime> lessonTime = getLessonTime(ctx);

                        int dayOfWeek = localDateTime.getDayOfWeek().ordinal();

                        if(lessonTime.isPresent()) {
                            int nextId = lessonTime.get().getId() + 1;
                            LessonGroup lessonGroup = botDataHandler.get().getLessonGroups()[dayOfWeek].get(nextId);

                            if(lessonGroup != null) {
                                ctx.getSource().getChannel().sendMessage(lessonGroup.print()).queue();
                            } else {
                                ctx.getSource().getChannel().sendMessage(new TranslatableText("lesson_not_found").getString()).queue();
                            }
                        } else {
                            ctx.getSource().getChannel().sendMessage(new TranslatableText("generic_not_found").getString()).queue();
                        }

                        return 0;
                    })
       );

       dispatcher.register(
               literal("przerwa")
                       .executes(ctx -> {
                           Optional<LessonTime> lessonTime = getLessonTime(ctx);

                           if(lessonTime.isPresent()) {
                               if(lessonTime.get().getType().isLesson()) {
                                   LessonTime pause = botDataHandler.get().getPauseTimes().get(lessonTime.get().getId());
                                   if(pause != null) {
                                       ctx.getSource().getChannel().sendMessage(pause.print().getString()).queue();
                                   } else {
                                       ctx.getSource().getChannel().sendMessage(new TranslatableText("generic_not_found").getString()).queue();
                                   }
                               } else {
                                   ctx.getSource().getChannel().sendMessage(lessonTime.get().print().getString()).queue();
                               }
                           } else {
                               ctx.getSource().getChannel().sendMessage(new TranslatableText("generic_not_found").getString()).queue();
                           }

                           return 0;
                       })
       );

       dispatcher.register(
               literal("jutro")
                    .executes(ctx -> {
                        printWholeDay(toLocalDateTime(ctx.getSource().getTimeCreated()).getDayOfWeek().plus(1).ordinal(), ctx);

                        return 0;
                    })
       );

        dispatcher.register(
                literal("dzisiaj")
                        .executes(ctx -> {
                            printWholeDay(toLocalDateTime(ctx.getSource().getTimeCreated()).getDayOfWeek().ordinal(), ctx);

                            return 0;
                        })
        );

        dispatcher.register(
                literal("teraz").executes(ctx -> {
                    LocalDateTime localDateTime = toLocalDateTime(ctx.getSource().getTimeCreated());
                    Optional<LessonTime> optionalLessonTime = botDataHandler.get().getLessonTimeTree().get(localDateTime.toLocalTime());

                    if(localDateTime.getDayOfWeek().ordinal() < 5) {
                        if(optionalLessonTime.isPresent()) {
                            LessonTime lessonTime = optionalLessonTime.get();

                            if(lessonTime.getType().isLesson()) {
                                LessonGroup lessonGroup = botDataHandler.get().getLessonGroups()[localDateTime.getDayOfWeek().ordinal()].get(lessonTime.getId());

                                if(lessonGroup != null) {
                                    ctx.getSource().getChannel().sendMessage(lessonGroup.print()).queue();
                                } else {
                                    ctx.getSource().getChannel().sendMessage(new TranslatableText("lesson_not_found").getString()).queue();
                                }
                            } else {
                                ctx.getSource().getChannel().sendMessage(lessonTime.print().getString()).queue();
                            }
                        } else {
                            ctx.getSource().getChannel().sendMessage(new TranslatableText("generic_not_found").getString()).queue();
                        }
                    } else {
                        ctx.getSource().getChannel().sendMessage(new TranslatableText("it_is_weekend").getString()).queue();
                    }

                    return 0;
                })
        );
    }

    private void printWholeDay(int dayOfWeek, CommandContext<Message> ctx) {
        if(dayOfWeek < 5) {
            StringBuilder builder = new StringBuilder();

            botDataHandler.get().getLessonGroups()[dayOfWeek].values().forEach(lessonGroup -> builder.append(lessonGroup.getLessonTime().print().getString()).append(": ").append(lessonGroup.print()).append("\n"));

            ctx.getSource().getChannel().sendMessage(builder.toString()).queue();
        } else {
            ctx.getSource().getChannel().sendMessage(new TranslatableText("it_is_weekend").getString()).queue();
        }
    }

    private Optional<LessonTime> getLessonTime(CommandContext<Message> ctx) {
        return botDataHandler.get().getLessonTimeTree().get(toLocalDateTime(ctx.getSource().getTimeCreated()).toLocalTime());
    }

    private static LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return LocalDateTime.ofInstant(offsetDateTime.toInstant(), zoneId);
    }

    private static <T> RequiredArgumentBuilder<Message, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    private static LiteralArgumentBuilder<Message> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }
}
