package com.perc.pavel.sportgeolocationgame;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by pavel on 19.12.2017.
 */

public class Flag {
    double lat;
    double lng;
    int teamColor;
    int number;
    
    boolean activated;
    
    private Marker marker;
    
    public void activate() {
        activated = true;
    }
    
    public void deactivate() {
        activated = false;
    }
    
    public Flag(double lat, double lng, int teamColor, int number) {
        this.lat = lat;
        this.lng = lng;
        this.teamColor = teamColor;
        this.number = number;
        activated = false;
    }
    
    public Flag(JSONObject jo) throws JSONException {
        lat = jo.getDouble("lat");
        lng = jo.getDouble("lng");
        teamColor = jo.getInt("teamColor");
        number = jo.getInt("number");
        activated = jo.getBoolean("activated");
    }
    
    public JSONObject getJson() {
        JSONObject ans = new JSONObject();
        try {
            ans.put("lat", lat);
            ans.put("lng", lng);
            ans.put("teamColor", teamColor);
            ans.put("number", number);
            
            ans.put("activated", activated);
        } catch (JSONException ignored) {}
        return ans;
    }
    
    /**
     * Возвращает цвет флажка с учётом прозрачности
     * @return полупрозрачный если не активирован, непрозрачный если неактивирован
     */
    public int getColorWithActivation() {
        if (activated)
            return teamColor;
        else
            return Color.argb(127, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor));
    }
    
    @Override
    public String toString() {
        return String.format("flag %d: (%f,%f)", number, lat, lng);
    }
    
    
    Marker getMarker() {
        if (marker == null)
            throw new NullPointerException("Tried to get null marker in " + toString());
        return marker;
    }
    
    void setMarker(Marker marker) {
        this.marker = marker;
    }
    
    boolean hasMarker() {
        return marker != null;
    }
    
    
    LatLng getPosition() {
        return new LatLng(lat, lng);
    }
}
