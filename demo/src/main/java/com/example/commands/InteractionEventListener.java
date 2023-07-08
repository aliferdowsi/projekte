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

public class InteractionEventListener extends ListenerAdapter {

    public static Game game = new Game();
    public static List<TextChannel> textChannel;
    private SlashCommandInteractionEvent event;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        this.event = event;
        super.onSlashCommandInteraction(event);
        switch (event.getName()) {
            case "startmafia":
                //Get the person who called the command
                Member member = event.getMember();
                if (member != null) {
                    GuildVoiceState voiceState = member.getVoiceState();
                    //Joins the voice call
                    if (voiceState != null && voiceState.inAudioChannel()) {
                        AudioChannelUnion voiceChannel = voiceState.getChannel();
                        event.getGuild().getAudioManager().openAudioConnection(voiceChannel);
                        PlayerManager playerManager = PlayerManager.get();
                        String currentDirectory = System.getProperty("user.dir");
                        System.out.println("Current working directory: " + currentDirectory);
                        playerManager.play(
                            event.getGuild(),
                            currentDirectory + "/src/main/voicerecording/bekhabid.m4a"
                        );

                        //initalize values for start
                        this.textChannel = event.getGuild().getTextChannelsByName("mafia", true);
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
                                //add mafias to mafiaPlayers so they know their teamates later
                                if (
                                    role.getRole().equals("godfather") ||
                                    role.getRole().equals("negotiator") ||
                                    role.getRole().equals("normalmafia")
                                ) {
                                    mafiaPlayers.add(tempPlayer);
                                }
                                tempPlayer.setPlayerThemselves(channelMember);
                                players.add(tempPlayer);
                                channelMember
                                    .getUser()
                                    .openPrivateChannel()
                                    .flatMap(privateChannel ->
                                        privateChannel.sendMessage(
                                            "Welcome to Mafia! Your Role is: \n " + role.getRole()
                                        )
                                    )
                                    .queue();
                                count--;
                            } else {
                                break;
                            }
                        }

                        //Set the players connected with discord users
                        game.setPlayers(players);
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
                                .flatMap(privateChannel ->
                                    privateChannel.sendMessage("The Mafia team is: " + mafianames)
                                )
                                .queue();
                        }
                    }
                }
                break;
            case "reportnight":
                ReportNight();
            case "analyzenight":
                String result =
                    game.analyzeNight() + "there is currently " + game.getPlayers().size() + " Players in the game.";
                if (this.textChannel.size() != 0) {
                    textChannel.get(0).sendMessage("Last night, the following players died: " + result).queue();
                    System.out.println(textChannel.get(0).getName());
                } else {
                    System.out.println("Text Channel not found");
                    return;
                }
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
                                .sendMessage("The following played has been successfully voted out: " + removePlayer)
                                .queue();
                            return;
                        }
                    }
                } else {
                    System.out.println("No command was given");
                    return;
                }
            case "startday":
                List<OptionMapping> optionss = event.getOptions();
                if (optionss.isEmpty() == false) {
                    int time = optionss.get(0).getAsInt();
                    startDay(time);
                } else {
                    System.out.println("No command was given for starting day for time");
                    return;
                }
            case "startnight":
                startNight();
        }
    }

    public void startDay(int time) {
        if (this.textChannel.size() != 0) {
            textChannel.get(0).sendMessage("Day has started...\n").queue();
            System.out.println(textChannel.get(0).getName());
        } else {
            System.out.println("Text Channel not found");
            return;
        }
        for (Player player : this.game.getPlayers()) {
            Member member = player.getPlayerThemselves();
            int intervalInSeconds = 2;

            // Send a message to the player
            // Send a message to the voice channel
            if (member != null) {
                event.getGuild().getAudioManager().closeAudioConnection();
                textChannel.get(0).sendMessage("message").queue();
            } else {
                System.out.println("MEMBER NULL");
            }

            // Wait for the interval
            try {
                TimeUnit.SECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String endMessage = "First day ended. Time to sleep!";
        textChannel.get(0).sendMessage(endMessage).queue();
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

        if (this.game.getIsGodfatherOut() == true) {
            System.out.println("WE ARE HERE11");
            for (Player p : this.game.getPlayers()) {
                if (p.getRole().getRole().equals("negotiator") || p.getRole().getRole().equals("normalmafia")) {
                    System.out.println("WE ARE HERE");
                    wakeGodFatherSubstitute();
                }
            }
        }
    }

    public void wakeGodFather() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("godfather")) {
                Member member = p.getPlayerThemselves();
                member
                    .getUser()
                    .openPrivateChannel()
                    .queue(privateChannel -> {
                        privateChannel.sendMessage("Choose someone to Kill!\n").queue();
                        String message = "";
                        for (int i = 0; i < this.game.getPlayers().size(); i++) {
                            message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                        }
                        privateChannel.sendMessage(message + "\n").queue();
                    });
            }
        }
    }

    public void wakeGodFatherSubstitute() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("negotiator") || p.getRole().getRole().equals("normalmafia")) {
                Member member = p.getPlayerThemselves();
                member
                    .getUser()
                    .openPrivateChannel()
                    .queue(privateChannel -> {
                        privateChannel.sendMessage("Choose someone to Kill!\n").queue();
                        String message = "";
                        for (int i = 0; i < this.game.getPlayers().size(); i++) {
                            message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                        }
                        privateChannel.sendMessage(message + "\n").queue();
                    });
            }
        }
    }

    public void wakeDoctor() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("doctor")) {
                Member member = p.getPlayerThemselves();
                member
                    .getUser()
                    .openPrivateChannel()
                    .queue(privateChannel -> {
                        privateChannel.sendMessage("Choose someone to SAVE!\n").queue();
                        String message = "";
                        for (int i = 0; i < this.game.getPlayers().size(); i++) {
                            message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                        }
                        privateChannel.sendMessage(message + "\n").queue();
                    });
            }
        }
    }

    public void wakeSniper() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("sniper")) {
                Member member = p.getPlayerThemselves();
                member
                    .getUser()
                    .openPrivateChannel()
                    .queue(privateChannel -> {
                        privateChannel.sendMessage("Choose someone to SHOOT!\n").queue();
                        String message = "";
                        for (int i = 0; i < this.game.getPlayers().size(); i++) {
                            message += i + " - " + this.game.getPlayers().get(i).getName() + "\n";
                        }
                        privateChannel.sendMessage(message + "\n").queue();
                    });
            }
        }
    }

    public void wakeDetective() {
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole().equals("detective")) {
                Member member = p.getPlayerThemselves();
                member
                    .getUser()
                    .openPrivateChannel()
                    .queue(privateChannel -> {
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

    public void wakeNegotiator() {}

    public void ReportNight() {
        System.out.println("Printing the report:");
        for (Player p : this.game.getPlayers()) {
            if (p.getRole().getRole() == "godfather") {
                GodFatherRole godFatherRole = (GodFatherRole) p.getRole();
                System.out.println("godfather killed: " + godFatherRole.getKilledPlayer().getName());
            } else if (p.getRole().getRole() == "detective") {
                DetectiveRole detectiverRole = (DetectiveRole) p.getRole();
                System.out.println("detective gussed: " + detectiverRole.getGussedPlayer().getName());
            } else if (p.getRole().getRole() == "doctor") {
                DoctorRole doctorRole = (DoctorRole) p.getRole();
                System.out.println("doctor saved: " + doctorRole.getSavedPlayer().getName());
            } else if (p.getRole().getRole() == "sniper") {
                SniperRole sniperRole = (SniperRole) p.getRole();
                System.out.println("sniper shot: " + sniperRole.getShottedPlayer().getName());
            } else if (p.getRole().getRole() == "negotiator") {
                NegotiatorRole sniperRole = (NegotiatorRole) p.getRole();
                System.out.println("negotiator shot: " + sniperRole.getKilledPlayer().getName());
            } else if (p.getRole().getRole() == "nromalmafia") {
                NormalMafiaRole sniperRole = (NormalMafiaRole) p.getRole();
                System.out.println("nromalmafia shot: " + sniperRole.getKilledPlayer().getName());
            }
        }
    }
}
