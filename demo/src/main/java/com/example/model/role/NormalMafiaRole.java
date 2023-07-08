package com.example.model.role;

import com.example.model.player.Player;

public class NormalMafiaRole extends Role {

    private Player killedPlayer;

    public Player getKilledPlayer() {
        return killedPlayer;
    }

    public void setKilledPlayer(Player killedPlayer) {
        this.killedPlayer = killedPlayer;
    }

    public NormalMafiaRole() {
        super("normalmafia");
    }
}
