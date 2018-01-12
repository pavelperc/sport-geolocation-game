package com.perc.pavel.sportgeolocationgame;

import android.location.Location;
import android.os.Handler;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.util.ArrayList;

/**
 * Created by pavel on 21.12.2017.
 * <p>
 * Двигает маркер в заданном направлении на постоянное расстояние раз в секунду
 * Направление меняется через rotLeft и rotRight
 * <p>
 * Примерно для Москвы:
 * 1 градус = 111km
 * 10 метров = 0.00009 градусов
 */

public class FakeGps implements Runnable {
    
    private static final double STEP = 0.00018;
    /**
     * задержка в миллисекундах
     */
    private static final int DELAY = 1500;
    
    double lat;
    double lng;
    private LocationCallback locationCallback;
    Handler handler = new Handler();
    
    /**
     * Направление движения в градусах
     * (это называется Азимут)
     */
    double bearing = 0;
    
    
    FakeGps(double lat, double lng, LocationCallback locationCallback) {
        this.lat = lat;
        this.lng = lng;
        this.locationCallback = locationCallback;
    
        // Сразу отправляем текущие координаты без задержки
        sendLocation();
    }
    
    
    void start() {
        handler.removeCallbacks(this);
        handler.postDelayed(this, DELAY);
    }
    void stop() {
        handler.removeCallbacks(this);
    }
    
    /**
     * Задаёт направление движения в градусах (азимут)
     */
    void setBearing(double bearing) {
        this.bearing = bearing;
    }
    
    
    /**
     * Мнгновенно отправляет текущие координаты в колбек
     */
    private void sendLocation() {
        Location location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lng);
        ArrayList<Location> list = new ArrayList<>();
        list.add(location);
        LocationResult locationResult = LocationResult.create(list);
        locationCallback.onLocationResult(locationResult);
    }
    
    
    
    @Override
    public void run() {
        // меняем координаты 
        lat += 0.718 * Math.cos(bearing * Math.PI / 180) * STEP;
        lng += Math.sin(bearing * Math.PI / 180) * STEP;
        
        sendLocation();
        
        handler.postDelayed(this, DELAY);
    }
}
