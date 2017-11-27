package com.perc.pavel.sportgeolocationgame;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pavel on 05.11.2017.
 */

public class Player {
    String name;
    double lat;
    double lng;
    int teamColor; 
    
    public Player(String name, double lat, double lng, int teamColor) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.teamColor = teamColor;
    }
    
    public Player(JSONObject player) throws JSONException {
        name = player.getString("name");
        lat = player.getDouble("lat");
        lng = player.getDouble("lng");
        teamColor = player.getInt("teamColor");
    }
    
    public JSONObject getJson() {
        JSONObject ans = new JSONObject();
        try {
            ans.put("name", name);
            ans.put("lat", lat);
            ans.put("lng", lng);
            ans.put("teamColor", teamColor);
        } catch (JSONException ignored) {}
        return ans;
    }
    
    @Override
    public String toString() {
        return String.format("%s: (%f,%f)", name, lat, lng);
    }
}
