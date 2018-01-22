package com.perc.pavel.sportgeolocationgame;

import java.io.Serializable;

/**
 * Профиль текущего либо любого друого пользователя
 * Created by pavel on 21.01.2018.
 */

public class Profile implements Serializable {
    private String name;
    private String login;
    private String password;
    
    public Profile(String name, String login, String password) {
        this.name = name;
        this.login = login;
        this.password = password;
    }
    
//    public Profile(String name, String login) {
//        this.name = name;
//        this.login = login;
//    }
    
    public String getName() {
        return name;
    }
    
    public String getLogin() {
        return login;
    }
    
    public String getPassword() {
        if (password == null)
            throw new RuntimeException("no in this profile password.");
        return password;
    }
}
