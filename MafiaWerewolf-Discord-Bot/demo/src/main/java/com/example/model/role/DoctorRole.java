package com.example.model.role;

import com.example.model.player.Player;

public class DoctorRole extends Role {

    private Boolean savedHimself = false;

    public Boolean getSavedHimself() {
        return savedHimself;
    }

    public void setSavedHimself(Boolean savedHimself) {
        this.savedHimself = savedHimself;
    }

    private Player savedPlayer;

    public Player getSavedPlayer() {
        return savedPlayer;
    }

    public void setSavedPlayer(Player savedPlayer) {
        this.savedPlayer = savedPlayer;
    }

    public DoctorRole() {
        super("doctor");
    }
}
