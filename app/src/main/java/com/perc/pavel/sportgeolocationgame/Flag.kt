package com.perc.pavel.sportgeolocationgame

import android.graphics.Color

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

import org.json.JSONException
import org.json.JSONObject

import java.util.Locale

/**
 * Created by pavel on 19.12.2017.
 */

class Flag(
        var lat: Double,
        var lng: Double,
        var teamColor: Int,
        var number: Int,
        var activated: Boolean = false
) {
    
    var marker: Marker? = null
    
    val json: JSONObject
        get() {
            val ans = JSONObject()
            try {
                ans.put("lat", lat)
                ans.put("lng", lng)
                ans.put("teamColor", teamColor)
                ans.put("number", number)
                
                ans.put("activated", activated)
            } catch (ignored: JSONException) {
            }
            
            return ans
        }
    
    
    /**
     * Возвращает цвет флажка с учётом прозрачности
     * @return полупрозрачный если не активирован, непрозрачный если неактивирован
     */
    val colorWithActivation: Int
        get() =
            if (activated)
                teamColor
            else
                Color.argb(127, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor))
    
    
    val position: LatLng
        get() = LatLng(lat, lng)
    
    
    @Throws(JSONException::class)
    constructor(jo: JSONObject) : this(
            lat = jo.getDouble("lat"),
            lng = jo.getDouble("lng"),
            teamColor = jo.getInt("teamColor"),
            number = jo.getInt("number"),
            activated = jo.getBoolean("activated")
    )
    
    override fun toString() = String.format("flag %d: (%f,%f)", number, lat, lng)
}
