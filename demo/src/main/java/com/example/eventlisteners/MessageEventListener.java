package com.example.eventlisteners;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.example.model.game.Game;
import com.example.model.player.Player;
import com.example.commands.InteractionEventListener;
import com.example.model.role.DetectiveRole;
import com.example.model.role.DoctorRole;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageEventListener extends ListenerAdapter {
    Game game = InteractionEventListener.game;
    List<TextChannel> textChannel;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        int targetIndex;
        int authorIndex = -1;
        this.textChannel = InteractionEventListener.textChannel;
        if (event.getChannelType() == ChannelType.PRIVATE && event.getAuthor().isBot() == false) {
            String response = event.getMessage().getContentRaw();
            targetIndex = Integer.parseInt(response);
            System.out.println("index:" + targetIndex );

            //Find who the Author is
            for(int i = 0; i < game.getPlayers().size(); i++){
                Player p = game.getPlayers().get(i);
                Member member = p.getPlayerThemselves();
                if (member.getId().equals(event.getAuthor().getId())) {
                    authorIndex = i;
                    System.out.println("Author ID FOUND");
                    break;
                }
            }

            if(authorIndex == -1){
                System.out.println("Author ID NOT  FOUND\n RETURNING");
                return;
            }

            for(int i = 0; i < game.getPlayers().size(); i++){
               
                
                Player p = game.getPlayers().get(authorIndex);
                if (p.getRole().getRole().equals("detective")) {
                    setDetectiveNightData(authorIndex, targetIndex,event);
                } 
                if (p.getRole().getRole().equals("doctor")) {
                    setDoctorNightData(authorIndex, targetIndex, event);
                } 
                if (p.getRole().getRole().equals("godfather")) {
                } 
                if (p.getRole().getRole().equals("sniper")) {
                }
            }

            

           
        }
        super.onMessageReceived(event);
        System.out.println("USER SAID:" + event.getMessage().getContentDisplay());
    }

    boolean setDetectiveNightData(int authorIndex, int targetIndex,MessageReceivedEvent event){
        DetectiveRole role = (DetectiveRole) game.getPlayers().get(authorIndex).getRole();
        role.setGussedPlayer(game.getPlayers().get(targetIndex));
        game.getPlayers().get(authorIndex).setRole(role);

        String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
        if(this.textChannel.size() == 0){
            System.out.println("Text Channel not found");
            return false;
        }
        if(temprole == "godfather"){
            event.getChannel().sendMessage("The Person you have guessed is Citizen\n").queue();

            System.out.println(textChannel.get(0).getName());
         
        } else if (temprole == "negotiator" || temprole == "normalmafia") {
            event.getChannel().sendMessage("The Person you have guessed is MAFIA!\n").queue();
            System.out.println(textChannel.get(0).getName());
         
        } else {
            event.getChannel().sendMessage("The Person you have guessed is Citizen\n").queue();
            System.out.println(textChannel.get(0).getName());
        }
        return true;
    }

    boolean setDoctorNightData(int authorIndex, int targetIndex,MessageReceivedEvent event){
        DoctorRole role = (DoctorRole) game.getPlayers().get(authorIndex).getRole();
        
        Boolean hasSavedHimself = role.getSavedHimself();

        String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
        if(this.textChannel.size() == 0){
            System.out.println("Text Channel not found");
            return false;
        }
        if( targetIndex == authorIndex ){
            if ( hasSavedHimself == false){
                role.setSavedPlayer(game.getPlayers().get(targetIndex));
                role.setSavedHimself(true);
                game.getPlayers().get(authorIndex).setRole(role);
                event.getChannel().sendMessage("The Person has been saved!\n").queue();
                return true;
            } else {
                event.getChannel().sendMessage("You cannot save yourself twice!!\n").queue();
                return false;
            }
        } else {
            role.setSavedPlayer(game.getPlayers().get(targetIndex));
            game.getPlayers().get(authorIndex).setRole(role);
            event.getChannel().sendMessage("The Person has been saved!\n").queue();
            return true;
        }
    }
}
