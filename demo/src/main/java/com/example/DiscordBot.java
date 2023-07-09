package com.example;

import com.example.commands.InteractionEventListener;
import com.example.eventlisteners.MessageEventListener;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot {

    public static void main(String[] args) throws LoginException {
        JDABuilder jdaBuilder = JDABuilder.createDefault("x");
        JDA jda = jdaBuilder
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
            .addEventListeners(new MessageEventListener(), new InteractionEventListener())
            .build();

        jda
            .upsertCommand("startmafia", "This command starts the mafia game")
            .setGuildOnly(false)
            .addOption(OptionType.INTEGER, "extramafias", "Extra Normal Mafia's", true)
            .addOption(OptionType.INTEGER, "extracitizen", "Extra Normal Citizen's", true)
            .queue();
        jda
            .upsertCommand("analyzenight", "This command analyz what happened at night and tells the users")
            .setGuildOnly(false)
            .queue();
        jda
            .upsertCommand("reportnight", "This command reports what happened at night (ONLY FOR DEBUGGING)")
            .setGuildOnly(false)
            .queue();
        jda
            .upsertCommand("removeplayer", "This command reports what happened at night")
            .setGuildOnly(false)
            .addOption(OptionType.STRING, "playertoberemoved", "Player to be removed", true)
            .queue();
        jda
            .upsertCommand("startday", "Starts the Day")
            .setGuildOnly(false)
            .addOption(OptionType.INTEGER, "second", "Player's turn in Seconds", true)
            .queue();
        jda.upsertCommand("startnight", "Starts the Night").setGuildOnly(false).queue();
        jda.upsertCommand("startvote", "Starts the Voting").setGuildOnly(false).queue();
    }
}
