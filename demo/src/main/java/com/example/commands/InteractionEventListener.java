package com.example.commands;

import com.example.configuration.PlayerManager;
import com.example.model.game.Game;
import com.example.model.player.Player;
import com.example.model.role.*;
import com.example.model.role.Role;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.Style;
import javax.swing.text.StyledEditorKit;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.factory.DefaultSendFactory;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import com.example.gameplay.GamePlay;

public class InteractionEventListener extends ListenerAdapter {

    public static Game game = new Game();
    public static List<TextChannel> textChannel;
    public static SlashCommandInteractionEvent eventt;
    private GamePlay gamePlay;
    PlayerManager playerManager = PlayerManager.get();
    String currentDirectory = System.getProperty("user.dir");

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        super.onSlashCommandInteraction(event);
        switch (event.getName()) {
            case "startmafia":
                // Get the person who called the command
                eventt = event;

                Member member = event.getMember();

                if (member != null) {
                    GuildVoiceState voiceState = member.getVoiceState();
                    // Joins the voice call
                    if (voiceState != null && voiceState.inAudioChannel()) {
                        AudioChannelUnion voiceChannel = voiceState.getChannel();
                        event.getGuild().getAudioManager().openAudioConnection(voiceChannel);
                        playAudio("announcement_start");
                        // initalize values for start
                        this.textChannel = event.getGuild().getTextChannelsByName("mafia", true);
                        GamePlay.setGuild(event.getGuild());
                        GamePlay.setTextChannel(textChannel);
                        List<Member> members = voiceChannel.getMembers();
                        List<Player> players = new ArrayList<>();

                        // Add extra Mafia or Citizen
                        List<OptionMapping> options = event.getOptions();
                        if (options.isEmpty() == false) {
                            int extraMafia = options.get(0).getAsInt();
                            int extraCiziten = options.get(1).getAsInt();
                            game.initalizeGame(extraMafia, extraCiziten);
                            System.out.println("GAME SIZE " + game.getGameRoles().size());
                        } else {
                            System.out.println("No command was given");
                            return;
                        }
                        // Iterating in every user in call
                        int count = game.getGameRoles().size();
                        List<Player> mafiaPlayers = new ArrayList<Player>();
                        String text = "Hello, I am your bot!";
                        for (Member channelMember : members) {
                            if (count != 0) {
                                Role role = game.getGameRoles().get(count - 1);
                                System.out.println("HERE:" + channelMember.getEffectiveName());
                                Player tempPlayer = new Player(channelMember.getEffectiveName(), role);
                                // add mafias to mafiaPlayers so they know their teamates later
                                if (role.getRole().equals("godfather") ||
                                        role.getRole().equals("negotiator") ||
                                        role.getRole().equals("normalmafia")) {
                                    mafiaPlayers.add(tempPlayer);
                                }
                                tempPlayer.setPlayerThemselves(channelMember);
                                players.add(tempPlayer);
                                channelMember
                                        .getUser()
                                        .openPrivateChannel()
                                        .flatMap(privateChannel -> privateChannel.sendMessage(
                                                "----------------------------------------------------------------\nWelcome to Mafia! Your Role is: \n "
                                                        + role.getRole()))
                                        .queue();
                                count--;
                            } else {
                                break;
                            }
                        }

                        // Set the players connected with discord users
                        game.setPlayers(players);
                        GamePlay.setGame(game);
                        GamePlay.setIsRoleOut();
                        setPlayersAudio();
                        String mafiaPlayersNames = "";
                        for (Player p : mafiaPlayers) {
                            mafiaPlayersNames += p.getName() + "( " + p.getRole().getRole() + ")" + ",";
                        }
                        final String mafianames = mafiaPlayersNames;
                        // Tell Mafia their friends
                        for (Player p : mafiaPlayers) {
                            Member channelMember = p.getPlayerThemselves();
                            channelMember
                                    .getUser()
                                    .openPrivateChannel()
                                    .flatMap(privateChannel -> privateChannel
                                            .sendMessage("The Mafia team is: " + mafianames))
                                    .queue();
                        }
                        event.reply("Mafia started successfully!").queue();
                    }
                }
                break;
            case "reportnight":
                GamePlay.ReportNight();
                break;
            case "analyzenight":
                String result = "";
                List<String> deadPlayerNames = game.analyzeNight();

                if (deadPlayerNames.size() == 0) {
                    playAudio("announcement_noOneOut");
                    textChannel.get(0).sendMessage("Last night, no one died!\n There is currently "
                            + game.getPlayers().size() + " Players in the game.\n").queue();
                    return;
                }
                for (String deadPlayer : deadPlayerNames) {
                    playAudio("bye_" + deadPlayer);
                    result += deadPlayer + ",";
                }
                result += "there is currently " + game.getPlayers().size()
                        + " Players in the game.";
                if (textChannel.size() != 0) {
                    textChannel.get(0).sendMessage("Last night, the following players died: " + result).queue();
                } else {
                    System.out.println("Text Channel not found");
                    return;
                }
                break;
            case "removeplayer":
                List<OptionMapping> options = event.getOptions();
                if (options.isEmpty() == false) {
                    String removePlayer = options.get(0).getAsString();
                    for (int i = 0; i < game.getPlayers().size(); i++) {
                        Player temp = game.getPlayers().get(i);
                        if (temp.getName().equals(removePlayer)) {
                            game.removePlayer(temp);
                            textChannel
                                    .get(0)
                                    .sendMessage(
                                            "The following played has been successfully voted out: " + removePlayer)
                                    .queue();
                            return;
                        }
                    }
                } else {
                    System.out.println("No command was given");
                    return;
                }
                break;
            case "startday":
                List<OptionMapping> optionss = event.getOptions();
                if (optionss.isEmpty() == false) {
                    int time = optionss.get(0).getAsInt();
                    GamePlay.startDay(time);
                } else {
                    System.out.println("No command was given for starting day for time");
                    return;
                }
                break;
            case "startnight":
                GamePlay.startNight();
                break;
            case "startvote":
                GamePlay.startVote();
                break;
        }
    }

    public void playAudio(String trackname) {
        playerManager.play(eventt.getGuild(), currentDirectory + "/src/main/voicerecording/" + trackname + ".m4a");
    }

    public void setPlayersAudio() {
        for (Player p : game.getPlayers()) {
            p.setByeAudio("bye" + "_" + p.getName());
            p.setTurnAudio("turn" + "_" + p.getName());
        }
    }
}
