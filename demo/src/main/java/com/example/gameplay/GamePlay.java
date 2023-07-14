package com.example.gameplay;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.configuration.PlayerManager;
import com.example.model.game.Game;
import com.example.model.player.Player;
import com.example.model.role.DetectiveRole;
import com.example.model.role.DoctorRole;
import com.example.model.role.GodFatherRole;
import com.example.model.role.NegotiatorRole;
import com.example.model.role.NormalMafiaRole;
import com.example.model.role.SniperRole;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GamePlay {
    private static Game game = new Game();
    private static List<TextChannel> textChannel;
    // The discord channel
    private static Guild guild;
    static PlayerManager playerManager = PlayerManager.get();
    static String currentDirectory = System.getProperty("user.dir");

    public static void startDay(int time) {
        if (textChannel.size() != 0) {
            textChannel.get(0).sendMessage("Day has started...\n").queue();
            System.out.println(textChannel.get(0).getName());
        } else {
            System.out.println("Text Channel not found");
            return;
        }
        for (Player player : game.getPlayers()) {
            Member member = player.getPlayerThemselves();
            int intervalInSeconds = 2;

            // Send a message to the player
            // Send a message to the voice channel
            if (member != null) {
                playAudio(player.getTurnAudio());
                textChannel.get(0).sendMessage("Turn: " + player.getName() + "\n").queue();
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
    }

    public static void startVote() {
        if (textChannel.size() != 0) {
            playAudio("announcement_vote");
            textChannel.get(0).sendMessage("\nVoting has started...\n").queue();
            for (Player p : game.getPlayers()) {
                textChannel
                        .get(0)
                        .sendMessage(p.getName() + "\n")
                        .queue(message -> {
                            // Upvote the message with a thumbs-up reaction
                            Emoji thumbsUpEmoji = Emoji.fromUnicode("\uD83D\uDC4D");
                            message.addReaction(thumbsUpEmoji).queue();
                        });
            }
            System.out.println(textChannel.get(0).getName());
        } else {
            System.out.println("Text Channel not found");
            return;
        }
    }

    public static void startNight() {
        playAudio("announcement_night");
        wakeUpInOrder();
    }

    public static void wakeGodFather() {
        for (Player p : game.getPlayers()) {
            if (p.getRole().getRole().equals("godfather")) {
                playAudio("announcement_mafia");
                Member member = p.getPlayerThemselves();
                member
                        .getUser()
                        .openPrivateChannel()
                        .queue(privateChannel -> {
                            privateChannel.sendMessage("Choose someone to Kill!\n").queue();
                            String message = "";
                            for (int i = 0; i < game.getPlayers().size(); i++) {
                                message += i + " - " + game.getPlayers().get(i).getName() + "\n";
                            }
                            privateChannel.sendMessage(message + "\n").queue();
                        });
            }
        }
    }

    public static void wakeGodFatherSubstitute() {
        boolean found = false;
        for (Player p : game.getPlayers()) {
            if (p.getRole().getRole().equals("negotiator") || p.getRole().getRole().equals("normalmafia")) {
                found = true;
                Member member = p.getPlayerThemselves();
                member
                        .getUser()
                        .openPrivateChannel()
                        .queue(privateChannel -> {
                            privateChannel.sendMessage("Choose someone to Kill!\n").queue();
                            String message = "";
                            for (int i = 0; i < game.getPlayers().size(); i++) {
                                message += i + " - " + game.getPlayers().get(i).getName() + "\n";
                            }
                            privateChannel.sendMessage(message + "\n").queue();
                        });
            }
        }
    }

    public static void wakeDoctor() {
        for (Player p : game.getPlayers()) {
            if (p.getRole().getRole().equals("doctor")) {
                playAudio("announcement_doctor");
                Member member = p.getPlayerThemselves();
                member
                        .getUser()
                        .openPrivateChannel()
                        .queue(privateChannel -> {
                            privateChannel.sendMessage("Choose someone to SAVE!\n").queue();
                            String message = "";
                            for (int i = 0; i < game.getPlayers().size(); i++) {
                                message += i + " - " + game.getPlayers().get(i).getName() + "\n";
                            }
                            privateChannel.sendMessage(message + "\n").queue();
                        });
            }
        }
        game.setDoctorNightFinish(true);
    }

    public static void wakeSniper() {
        for (Player p : game.getPlayers()) {
            if (p.getRole().getRole().equals("sniper")) {
                playAudio("announcement_sniper");
                Member member = p.getPlayerThemselves();
                member
                        .getUser()
                        .openPrivateChannel()
                        .queue(privateChannel -> {
                            privateChannel.sendMessage("Choose someone to SHOOT!\n").queue();
                            String message = "";
                            for (int i = 0; i < game.getPlayers().size(); i++) {
                                message += i + " - " + game.getPlayers().get(i).getName() + "\n";
                            }
                            privateChannel.sendMessage(message + "\n").queue();
                        });
            }
        }
        game.setSniperNightFinish(true);
    }

    public static void wakeDetective() {
        for (Player p : game.getPlayers()) {
            if (p.getRole().getRole().equals("detective")) {
                playAudio("announcement_detective");
                Member member = p.getPlayerThemselves();
                member
                        .getUser()
                        .openPrivateChannel()
                        .queue(privateChannel -> {
                            privateChannel.sendMessage("Choose someone to guess their identity!\n").queue();
                            String message = "";
                            for (int i = 0; i < game.getPlayers().size(); i++) {
                                message += i + " - " + game.getPlayers().get(i).getName() + "\n";
                            }
                            privateChannel.sendMessage(message + "\n").queue();
                        });
            }
        }
        game.setDetectiveNightFinish(true);
    }

    public static void ReportNight() {
        System.out.println("Printing the report:");
        for (Player p : game.getPlayers()) {
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

    public static boolean setDetectiveNightData(int authorIndex, int targetIndex, MessageReceivedEvent event) {
        DetectiveRole role = (DetectiveRole) game.getPlayers().get(authorIndex).getRole();
        role.setGussedPlayer(game.getPlayers().get(targetIndex));
        game.getPlayers().get(authorIndex).setRole(role);

        String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
        if (textChannel.size() == 0) {
            System.out.println("Text Channel not found");
            return false;
        }
        if (temprole == "godfather") {
            event.getChannel().sendMessage("The Person you have guessed is Citizen\n").queue();

            System.out.println(textChannel.get(0).getName());
        } else if (temprole == "negotiator" || temprole == "normalmafia") {
            event.getChannel().sendMessage("The Person you have guessed is MAFIA!\n").queue();
            System.out.println(textChannel.get(0).getName());
        } else {
            event.getChannel().sendMessage("The Person you have guessed is Citizen\n").queue();
            System.out.println(textChannel.get(0).getName());
        }
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        playAudio("sleep_detective");
        return true;
    }

    public static boolean setDoctorNightData(int authorIndex, int targetIndex, MessageReceivedEvent event) {
        DoctorRole role = (DoctorRole) game.getPlayers().get(authorIndex).getRole();

        Boolean hasSavedHimself = role.getSavedHimself();

        String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
        if (textChannel.size() == 0) {
            System.out.println("Text Channel not found");
            return false;
        }
        if (targetIndex == authorIndex) {
            if (hasSavedHimself == false) {
                role.setSavedPlayer(game.getPlayers().get(targetIndex));
                role.setSavedHimself(true);
                game.getPlayers().get(authorIndex).setRole(role);
                event.getChannel().sendMessage("The Person has been saved!\n").queue();
                game.setDoctorNightFinish(true);
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playAudio("sleep_doctor");

                wakeSniper();
                return true;
            } else {
                event.getChannel().sendMessage("You cannot save yourself twice!!\n").queue();
                return false;
            }
        } else {
            role.setSavedPlayer(game.getPlayers().get(targetIndex));
            game.getPlayers().get(authorIndex).setRole(role);
            event.getChannel().sendMessage("SUCCESS!!!\n").queue();
            game.setDoctorNightFinish(true);
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            playAudio("sleep_doctor");
            wakeSniper();
            return true;
        }
    }

    public static boolean setGodfatherNightData(int authorIndex, int targetIndex, MessageReceivedEvent event) {
        GodFatherRole role = (GodFatherRole) game.getPlayers().get(authorIndex).getRole();

        String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
        if (textChannel.size() == 0) {
            System.out.println("Text Channel not found");
            return false;
        }
        if (temprole == "godfather") {
            event.getChannel().sendMessage("Cannot guess yourself\n").queue();
            return false;
        } else if (temprole == "negotiator" || temprole == "normalmafia") {
            event.getChannel().sendMessage("You cannot kill your own teamates dummy!\n").queue();
            return false;
        } else {
            event.getChannel().sendMessage("SUCCESS!!!\n").queue();
            role.setKilledPlayer(game.getPlayers().get(targetIndex));
            game.getPlayers().get(authorIndex).setRole(role);
            game.setGodfatherNightFinish(true);
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            playAudio("sleep_mafia");
            wakeUpInOrder();
            return true;
        }
    }

    public static boolean setGodfatherSubstituteNightData(int authorIndex, int targetIndex,
            MessageReceivedEvent event) {
        Player p = game.getPlayers().get(authorIndex);

        if (p.getRole().getRole().equals("normalmafia")) {
            NormalMafiaRole role = (NormalMafiaRole) game.getPlayers().get(authorIndex).getRole();
            String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
            if (textChannel.size() == 0) {
                System.out.println("Text Channel not found");
                return false;
            }
            if (temprole == "normalmafia") {
                event.getChannel().sendMessage("Cannot guess yourself\n").queue();
                return false;
            } else if (temprole == "negotiator") {
                event.getChannel().sendMessage("You cannot kill your own dummy!\n").queue();
                return false;
            } else {
                event.getChannel().sendMessage("SUCCESS!!!\n").queue();
                role.setKilledPlayer(game.getPlayers().get(targetIndex));
                game.getPlayers().get(authorIndex).setRole(role);
                game.setGodfatherNightFinish(true);
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playAudio("sleep_mafia");
                wakeUpInOrder();
                return true;
            }
        } else {
            NegotiatorRole role = (NegotiatorRole) game.getPlayers().get(authorIndex).getRole();
            String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
            if (textChannel.size() == 0) {
                System.out.println("Text Channel not found");
                return false;
            }
            if (temprole == "negotiator") {
                event.getChannel().sendMessage("Cannot guess yourself\n").queue();
                return false;
            } else if (temprole == "normalmafia") {
                event.getChannel().sendMessage("You cannot kill your own teamates dummy!\n").queue();
                return false;
            } else {
                event.getChannel().sendMessage("SUCCESS!!!\n").queue();
                role.setKilledPlayer(game.getPlayers().get(targetIndex));
                game.getPlayers().get(authorIndex).setRole(role);
                game.setGodfatherNightFinish(true);
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playAudio("sleep_mafia");

                wakeUpInOrder();
                return true;
            }
        }
    }

    public static boolean setSniperNightData(int authorIndex, int targetIndex, MessageReceivedEvent event) {
        SniperRole role = (SniperRole) game.getPlayers().get(authorIndex).getRole();
        role.setShottedPlayer(game.getPlayers().get(targetIndex));
        game.getPlayers().get(authorIndex).setRole(role);

        String temprole = game.getPlayers().get(targetIndex).getRole().getRole();
        if (textChannel.size() == 0) {
            System.out.println("Text Channel not found");
            return false;
        }
        event.getChannel().sendMessage("SUCCESS!!!\n").queue();
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        playAudio("sleep_sniper");
        wakeUpInOrder();
        return true;
    }

    public static void wakeUpInOrder() {
        if (game.getIsGodfatherOut() == true) {
            if (game.getGodfatherNightFinish() != null && game.getGodfatherNightFinish() == false) {
                System.out.println("sub gf");
                wakeGodFatherSubstitute();
                return;
            }
        } else {
            if (game.getGodfatherNightFinish() != null && game.getGodfatherNightFinish() == false) {
                System.out.println("gf");
                wakeGodFather();
                return;
            }
        }
        if (game.getDoctorNightFinish() != null && game.getDoctorNightFinish() == false) {
            System.out.println("doc");
            wakeDoctor();
            return;
        }
        if (game.getDetectiveNightFinish() != null && game.getDetectiveNightFinish() == false) {
            System.out.println("det");
            wakeDetective();
            return;
        }
        if (game.getSniperNightFinish() != null && game.getSniperNightFinish() == false) {
            System.out.println("sniper");
            wakeSniper();
            return;
        }

    }

    public static void setIsRoleOut() {
        for (Player p : game.getPlayers()) {

            if (p.getRole().getRole() == "godfather") {
                game.setGodfatherNightFinish(false);
                continue;
            }
            if (p.getRole().getRole() == "doctor") {
                game.setDoctorNightFinish(false);
                continue;
            }
            if (p.getRole().getRole() == "sniper") {
                game.setSniperNightFinish(false);
                continue;
            }
            if (p.getRole().getRole() == "detective") {
                game.setDetectiveNightFinish(false);
                continue;
            }
        }

    }

    public static void playAudio(String trackname) {

        playerManager.play(guild, currentDirectory + "/src/main/voicerecording/" + trackname + ".m4a");
    }

    public static Guild getGuild() {
        return guild;
    }

    public static void setGuild(Guild guild) {
        GamePlay.guild = guild;
    }

    public static Game getGame() {
        return game;
    }

    public static void setGame(Game game) {
        GamePlay.game = game;
    }

    public static List<TextChannel> getTextChannel() {
        return textChannel;
    }

    public static void setTextChannel(List<TextChannel> textChannel) {
        GamePlay.textChannel = textChannel;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public void setPlayerManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

}
