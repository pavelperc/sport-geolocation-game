package com.perc.pavel.sportgeolocationgame;

import android.location.Location;
import android.os.Handler;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

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
    
    private double STEP = 0.00018;
    /**
     * задержка в миллисекундах
     */
    private int DELAY = 1500;
    
    
    double lat;
    double lng;
    private LocationCallback locationCallback;
    Handler handler = new Handler();
    
    /**
     * Направление движения в градусах
     * (это называется Азимут)
     */
    double bearing = 0;
    private boolean go = false;
    
    
    FakeGps(double lat, double lng, LocationCallback locationCallback) {
        this.lat = lat;
        this.lng = lng;
        this.locationCallback = locationCallback;
    
        // Сразу отправляем текущие координаты без задержки
        sendLocation();
        handler.postDelayed(this, DELAY);
    }
    
    void setSuperSpeed(boolean superSpeed) {
        if (superSpeed) {
            DELAY = 500;
        } else {
            DELAY = 1500;        }
    }
    
    void start() {
        go = true;
        
    }
    void stop() {
        go = false;
    }
    
    void destroy() {
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
        
        if (go) {
            // меняем координаты 
            lat += 0.718 * Math.cos(bearing * Math.PI / 180) * STEP;
            lng += Math.sin(bearing * Math.PI / 180) * STEP;
        }
        sendLocation();
        
        handler.postDelayed(this, DELAY);
    }
    
    /**
     * @param latLng 
     * @param bearing degrees
     * @param distance meters
     * @return
     */
    static LatLng getNextCoord(LatLng latLng, int bearing, int distance) {
        double R = 6378100; // Radius of the Earth
        double brng = Math.toRadians(bearing); // Bearing in radians.
        
        double lat1 = Math.toRadians(latLng.latitude); // Current lat point converted to radians
        double lon1 = Math.toRadians(latLng.longitude); // Current long point converted to radians
            
        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(distance/R) +
                Math.cos(lat1)*Math.sin(distance/R)*Math.cos(brng));
    
        double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(distance/R)*Math.cos(lat1),
                Math.cos(distance/R)-Math.sin(lat1)*Math.sin(lat2));
    
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        return new LatLng(lat2, lon2);
    }
}
