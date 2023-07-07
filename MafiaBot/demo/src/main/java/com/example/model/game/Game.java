package com.example.model.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.example.model.player.Player;
import com.example.model.role.DetectiveRole;
import com.example.model.role.DoctorRole;
import com.example.model.role.GodFatherRole;
import com.example.model.role.NegotiatorRole;
import com.example.model.role.NormalCitizenRole;
import com.example.model.role.NormalMafiaRole;
import com.example.model.role.Role;
import com.example.model.role.SniperRole;

public class Game {
    private List<Player> players;

    private List<Role> gameRoles = new ArrayList<>(Arrays.asList(
        //new GodFatherRole(),
        //new NegotiatorRole(),
        new DetectiveRole(),
        new DoctorRole()
       // new SniperRole()
    ));

    public Game() {
    }

    public void initalizeGame(int extraMafia,int extraCitizen){
        for(int i = 0; i < extraMafia; i++){
            gameRoles.add(new NormalMafiaRole());
        }
        for(int i = 0; i < extraCitizen; i++){
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

   
}
