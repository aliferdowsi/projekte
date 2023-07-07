package com.example.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.text.Style;
import javax.swing.text.StyledEditorKit;

import org.jetbrains.annotations.NotNull;

import com.example.model.game.Game;
import com.example.model.player.Player;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.factory.DefaultSendFactory;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MemberAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import com.example.model.role.*;
import com.example.model.role.Role;
public class InteractionEventListener extends ListenerAdapter {
    public static Game game = new Game();
    public static List<TextChannel> textChannel;
    private SlashCommandInteractionEvent event;
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        this.event = event;
        super.onSlashCommandInteraction(event);
        switch (event.getName()){
            case "startmafia":

                //Get the person who called the command
                Member member = event.getMember();
                if (member != null) {

                    GuildVoiceState voiceState = member.getVoiceState();
                    //Joins the voice call
                    if (voiceState != null && voiceState.inAudioChannel()) {
                        AudioChannelUnion voiceChannel = voiceState.getChannel();
                        event.getGuild().getAudioManager().openAudioConnection(voiceChannel);
                        //initalize values for start
                        this.textChannel = event.getGuild().getTextChannelsByName("mafia", true);
                        List<Member> members = voiceChannel.getMembers();
                        List<Player> players = new ArrayList<>();

                        // Add extra Mafia or Citizen 
                        List<OptionMapping> options = event.getOptions();
                        if (options.isEmpty() == false){
                            int extraMafia = options.get(0).getAsInt();
                            int extraCiziten = options.get(1).getAsInt();
                            game.initalizeGame(extraMafia,extraCiziten);
                            System.out.println("GAME SIZE " + game.getGameRoles().size());
                        } else {
                            System.out.println("No command was given");
                            return;
                        }
                        // Iterating in every user in call
                        int count = game.getGameRoles().size();
                        for (Member channelMember : members) {
                            if (count != 0 ){
                                Role role = game.getGameRoles().get(count-1);
                                System.out.println("HERE:" + channelMember.getEffectiveName());
                                Player tempPlayer = new Player(channelMember.getEffectiveName(),role);
                                tempPlayer.setPlayerThemselves(channelMember);
                                players.add(tempPlayer);
                                channelMember.getUser().openPrivateChannel()
                                    .flatMap(privateChannel -> privateChannel.sendMessage("Welcome to Mafia! Your Role is: \n "+ role.getRole() ))
                                    .queue();
                                count--;
                            } else {
                                break;
                            }
                        }
                        //Set the players connected with discord users
                        game.setPlayers(players); 

                        // Start First Day
                        startFirstDay();
                        
                    }
                }
                break;
        }
    }    

    public void startFirstDay(){
        if(this.textChannel.size() != 0){
                textChannel.get(0).sendMessage("WELCOME TO THE GAME OF MAFIA ! \n Starting First Day...\n").queue();
                System.out.println(textChannel.get(0).getName());
        } else {
            System.out.println("Text Channel not found");
            return;
        }

        for(Player player : this.game.getPlayers()){

           Member member = player.getPlayerThemselves();
           int intervalInSeconds = 2;
           
           // Send a message to the player
            // Send a message to the voice channel
            if (member != null) {
                String message = "Turn: " + member.getEffectiveName();
                textChannel.get(0).sendMessage(message).queue();
            } else {
                System.out.println("MEMBER NULL");
            }
            
            // Wait for the interval
            try {
                TimeUnit.SECONDS.sleep(intervalInSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String endMessage = "First day ended. Time to sleep!";
        textChannel.get(0).sendMessage(endMessage).queue();
        startNight();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("WE REACHED HERE");

    }

    public void startNight() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("detective")) {
                wakeDetective();
                System.out.println("WOKE detective");
            } 
            if (p.getRole().getRole().equals("doctor")) {
                wakeDoctor();
                System.out.println("WOKE DOCTOR");
            } 
            if (p.getRole().getRole().equals("godfather")) {
                wakeGodFather();
                System.out.println("GODFATHER");
            } 
            if (p.getRole().getRole().equals("sniper")) {
                wakeSniper();
                System.out.println("SNIPER");
            }
        }
        ReportNight();
    }
    
    public void wakeGodFather() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("godfather")) {
                Member member = p.getPlayerThemselves();
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("Choose someone to Kill!\n").queue();
                    String message ="";
                    for(int i = 0; i < this.game.getPlayers().size(); i++){
                        message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                    }
                    privateChannel.sendMessage(message+"\n").queue();
                    
                });
            }
        }
    }

    

    public void wakeDoctor(){
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("doctor")) {
                Member member = p.getPlayerThemselves();
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("Choose someone to SAVE!\n").queue();
                    String message ="";
                    for(int i = 0; i < this.game.getPlayers().size(); i++){
                        message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                    }
                    privateChannel.sendMessage(message+"\n").queue();
                });
            }
        }
    }

    public void wakeSniper(){
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("sniper")) {
                Member member = p.getPlayerThemselves();
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("Choose someone to SHOOT!\n").queue();
                    String message ="";
                    for(int i = 0; i < this.game.getPlayers().size(); i++){
                        message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                    }
                    privateChannel.sendMessage(message+"\n").queue();
                });
            }
        }
    }

    public void wakeDetective() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("detective")) {
                Member member = p.getPlayerThemselves();
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("Choose someone to guess their identity!\n").queue();
                    String message = "";
                    for (int i = 0; i < this.game.getPlayers().size(); i++) {
                        message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                    }
                    privateChannel.sendMessage(message + "\n").queue();
                });
            }
        }
    }
    

    public void ReportNight(){
        System.out.println("Printing the report:");
        for(Player p :  this.game.getPlayers()){
            if ( p.getRole().getRole() == "godfather" ){
                GodFatherRole godFatherRole = (GodFatherRole) p.getRole();
                System.out.println("godfather killed: " +  godFatherRole.getKilledPlayer().getName());
            } else if ( p.getRole().getRole() == "detective" ){
                DetectiveRole godFatherRole = (DetectiveRole) p.getRole();
                System.out.println("godfather killed: " +  godFatherRole.getGussedPlayer().getName());
            } else if ( p.getRole().getRole() == "doctor" ){
                DoctorRole godFatherRole = (DoctorRole) p.getRole();
                System.out.println("godfather killed: " +  godFatherRole.getSavedPlayer().getName());
            } else if ( p.getRole().getRole() == "sniper" ){
                SniperRole godFatherRole = (SniperRole) p.getRole();
                System.out.println("godfather killed: " +  godFatherRole.getShottedPlayer().getName());
            }
        }
    }


}
