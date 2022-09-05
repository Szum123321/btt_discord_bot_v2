package pl.szymon.btt_bot.bot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
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

    public BotEventListener(String klassName, String login, String passowrd, String zoneName) {
        registerCommands();

        botDataHandler = new BotDataHandler(klassName, login, passowrd);

        zoneId = ZoneId.of(zoneName);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        botDataHandler.updateData(BotDataHandler.DEFAULT_LOG_PRINT_HANDLER).run();

        log.info("Bot Ready. Available {} guilds", event.getGuildAvailableCount());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();

        log.trace("Recieved! {} from {}", message.getContentRaw(), message.getAuthor().getName());
        if(event.getAuthor().isBot())
            return;

        String content = message.getContentRaw();

        if(content.startsWith("!")) {
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
        } catch (Exception e) {
            log.error("An exception occurred!", e);
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
                        .then(
                                argument("command", StringArgumentType.word())
                                .executes(ctx -> {
                                    CommandNode<Message> commandNode = dispatcher.getRoot().getChild(StringArgumentType.getString(ctx, "command"));

                                    if(commandNode == null) {
                                        ctx.getSource().getChannel().sendMessage(new TranslatableText("command_not_found").getString()).queue();
                                        return 0;
                                    }

                                    if(commandNode.getRedirect() != null) {
                                        ctx.getSource().getChannel().sendMessage(
                                                new TranslatableText("help_" + commandNode.getRedirect().getName()).getString()
                                        ).queue();
                                    } else {
                                        ctx.getSource().getChannel().sendMessage(
                                                new TranslatableText("help_" + commandNode.getName()).getString()
                                        ).queue();
                                    }

                                    return 0;
                                })
                        )
                        .executes(ctx -> {
                            StringBuilder builder = new StringBuilder();

                            dispatcher.getRoot().getChildren().stream()
                                    .filter(commandNode -> commandNode instanceof LiteralCommandNode)
                                    .forEach(commandNode -> builder.append(commandNode.getName()).append(", "));

                            ctx.getSource().getChannel().sendMessage(new TranslatableText("help_generic", builder.toString()).getString()).queue();

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

                           if(lessonTime.isEmpty()) {
                               ctx.getSource().getChannel().sendMessage(new TranslatableText("generic_not_found").getString()).queue();
                                return 0;
                           }

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
                    var now = toLocalDateTime(ctx.getSource().getTimeCreated());
                    if(now.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
                        ctx.getSource().getChannel().sendMessage(new TranslatableText("it_is_weekend").getString()).queue();
                        return 0;
                    }

                    var timeOptional = botDataHandler.get().getLessonTimeTree().get(now.toLocalTime());
                    if(timeOptional.isEmpty()) {
                        ctx.getSource().getChannel().sendMessage(new TranslatableText("lesson_not_found").getString()).queue();
                        return 0;
                    }
                    var time = timeOptional.get();

                    if(time.getType().isLesson()) {
                        LessonGroup lessonGroup = botDataHandler.get().getLessonGroups()[now.getDayOfWeek().ordinal()].get(time.getId());

                        if(lessonGroup != null) {
                            ctx.getSource().getChannel().sendMessage(lessonGroup.print()).queue();
                        } else {
                            ctx.getSource().getChannel().sendMessage(new TranslatableText("lesson_not_found").getString()).queue();
                        }
                    } else {
                        ctx.getSource().getChannel().sendMessage(time.print().getString()).queue();
                    }

                    return 0;
                })
        );

        //redirecty nie działają na komendach bez argumentu
        dispatcher.register(literal("p").redirect(dispatcher.getRoot().getChild("przerwa")).executes(dispatcher.getRoot().getChild("przerwa").getCommand()));

        dispatcher.register(literal("n").redirect(dispatcher.getRoot().getChild("next")).executes(dispatcher.getRoot().getChild("next").getCommand()));

        dispatcher.register(literal("j").redirect(dispatcher.getRoot().getChild("jutro")).executes(dispatcher.getRoot().getChild("jutro").getCommand()));

        dispatcher.register(literal("t").redirect(dispatcher.getRoot().getChild("teraz")).executes(dispatcher.getRoot().getChild("teraz").getCommand()));

        dispatcher.register(literal("now").redirect(dispatcher.getRoot().getChild("teraz")).executes(dispatcher.getRoot().getChild("teraz").getCommand()));
    }

    private void printWholeDay(int dayOfWeek, CommandContext<Message> ctx) {
        if(dayOfWeek < 5) {
            StringBuilder builder = new StringBuilder();

            botDataHandler.get().getLessonGroups()[dayOfWeek].values().forEach(lessonGroup -> builder.append(lessonGroup.getLessonTime().print().getString()).append(": ").append(lessonGroup.print()).append("\n"));

            sendLongMessage(builder.toString(), ctx);
        } else {
            ctx.getSource().getChannel().sendMessage(new TranslatableText("it_is_weekend").getString()).queue();
        }
    }

    private void sendLongMessage(String str, CommandContext<Message> ctx) {
        while(str.length() >= 2000) {
            ctx.getSource().getChannel().sendMessage(str.substring(0, 1999)).queue();
            str = str.substring(2000, str.length() - 1);
        }

        if(str.length() > 0)
            ctx.getSource().getChannel().sendMessage(str).queue();
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
