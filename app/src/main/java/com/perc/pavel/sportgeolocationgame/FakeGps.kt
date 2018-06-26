package com.perc.pavel.sportgeolocationgame

import android.location.Location
import android.os.Handler

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng

import java.util.ArrayList

/**
 * Created by pavel on 21.12.2017.
 *
 *
 * Двигает маркер в заданном направлении на постоянное расстояние раз в секунду
 * Направление меняется через rotLeft и rotRight
 *
 *
 * Примерно для Москвы:
 * 1 градус = 111km
 * 10 метров = 0.00009 градусов
 */

class FakeGps(var lat: Double, var lng: Double, private val locationCallback: LocationCallback) : Runnable {
    
    private val STEP = 0.00018
    
    /** Delay between steps in milliseconds*/
    private val DELAY
        get() = if (superSpeed) 500L else 1500L
    
    private val handler = Handler()
    
    /**
     * Направление движения в градусах
     * (это называется Азимут)
     */
    var bearing = 0.0
    
    var superSpeed: Boolean = false
    
    private var go = false
    
    init {
        // Сразу отправляем текущие координаты без задержки
        sendLocation()
        handler.postDelayed(this, DELAY)
    }
    
    fun start() {
        go = true
    }
    
    fun stop() {
        go = false
    }
    
    fun destroy() {
        handler.removeCallbacks(this)
    }
    
    
    /**
     * Мнгновенно отправляет текущие координаты в колбек
     */
    private fun sendLocation() {
        val location = Location("")
        location.latitude = lat
        location.longitude = lng
        val list = ArrayList<Location>()
        list.add(location)
        val locationResult = LocationResult.create(list)
        locationCallback.onLocationResult(locationResult)
    }
    
    /** Callback for [handler]*/
    override fun run() {
        
        if (go) {
            // меняем координаты 
            lat += 0.718 * Math.cos(bearing * Math.PI / 180) * STEP
            lng += Math.sin(bearing * Math.PI / 180) * STEP
        }
        sendLocation()
        
        handler.postDelayed(this, DELAY)
    }
    
    companion object {
        
        /**
         * @param latLng
         * @param bearing degrees
         * @param distance meters
         * @return
         */
        fun getNextCoord(latLng: LatLng, bearing: Int, distance: Int): LatLng {
            val R = 6378100.0 // Radius of the Earth
            val brng = Math.toRadians(bearing.toDouble()) // Bearing in radians.
            
            val lat1 = Math.toRadians(latLng.latitude) // Current lat point converted to radians
            val lon1 = Math.toRadians(latLng.longitude) // Current long point converted to radians
            
            var lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / R) + Math.cos(lat1) * Math.sin(distance / R) * Math.cos(brng))
            
            var lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(distance / R) * Math.cos(lat1),
                    Math.cos(distance / R) - Math.sin(lat1) * Math.sin(lat2))
            
            lat2 = Math.toDegrees(lat2)
            lon2 = Math.toDegrees(lon2)
            return LatLng(lat2, lon2)
        }
    }
}
