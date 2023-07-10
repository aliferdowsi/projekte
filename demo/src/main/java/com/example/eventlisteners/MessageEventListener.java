package com.example.eventlisteners;

import com.example.commands.InteractionEventListener;
import com.example.configuration.PlayerManager;
import com.example.gameplay.GamePlay;
import com.example.model.game.Game;
import com.example.model.player.Player;
import com.example.model.role.DetectiveRole;
import com.example.model.role.DoctorRole;
import com.example.model.role.GodFatherRole;
import com.example.model.role.NegotiatorRole;
import com.example.model.role.NormalMafiaRole;
import com.example.model.role.SniperRole;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageEventListener extends ListenerAdapter {

    Game game = InteractionEventListener.game;
    List<TextChannel> textChannel;
    PlayerManager playerManager = PlayerManager.get();
    String currentDirectory = System.getProperty("user.dir");
    GamePlay gamePlay;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        this.textChannel = InteractionEventListener.textChannel;
        int targetIndex;
        int authorIndex = -1;
        // Processing Players Night Commands
        if (event.getChannelType() == ChannelType.PRIVATE && event.getAuthor().isBot() == false) {
            String response = event.getMessage().getContentRaw();
            targetIndex = Integer.parseInt(response);
            System.out.println("index:" + targetIndex);

            // Find who the Author is
            for (int i = 0; i < game.getPlayers().size(); i++) {
                Player p = game.getPlayers().get(i);
                Member member = p.getPlayerThemselves();
                if (member.getId().equals(event.getAuthor().getId())) {
                    authorIndex = i;
                    System.out.println("Author ID FOUND");
                    break;
                }
            }

            if (authorIndex == -1) {
                System.out.println("Author ID NOT  FOUND\n RETURNING");
                return;
            }
            Player p = game.getPlayers().get(authorIndex);
            if (p.getRole().getRole().equals("detective")) {
                GamePlay.setDetectiveNightData(authorIndex, targetIndex, event);
            }
            if (p.getRole().getRole().equals("doctor")) {
                GamePlay.setDoctorNightData(authorIndex, targetIndex, event);
            }
            if (p.getRole().getRole().equals("godfather")) {
                GamePlay.setGodfatherNightData(authorIndex, targetIndex, event);
            }

            if (p.getRole().getRole().equals("sniper")) {
                GamePlay.setSniperNightData(authorIndex, targetIndex, event);
            }

            // If godfather is out, we call the other mafias
            if (game.getIsGodfatherOut() == true &&
                    (p.getRole().getRole().equals("negotiator") || p.getRole().getRole().equals("normalmafia"))) {
                GamePlay.setGodfatherSubstituteNightData(authorIndex, targetIndex, event);
            }
        }
        super.onMessageReceived(event);
        System.out.println(event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());
    }

    public void playAudio(String trackname) {
        SlashCommandInteractionEvent event = InteractionEventListener.eventt;
        playerManager.play(event.getGuild(), currentDirectory + "/src/main/voicerecording/" + trackname + ".m4a");
    }
}
