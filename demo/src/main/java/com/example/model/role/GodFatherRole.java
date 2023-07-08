package com.example.model.role;

import com.example.model.player.Player;

public class GodFatherRole extends Role {
    private Player killedPlayer;

    public Player getKilledPlayer() {
        return killedPlayer;
    }

    public void setKilledPlayer(Player killedPlayer) {
        this.killedPlayer = killedPlayer;
    }

    public GodFatherRole() {
        super("godfather");
    }
}
