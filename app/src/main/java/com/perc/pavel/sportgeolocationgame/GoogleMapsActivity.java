package com.perc.pavel.sportgeolocationgame;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Random rnd = new Random();
    private GoogleMap googleMap;

    private Marker locationMarker;


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
                locationMarker.setRotation(location.getBearing());


//                // Анимация камеры
//                googleMap.animateCamera(CameraUpdateFactory.newLatLng(locToLL(myLastLocation)), 1000);

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
        setContentView(R.layout.activity_google_maps);
        
        
        
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.setBuildingsEnabled(false);
        startLocationUpdates();
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
        
        
        // Создаём маркер игрока
        locationMarker = googleMap.addMarker(new MarkerOptions()
                .position(locToLL(myLastLocation))
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_arrow))
                .flat(true));
        locationMarker.setTag("player-location");
        
        // Создаём маркеры флажков
        double lat = myLastLocation.getLatitude();
        double lng = myLastLocation.getLongitude();
        double delta = 0.01;// В градусах

        BitmapDescriptor flagIcon = BitmapDescriptorFactory.fromResource(R.drawable.purple_flag);
        
        ArrayList<MarkerOptions> flags = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LatLng ll = new LatLng(lat + rnd.nextDouble() * delta - delta / 2, lng + rnd.nextDouble() * delta - delta / 2);
            googleMap.addMarker(new MarkerOptions()
                    .position(ll)
                    .anchor(0.1f, 1f)
                    .icon(flagIcon)).setTag(i);
        }
        
        
        
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (marker.getTag() == "player-location")
                    return true;
                
                double dist = myLastLocation.distanceTo(llToLoc(marker.getPosition()));
                Toast.makeText(GoogleMapsActivity.this, marker.getTag() + "\ndistance: " + dist, Toast.LENGTH_SHORT).show();
                
                if (dist < 200) {
                    marker.remove();
                }
                return true;
            }
        });


        // Мнгновенное перемещение камеры в центр локации с заданным масштабом
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locToLL(myLastLocation), 14));
        
        // Анимация камеры
        CameraPosition position = new CameraPosition.Builder()
                .tilt(60)// Наклон
                .target(locToLL(myLastLocation))
                .zoom(18)
                .bearing(myLastLocation.getBearing())// Направление
                .build();
        
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2500, null);
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

    private Location llToLoc(LatLng latLng) {
        Location res = new Location("");
        res.setLatitude(latLng.latitude);
        res.setLongitude(latLng.longitude);
        return res;
    }
    
    void btnMyLocationClick(View v) {
//        // Анимация камеры
//        if (myLastLocation != null)
//            googleMap.animateCamera(CameraUpdateFactory.newLatLng(locToLL(myLastLocation)), 1000);

        if (myLastLocation != null) {
            // Анимация камеры
            CameraPosition position = new CameraPosition.Builder()
                    .tilt(googleMap.getCameraPosition().tilt)// Наклон
                    .target(locToLL(myLastLocation))
                    .zoom(18)
                    .bearing(myLastLocation.getBearing())// Направление
                    .build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500, null);
        }
    }

    void btnZoomInClick(View v) {
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    void btnZoomOutClick(View v) {
        googleMap.animateCamera(CameraUpdateFactory.zoomOut());
    }


    /**
     * Настраивает анимацию маркера.
     */
    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.
        
        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            double latitude = (startValue.latitude
                    + ((endValue.latitude - startValue.latitude) * fraction));
            double longitude = startValue.longitude
                    + ((endValue.longitude - startValue.longitude) * fraction);
            
            return new LatLng(latitude, longitude);
        }
    }
}
