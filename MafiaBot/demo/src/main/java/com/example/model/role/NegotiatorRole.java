package com.example.model.role;

import com.example.model.player.Player;

public class NegotiatorRole extends Role{
    private Player negotiatedPlayer;

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
