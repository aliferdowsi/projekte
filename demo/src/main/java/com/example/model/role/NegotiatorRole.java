package com.example.model.role;

import com.example.model.player.Player;

public class NegotiatorRole extends Role{
    private Player negotiatedPlayer;
    private Player killedPlayer;

    public Player getKilledPlayer() {
        return killedPlayer;
    }

    public void setKilledPlayer(Player killedPlayer) {
        this.killedPlayer = killedPlayer;
    }

    public Player getNegotiatedPlayer() {
        return negotiatedPlayer;
    }

    public void setNegotiatedPlayer(Player negotiatedPlayer) {
        this.negotiatedPlayer = negotiatedPlayer;
    }

    public NegotiatorRole() {
        super("negotiator");
    }

    public boolean negotiate(Player player){
        return true;
    }

    
}
