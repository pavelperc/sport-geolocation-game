package com.perc.pavel.sportgeolocationgame;

/**
 * Created by pavel on 16.01.2018.
 */

class UserMessage {
    String getMessage() {
        return userMessage;
    }
    
    String getSenderLogin() {
        return login;
    }
    
    private String userMessage;
    private String login;
    
    public UserMessage(String userMessage, String login) {
        this.userMessage = userMessage;
        this.login = login;
    }
}
