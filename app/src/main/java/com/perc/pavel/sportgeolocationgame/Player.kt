package com.perc.pavel.sportgeolocationgame

import android.graphics.Color

import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.Marker
import org.json.JSONException

import org.json.JSONObject

/**
 * Created by pavel on 05.11.2017.
 */

class Player(
        var login: String,
        var name: String,
        var lat: Double = UNSET_COORD,
        var lng: Double = UNSET_COORD,
        var teamColor: Int = NO_TEAM_COLOR
) {
    
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
    
    companion object {
        /** Цвет игрока, который не выбрал команду. */
        const val NO_TEAM_COLOR = Color.GRAY
        
        private const val UNSET_COORD = -1000.0
    }
    
    var marker: Marker? = null
    
    var coords: LatLng
        get() {
            if (!hasCoords())
                throw UnsupportedOperationException("No coords in " + toString())
            return LatLng(lat, lng)
        }
        set(latLng) {
            lat = latLng.latitude
            lng = latLng.longitude
        }
    
    
    fun hasMarker(): Boolean {
        return marker != null
    }
    
    fun hasCoords(): Boolean {
        return lat != UNSET_COORD || lng != UNSET_COORD
    }
    
    
    fun setCoords(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }
    
    
    @Throws(JSONException::class)
    constructor(player: JSONObject) : this(
            login = player.getString("login"),
            name = player.optString("name", player.getString("login")),
            lat = player.optDouble("lat", UNSET_COORD),
            lng = player.optDouble("lng", UNSET_COORD),
            teamColor = player.optInt("team_color", NO_TEAM_COLOR)
    )
    
    fun hasTeam(): Boolean {
        return teamColor != NO_TEAM_COLOR
    }
    
    override fun toString() =
            String.format("player %s: teamcolor = %d; coords = (%f,%f)", login, teamColor, lat, lng)
}
