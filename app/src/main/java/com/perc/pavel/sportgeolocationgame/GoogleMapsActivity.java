package com.perc.pavel.sportgeolocationgame;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int FLAGS_NUMBER = 30;
    private Random rnd = new Random();
    private GoogleMap googleMap;
    private Map<String, Player> playersMap = new HashMap<>();
    private Marker locationMarker;
    private Map<String, Marker> playerMarkersMap = new HashMap<>();
    
    
    private final Handler updatePlayersHandler = new Handler();
    
    /**
     * Циклическое обновление состояния игроков
     */
    private final Runnable updatePlayersRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("my_tag", "in updatePLayersRunnable");
            // собираем свои данные
            JSONObject jo = new JSONObject();
            try {
                jo.put("type", "getPlayerLocations");
                jo.put("lat", myLastLocation.getLatitude());
                jo.put("lng", myLastLocation.getLongitude());
            } catch (JSONException ignored) {}
            // отправляем запрос на получение данных игроков
            TcpClientFake.getInstance().httpRequest(jo, new HttpListener() {
                @Override
                public void onMessageReceived(JSONObject message) {
                    try {
                        JSONArray ja = message.getJSONArray("players");
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jo = ja.getJSONObject(i);
                            
                            // создаём новый объект пользователя по json
                            Player p = new Player(jo);
                            Log.d("my_tag", "player received: " + p);
                            
                            playersMap.put(p.name, p);
                            // Если этот пользователь есть на карте - анимируем его положение. Иначе - добавляем
                            if (playerMarkersMap.containsKey(jo.getString("name"))) {
                                updatePlayerMarker(playerMarkersMap.get(p.name), p);
                            }
                            else {
                                addPlayerMarker(p);
                            }
                            // ПОЧЕМУ БЕЗ ЭТОГО ВСЁ ВИСНЕТ??????????
                            updatePlayersHandler.removeCallbacks(updatePlayersRunnable);
                            
                            updatePlayersHandler.postDelayed(updatePlayersRunnable, 2000);
                        }
                    } catch (JSONException ignored){}
                }
            });
        }
    };

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
            
            Log.d("my_tag", "got location result: (" 
                    + location.getLatitude() + "," + location.getLongitude()
                    + ") bearing = " + location.getBearing()
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
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.green_map_style));
        
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
                .icon(vectorToBitmap(R.drawable.ic_white_arrow, Color.BLUE))
                .zIndex(1)
                .flat(true));
        locationMarker.setTag("player-location");
        
        // Создаём маркеры флажков
        double lat = myLastLocation.getLatitude();
        double lng = myLastLocation.getLongitude();
        double delta = 0.01;// В градусах
        
        //BitmapDescriptor flagIcon = BitmapDescriptorFactory.fromResource(R.drawable.purple_flag);
        
        ArrayList<MarkerOptions> flags = new ArrayList<>();
        for (int i = 0; i < FLAGS_NUMBER; i++) {
            LatLng ll = new LatLng(lat + rnd.nextDouble() * delta - delta / 2, lng + rnd.nextDouble() * delta - delta / 2);
            googleMap.addMarker(new MarkerOptions()
                    .position(ll)
                    .anchor(8f / 48f, 1f)
                    .zIndex(2)
                    .icon(vectorToBitmap(R.drawable.ic_white_flag, Color.MAGENTA))).setTag("flag_" + i);
        }
        
        
        
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (marker.getTag() == "player-location")
                    return true;
                if (marker.getTag() == "player") {
                    return false;
                }
                
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
        
        
        // запрос на начало игры
    
        JSONObject jo = new JSONObject();
        try {
            jo.put("type", "startGame");
            jo.put("lat", myLastLocation.getLatitude());
            jo.put("lng", myLastLocation.getLongitude());
        } catch (JSONException ignored) {}
        TcpClientFake.getInstance().httpRequest(jo, new HttpListener() {
            @Override
            public void onMessageReceived(JSONObject message) {
                startPlayersUpdates();
            }
        });
    }
    
    private void startPlayersUpdates() {
        updatePlayersRunnable.run();
    }
    
    private void stopPlayersUpdates() {
        updatePlayersHandler.removeCallbacks(updatePlayersRunnable);
        Log.d("my_tag", "players update stopped");
    }
    
    private void addPlayerMarker(Player player) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(locToLL(myLastLocation))
                .anchor(0.5f, 0.5f)
                .icon(vectorToBitmap(R.drawable.ic_white_arrow, player.teamColor))
                .title(player.name)
                .zIndex(0)
                .flat(true));
        marker.setTag("player");
        playerMarkersMap.put(player.name, marker);
    }
    
    private void updatePlayerMarker(Marker marker, Player p) {
        // Сами вычисляем направление игрока
        Location oldPos = llToLoc(marker.getPosition());
        Location newPos = llToLoc(new LatLng(p.lat, p.lng));
        float newBearing = oldPos.bearingTo(newPos);
        
        // Поворот маркера
        marker.setRotation(newBearing);
        
        // Анимация маркера
        ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position",
                new LatLngEvaluator(), marker.getPosition(), locToLL(newPos));
        markerAnimator.setDuration(500);
        markerAnimator.start();
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
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() * 2,
                vectorDrawable.getIntrinsicHeight() * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.MULTIPLY);
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        stopPlayersUpdates();
    }
}
