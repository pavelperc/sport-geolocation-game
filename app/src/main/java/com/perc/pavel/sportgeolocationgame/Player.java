package com.perc.pavel.sportgeolocationgame;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pavel on 05.11.2017.
 */

class Player {
    /** Цвет игрока, который не выбрал команду.*/
    static final int NO_TEAM_COLOR = Color.GRAY;
    
    private static final int UNSET_COORD = -1000;
    
    String login;
    String name;
    double lat = UNSET_COORD;
    double lng = UNSET_COORD;
    int teamColor = NO_TEAM_COLOR;
    
    
    private Marker marker;
    
    public Marker getMarker() {
        if (marker == null)
            throw new NullPointerException("Tried to get null marker in " + toString());
        return marker;
    }
    
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    
    boolean hasMarker() {
        return marker != null;
    }
    
    boolean hasCoords() {
        return lat != UNSET_COORD;
    }
    
    void setCoords(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    
    
    public void setCoords(LatLng latLng) {
        lat = latLng.latitude;
        lng = latLng.longitude;
    }
    
    LatLng getCoords() {
        if (!hasCoords())
            throw new UnsupportedOperationException("No coords in " + toString());
        return new LatLng(lat, lng);
    }
    
    Player(String login) {
        this.login = login;
    }
    
    Player(String login, double lat, double lng, int teamColor) {
        this(login, login, lat, lng, teamColor);
    }
    Player(String login, String name, double lat, double lng, int teamColor) {
        this.login = login;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.teamColor = teamColor;
    }
    
    
    Player(JSONObject player) throws JSONException {
        login = player.getString("login");
        name = player.getString("name");
        
        lat = player.optDouble("lat", UNSET_COORD);
        lng = player.optDouble("lng", UNSET_COORD);
        teamColor = player.optInt("team_color", NO_TEAM_COLOR);
    }
    
//    public JSONObject getJson() {
//        if (!hasCoords())
//            throw new UnsupportedOperationException("can not get json without coords in " + toString());
//        JSONObject ans = new JSONObject();
//        try {
//            ans.put("login", login);
//            ans.put("name", name);
//            ans.put("lat", lat);
//            ans.put("lng", lng);
//            ans.put("teamColor", teamColor);
//        } catch (JSONException ignored) {}
//        return ans;
//    }
    
    boolean hasTeam() {
        return teamColor != NO_TEAM_COLOR;
    }
    
    @Override
    public String toString() {
        return String.format("player %s: teamcolor = %d; coords = (%f,%f)", login, teamColor, lat, lng);
    }
    
    public void setTeamColor(int teamColor) {
        this.teamColor = teamColor;
    }
}
