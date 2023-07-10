package com.example.model.game;

import com.example.model.player.Player;
import com.example.model.role.DetectiveRole;
import com.example.model.role.DoctorRole;
import com.example.model.role.GodFatherRole;
import com.example.model.role.NegotiatorRole;
import com.example.model.role.NormalCitizenRole;
import com.example.model.role.NormalMafiaRole;
import com.example.model.role.Role;
import com.example.model.role.SniperRole;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Game {

    private List<Player> players;
    private Boolean isGodfatherOut = false;

    private Boolean godfatherNightFinish = false;
    private Boolean doctorNightFinish = false;
    private Boolean detectiveNightFinish = false;
    private Boolean sniperNightFinish = false;

    public Boolean getGodfatherNightFinish() {
        return godfatherNightFinish;
    }

    public void setGodfatherNightFinish(Boolean godfatherNightFinish) {
        this.godfatherNightFinish = godfatherNightFinish;
    }

    public Boolean getDoctorNightFinish() {
        return doctorNightFinish;
    }

    public void setDoctorNightFinish(Boolean doctorNightFinish) {
        this.doctorNightFinish = doctorNightFinish;
    }

    public Boolean getDetectiveNightFinish() {
        return detectiveNightFinish;
    }

    public void setDetectiveNightFinish(Boolean detectiveNightFinish) {
        this.detectiveNightFinish = detectiveNightFinish;
    }

    public Boolean getSniperNightFinish() {
        return sniperNightFinish;
    }

    public void setSniperNightFinish(Boolean sniperNightFinish) {
        this.sniperNightFinish = sniperNightFinish;
    }

    public Boolean getIsGodfatherOut() {
        return isGodfatherOut;
    }

    public void setIsGodfatherOut(Boolean isGodfatherOut) {
        this.isGodfatherOut = isGodfatherOut;
    }

    private List<Role> gameRoles = new ArrayList<>(
            Arrays.asList(
                    new GodFatherRole(),
                    // new NegotiatorRole(),
                    // new DetectiveRole(),
                    new DoctorRole(),
                    new SniperRole()));

    public Game() {
    }

    public void initalizeGame(int extraMafia, int extraCitizen) {
        for (int i = 0; i < extraMafia; i++) {
            gameRoles.add(new NormalMafiaRole());
        }
        for (int i = 0; i < extraCitizen; i++) {
            gameRoles.add(new NormalCitizenRole());
        }

        Collections.shuffle(this.gameRoles);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Role> getGameRoles() {
        return gameRoles;
    }

    public void setGameRoles(List<Role> gameRoles) {
        this.gameRoles = gameRoles;
    }

    public List<String> analyzeNight() {
        List<Player> lostPlayers = new ArrayList<Player>();
        String doctorSavedName = "";
        List<String> deadPlayers = new ArrayList<>();

        for (Player p : getPlayers()) {
            if (p.getRole().getRole() == "doctor") {
                DoctorRole role = (DoctorRole) p.getRole();
                doctorSavedName = role.getSavedPlayer().getName();
                break;
            }
        }
        for (int i = 0; i < getPlayers().size(); i++) {
            Player p = getPlayers().get(i);
            if (p.getRole().getRole() == "godfather") {
                GodFatherRole godFatherRole = (GodFatherRole) p.getRole();
                Player killed = godFatherRole.getKilledPlayer();
                if (killed.getName().equals(doctorSavedName) == false && killed != null) {
                    deadPlayers.add(killed.getName());
                    removePlayer(killed);
                }
            } else if (p.getRole().getRole() == "sniper") {
                SniperRole godFatherRole = (SniperRole) p.getRole();
                Player shottedPlayer = godFatherRole.getShottedPlayer();
                if (shottedPlayer.getRole().equals("godfather") == false && shottedPlayer != null) {
                    deadPlayers.add(shottedPlayer.getName());
                    removePlayer(shottedPlayer);
                }
            } else if (p.getRole().getRole() == "negotiator") {
                NegotiatorRole negotiatorRole = (NegotiatorRole) p.getRole();
                if (negotiatorRole.getKilledPlayer() != null) {
                    Player shottedPlayer = negotiatorRole.getKilledPlayer();
                    if (shottedPlayer.getName().equals(doctorSavedName) == false) {
                        deadPlayers.add(shottedPlayer.getName());
                        removePlayer(shottedPlayer);
                    }
                }
            } else if (p.getRole().getRole() == "normalmafia") {
                NormalMafiaRole normalMafiaRole = (NormalMafiaRole) p.getRole();
                if (normalMafiaRole.getKilledPlayer() != null) {
                    Player shottedPlayer = normalMafiaRole.getKilledPlayer();
                    if (shottedPlayer.getName().equals(doctorSavedName) == false) {
                        deadPlayers.add(shottedPlayer.getName());
                        removePlayer(shottedPlayer);
                    }
                }
            }
        }
        return deadPlayers;
    }

    public void resetPlayerNightData() {
        for (Player p : getPlayers()) {
            if (p.getRole().getRole() == "godfather") {
                GodFatherRole godFatherRole = (GodFatherRole) p.getRole();
                godFatherRole.setKilledPlayer(null);
            } else if (p.getRole().getRole() == "detective") {
                DetectiveRole godFatherRole = (DetectiveRole) p.getRole();
                godFatherRole.setGussedPlayer(null);
            } else if (p.getRole().getRole() == "doctor") {
                DoctorRole godFatherRole = (DoctorRole) p.getRole();
                godFatherRole.setSavedPlayer(null);
            } else if (p.getRole().getRole() == "sniper") {
                SniperRole godFatherRole = (SniperRole) p.getRole();
                godFatherRole.setShottedPlayer(null);
            } else if (p.getRole().getRole() == "negotiator") {
                NegotiatorRole godFatherRole = (NegotiatorRole) p.getRole();
                godFatherRole.setKilledPlayer(null);
            } else if (p.getRole().getRole() == "normalmafia") {
                NormalMafiaRole godFatherRole = (NormalMafiaRole) p.getRole();
                godFatherRole.setKilledPlayer(null);
            }
        }
    }

    public void removePlayer(Player player) {
        for (int j = 0; j < getPlayers().size(); j++) {
            Player playertemp = getPlayers().get(j);
            if (playertemp.getName().equals(player.getName())) {
                if (playertemp.getRole().getRole().equals("godfather") == true) {
                    System.out.println("WE ARE HERE222");
                    isGodfatherOut = true;
                }
                getPlayers().remove(j);
            }
        }
    }
}
