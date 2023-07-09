package com.example.model.player;

import com.example.model.role.Role;
import net.dv8tion.jda.api.entities.Member;

public class Player {

    private String name;
    private Role role;
    private Member playerThemselves; // Discord object for JDA
    private String byeAudio;
    private String turnAudio;

    public String getByeAudio() {
        return byeAudio;
    }

    public void setByeAudio(String byeAudio) {
        this.byeAudio = byeAudio;
    }

    public String getTurnAudio() {
        return turnAudio;
    }

    public void setTurnAudio(String turnAudio) {
        this.turnAudio = turnAudio;
    }

    public Member getPlayerThemselves() {
        return playerThemselves;
    }

    public void setPlayerThemselves(Member playerThemselves) {
        this.playerThemselves = playerThemselves;
    }

    public Player(String name, Role role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
