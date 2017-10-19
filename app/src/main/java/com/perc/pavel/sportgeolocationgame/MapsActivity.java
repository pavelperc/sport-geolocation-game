package com.perc.pavel.sportgeolocationgame;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private Random rnd = new Random();
    private MapView mapView;
    private MapboxMap mapboxMap;
    
    private MarkerView locationMarker;
    

    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Последнее местоположение.
     */
    private Location myLastLocation = null;
    
    /**
     * Регулярное обновление местоположения.
     */
    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();

            Log.d("my_tag", "got location result, bearing = " + location.getBearing()
                    + "accuracy = " + location.getAccuracy());
            

            if (myLastLocation == null) {
                myLastLocation = location;
                onFirstLocationUpdate();
            } else {
                // Сами вычисляем направление текущего игрока
                float newBearing = myLastLocation.bearingTo(location);
                myLastLocation = location;
                myLastLocation.setBearing(newBearing);
                
                // Поворот маркера
                locationMarker.setRotation(location.getBearing() - (float)mapboxMap.getCameraPosition().bearing);
                

//                // Анимация камеры
//                mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(locToLL(myLastLocation)), 1000);
                
                // Анимация маркера
                ValueAnimator markerAnimator = ObjectAnimator.ofObject(locationMarker, "position",
                        new LatLngEvaluator(), locationMarker.getPosition(), locToLL(myLastLocation));
                markerAnimator.setDuration(500);
                markerAnimator.start();
            }
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_key));
        setContentView(R.layout.activity_maps);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = (MapView) findViewById(R.id.mapView);
                
        
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        
        
        startLocationUpdates();
        mapboxMap.setOnCameraMoveListener(new MapboxMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition position = mapboxMap.getCameraPosition();
                // Поворот маркера при движении камеры
                if (locationMarker != null) {
                    float newRotation = myLastLocation.getBearing() - (float) position.bearing;
                    // Если направление не менялось - ничего не обновляем
                    if (locationMarker.getRotation() != newRotation)
                        locationMarker.setRotation(newRotation);
                }
            }
        });
    }


    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException ignored) {
        }
        ;

        Log.d("my_tag", "location update started");

    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback);
        Log.d("my_tag", "location update stopped");
    }

    private void onFirstLocationUpdate() {
//        Icon icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.drawable.blue_man);
//        locationMarker = mapboxMap.addMarker(new MarkerOptions()
//                .position(locToLL(myLastLocation))
//                .icon(icon));

        // Создаём маркер игрока
        Icon icon = IconFactory.getInstance(MapsActivity.this).fromResource(R.drawable.blue_arrow);
        locationMarker = mapboxMap.addMarker(new MarkerViewOptions()
                .position(locToLL(myLastLocation))
                .anchor(0.5f, 0.5f)
                .icon(icon)
                .flat(true));
        locationMarker.setRotation(myLastLocation.getBearing());
        
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(locToLL(myLastLocation)));
        
        Icon flagIcon = IconFactory.getInstance(MapsActivity.this).fromResource(R.drawable.red_flag);
        // Создаём маркеры флажков
        double lat = myLastLocation.getLatitude();
        double lng = myLastLocation.getLongitude();
        double delta = 0.01;// В градусах
        ArrayList<MarkerOptions> flags = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LatLng ll = new LatLng(lat + rnd.nextDouble() * delta - delta / 2, lng + rnd.nextDouble() * delta - delta / 2);
            flags.add(new MarkerOptions().position(ll).icon(flagIcon));
            
        }
        
        mapboxMap.addMarkers(flags);
        
        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                double dist = marker.getPosition().distanceTo(locToLL(myLastLocation));
                Toast.makeText(MapsActivity.this, marker.getId() + "\ndistance: " + dist, Toast.LENGTH_SHORT).show();
                
                if (dist < 200) {
                    marker.remove();
                }
                return true;
            }
        });
        
        // Анимация камеры
        CameraPosition position = new CameraPosition.Builder()
                .tilt(60)// Наклон
                .target(locToLL(myLastLocation))
                .zoom(16)
                .bearing(myLastLocation.getBearing())// Направление
                .build();
        
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2500);
    }

    /**
     * Преобразовать Location в LatLng
     *
     * @param location объект Location
     * @return объект LatLng
     */
    private LatLng locToLL(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        stopLocationUpdates();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    
    void btnMyLocationClick(View v) {
//        // Анимация камеры
//        if (myLastLocation != null)
//            mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(locToLL(myLastLocation)), 1000);

        if (myLastLocation != null) {
            // Анимация камеры
            CameraPosition position = new CameraPosition.Builder()
                    .tilt(60)// Наклон
                    .target(locToLL(myLastLocation))
                    .zoom(16)
                    .bearing(myLastLocation.getBearing())// Направление
                    .build();

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
        }
    }
    
    void btnZoomInClick(View v) {
        mapboxMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    void btnZoomOutClick(View v) {
        mapboxMap.animateCamera(CameraUpdateFactory.zoomOut());
    }
    
    
    /**
     * Настраивает анимацию маркера.
     */
    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }
}
