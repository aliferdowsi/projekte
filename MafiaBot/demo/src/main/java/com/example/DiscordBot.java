package com.example;

import javax.security.auth.login.LoginException;

import com.example.commands.InteractionEventListener;
import com.example.eventlisteners.MessageEventListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot {
    public static void main(String[] args) throws LoginException{
        JDABuilder jdaBuilder = JDABuilder.createDefault("MTEyNjYzNDM4MTcxMzM1MDgyNg.GwCgs2.Y8_gjDh4f3NFdiml1RiuI8vdAqcdNBOgIupIW8");
        JDA jda = jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES).addEventListeners(new MessageEventListener(),new InteractionEventListener()).
        build();

        jda.upsertCommand("startmafia", "This command starts the mafia game").setGuildOnly(false).addOption(OptionType.INTEGER, "extramafias" , "Extra Normal Mafia's", true)
        .addOption(OptionType.INTEGER, "extracitizen" , "Extra Normal Citizen's", true).queue();
    }
}
