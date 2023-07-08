package com.example.model.role;

import com.example.model.player.Player;

public class DetectiveRole extends Role {
    private Player gussedPlayer;
    public Player getGussedPlayer() {
        return gussedPlayer;
    }
    public void setGussedPlayer(Player gussedPlayer) {
        this.gussedPlayer = gussedPlayer;
    }
    public DetectiveRole(){
        super("detective");
    }
}
