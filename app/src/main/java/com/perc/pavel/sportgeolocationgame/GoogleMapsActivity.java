package com.perc.pavel.sportgeolocationgame;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GoogleMapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final int FLAGS_NUMBER = 30;
    
    /** Профиль текущего игрока.*/
    Profile myProfile;
    private int roomId;
    private boolean createGame;
    
    private Random rnd = new Random();
    private GoogleMap googleMap;
    SupportMapFragment mapFragment;
    
    private FakeGps fakeGps;
    
    private Map<String, Player> playersMap = new HashMap<>();
    private Map<String, Marker> playerMarkersMap = new HashMap<>();
    
    
    private ArrayList<Flag> flags = new ArrayList<>();
    private ArrayList<Marker> flagMarkers = new ArrayList<>();
    ArrayList<Integer> teamColors = new ArrayList<>();
    private Map<Marker, Flag> markerToFlagMap = new HashMap<>();
    Circle circle;
    
    private BottomSheetHandler bottomSheetHandler;
    
    private int myTeamColor;
    private Marker myLocationMarker;
    
    /** Последнее местоположение.*/
    Location myLastLocation = null;
    
    private Bitmap whiteFlagBitmap;
    
    /**
     * Управляет отложенными потоками для обновления данных с сервера
     */
    private final Handler updatePlayersHandler = new Handler();
    /**
     * Нужен для запроса на старт и остановку обновлений геолокации
     */
    private FusedLocationProviderClient fusedLocationClient;
    
    
    // Для чата
    
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private List<UserMessage> messageList;
    private EditText etChatBox;
    private RelativeLayout rlChat;
    private TextView tvMissedMsg;
    private int missedMsgCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
    
    
        // Загружаем в оперативную память значок флажка
        whiteFlagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.white_flag);
        
        myProfile = (Profile) getIntent().getSerializableExtra("profile");
        // получаем цвета команд и свой цвет
        teamColors = (ArrayList<Integer>) getIntent().getSerializableExtra("teamColors");
        myTeamColor = getIntent().getIntExtra("myTeamColor", teamColors.get(0));
        
//        myName = getIntent().getStringExtra("name");
        roomId = getIntent().getIntExtra("roomId", -1);
        createGame = getIntent().getBooleanExtra("createGame", false);
    
    
        // создание выдвигающегося экрана снизу
        bottomSheetHandler = new BottomSheetHandler(this);
    
    
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        
        
        // Треть карты должна уехать вниз за экран когда mapFragment.getView будет отрисован
        mapFragment.getView().post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
                // params.height изначально равна -1 (MATCH_PARENT)
                // а вот mapFragment.getView().getHeight() выдаёт настоящую высоту
                params.height = (int) (mapFragment.getView().getHeight() * 1.5);
                mapFragment.getView().setLayoutParams(params);
            }
        });
        
        mapFragment.getMapAsync(this);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        
        // Создание и настройка чата
        
        etChatBox = (EditText) findViewById(R.id.etChatBox);
        tvMissedMsg = (TextView) findViewById(R.id.tvMissedMsg);
        tvMissedMsg.setVisibility(View.INVISIBLE);// чтобы btnExpandChatClick не прыгала
        // добавляем реакцию на нажатие кнопки отправки сообщения на клавиатуре
        etChatBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    btnChatSendClick(null);
                    return true;
                }
                return false;
            }
        });
        
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        messageList = new ArrayList<>();
        mMessageAdapter = new MessageListAdapter(this, messageList, myProfile.getName());
        
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
        
        rlChat = (RelativeLayout) findViewById(R.id.rlChat);
        rlChat.setVisibility(View.GONE);
        
        TcpClient.getInstance().startAsync(new TcpConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TcpClient.SERVER_LOG, "in onConnected.");
                Toast.makeText(GoogleMapsActivity.this, "connected tcp", Toast.LENGTH_SHORT).show();
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("type", "connection");
                    jo.put("login", myProfile.getLogin());
                    TcpClient.getInstance().sendMessage(jo);
                } catch (JSONException ignored) {
                }
            }
            
            @Override
            public void onConnectionError(String error) {
                try { // на случай если активти уже закрыта.
//                    Toast.makeText(GoogleMapsActivity.this, error, Toast.LENGTH_SHORT).showCollapsed();
                    showConnectionErrorAlert(error, this);
                    Log.d(TcpClient.SERVER_LOG, "showed alertDialog");
                    
                } catch (Exception e) {
                    Log.d(TcpClient.SERVER_LOG, "exception in activity onConnectionError: " + e.getMessage());
                }
            }
        });
        
        TcpClient.getInstance().addMessageListener(new TcpMessageListener() {
            @Override
            public void onTCPMessageReceived(JSONObject data) {
                try {
                    if (data.getString("type").equals("message_chat")) {
                        addMessageToChat(data.getString("message"), data.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Вывести диалоговое окно
     *
     * @param error              Сообщение об ошибке
     * @param connectionListener Слушатель для переподключения.
     */
    private void showConnectionErrorAlert(String error, final TcpConnectionListener connectionListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapsActivity.this);
        String serverIsStopped = TcpClient.getInstance().isTcpRunning() ? "Сервер не остановлен." : "Сервер остановлен.";
        builder.setTitle("Ошибка работы сервера!\n" + serverIsStopped)
                .setMessage(error)
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // если мы не закончили tcp connection, заканчиваем
                        // потом ещё раз запускаемся заново с тем же листенером.
                        TcpClient.getInstance().reconnect(connectionListener);
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        // загрузка стиля карты
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.purple_map_style));
        
        googleMap.getUiSettings().setCompassEnabled(false);
        //googleMap.setBuildingsEnabled(false);
        
    
        // -----настройка управления геолокациией-----
        
        LinearLayout llRotate = (LinearLayout) findViewById(R.id.llRotate);
        SharedPreferences pref = getSharedPreferences("Settings", MODE_PRIVATE);
        llRotate.setVisibility(pref.getBoolean("fakeGps", false) ? View.VISIBLE : View.GONE);
        if (pref.getBoolean("fakeGps", false)) {
            // Настройка fakeGps и начального местоположения
            double lat, lng;
            try {
                String locationStr = pref.getString("startGps", "");
                lat = Double.valueOf(locationStr.split("; ")[0]);
                lng = Double.valueOf(locationStr.split("; ")[1]);
            } catch (Exception e) {
//                // New York
//                lat = 40.730610;
//                lng = -73.935242;
                
                // Moscow
                lat = 55.7513367;
                lng = 37.6255067;
            }
            
            fakeGps = new FakeGps(lat, lng, mLocationCallback);
            //fakeGps.start();
            
            // настройка переключателя ходьбы
            Switch swchGo = (Switch) findViewById(R.id.swchGo);
            Switch swchSuperSpeed = (Switch) findViewById(R.id.swchSuperSpeed);
            swchGo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        fakeGps.start();
                    else
                        fakeGps.stop();
                }
            });
            swchSuperSpeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    fakeGps.setSuperSpeed(isChecked);
                }
            });
            
        } else {
            // считываем реальную геолокацию
            startLocationUpdates();
        }
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
        if (fakeGps != null) {
            fakeGps.stop();
        }
        Log.d("my_tag", "location update stopped");
    }
    
    void generateFlags(int flagsNumber, int radius) {
        // удаляем все старые флажки
        for (Marker marker : markerToFlagMap.keySet()) {
            marker.remove();
        }
        markerToFlagMap.clear();
        flags.clear();
        
        
        // Создаём маркеры флажков
//        double lat = myLastLocation.getLatitude();
//        double lng = myLastLocation.getLongitude();
////        double delta = 0.01;// В градусах
//        double delta = 0.00018 * radius;// В градусах
        
        
        for (int i = 0; i < flagsNumber; i++) {
            
//            LatLng ll = new LatLng(lat + rnd.nextDouble() * delta - delta / 2, lng + rnd.nextDouble() * delta - delta / 2);
    
            LatLng llNew = FakeGps.getNextCoord(locToLL(myLastLocation), rnd.nextInt(360), rnd.nextInt(radius - 20) + 20);
            
            int color = teamColors.get(i % teamColors.size());
            Flag flag = new Flag(llNew.latitude, llNew.longitude, color, i);
            
            
            flags.add(flag);
            Marker fm = googleMap.addMarker(new MarkerOptions()
                    .position(llNew)
                    .anchor(8f / 48f, 1f)
                    .zIndex(2)
                    .icon(getColoredImage(R.drawable.white_flag, flag.getColorWithActivation())));
            fm.setTag("flag");
            markerToFlagMap.put(fm, flag);
        }
    
    
    }
    
    
    /**
     * Вызывается после первого получения геолокации игрока
     * Содержит старт и настройку новой игры, создание флажков и команд
     */
    private void onFirstLocationUpdate() {
        
        // Создаём маркер игрока
        myLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(locToLL(myLastLocation))
                .anchor(0.5f, 0.5f)
                .icon(getColoredImage(R.drawable.white_arrow_me, myTeamColor))
                .zIndex(1)
                .rotation(0)
                .flat(true));
        myLocationMarker.setTag("player-location");
        
//        generateFlags(30, 100);
        
        // обработка нажатия на маркер
        googleMap.setOnMarkerClickListener(this);
        
        
        // Мнгновенное перемещение камеры в центр локации с заданным масштабом
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locToLL(myLastLocation), 14));
        
        // Анимация камеры
        CameraPosition position = new CameraPosition.Builder()
                .tilt(60)// Наклон
                .target(locToLL(myLastLocation))
                .zoom(18)
                .bearing(myLastLocation.getBearing())// Направление
                .build();
        
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2500, null);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    
        
        
        
        // открытие выдвигающегося экрана снизу
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bottomSheetHandler.showCollapsed();
            }
        }, 500);
    
        // создание кружка, внутри которого будут флажки
        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude()))
                .radius(100)
                .strokeColor(getResources().getColor(android.R.color.holo_purple)));
        
        
        
        // запрос на начало игры
        
        JSONObject jo = new JSONObject();
        try {
            jo.put("type", "startGame");
            jo.put("lat", myLastLocation.getLatitude());
            jo.put("lng", myLastLocation.getLongitude());
            
            JSONObject setup = new JSONObject();
            setup.put("teamColors", new JSONArray(teamColors));
            
            JSONArray jFlags = new JSONArray();
            for (Flag flag : flags) {
                jFlags.put(flag.getJson());
            }
            setup.put("flags", jFlags);
            
            jo.put("setupGameInfo", setup);
            
        } catch (JSONException ignored) {
        }
        TcpClientFake.getInstance().httpRequest(jo, new HttpListener() {
            @Override
            public void onFailure(String error) {
                
            }
            
            @Override
            public void onResponse(String message) {
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
                .icon(getColoredImage(R.drawable.white_arrow, player.teamColor))
                .title(player.name)
                .zIndex(0)
                .flat(true));
        marker.setTag("player");
        playerMarkersMap.put(player.name, marker);
    }
    
    /**
     * Обновление маркеров других игроков
     *
     * @param marker
     * @param p
     */
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
    LatLng locToLL(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
    
    private Location llToLoc(LatLng latLng) {
        Location res = new Location("");
        res.setLatitude(latLng.latitude);
        res.setLongitude(latLng.longitude);
        return res;
    }
    
    void btnMyLocationClick(View v) {
        if (myLastLocation != null) {
            
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .tilt(googleMap.getCameraPosition().tilt)// Наклон
                    .target(locToLL(myLastLocation))
                    .zoom(18)
                    .bearing(myLastLocation.getBearing())// Направление
                    .build()));


//            // Вычисление местоположения камеры, чтобы маркер стал внизу карты
//            View view = mapFragment.getView();
//            
//            Projection projection = googleMap.getProjection();
//            LatLng markerPosition = locToLL(myLastLocation);
//            Point markerPoint = projection.toScreenLocation(markerPosition);
//            Point targetPoint = new Point(markerPoint.x, markerPoint.y - view.getHeight() / 4);
//            // Нужная позиция
//            LatLng targetPosition = projection.fromScreenLocation(targetPoint);
//            
//            
//            
//            // Анимация камеры
//            CameraPosition position = new CameraPosition.Builder()
//                    .tilt(googleMap.getCameraPosition().tilt)// Наклон
//                    .target(targetPosition)
//                    .zoom(18) // непонятно как будет работать со сдвигом
//                    .bearing(myLastLocation.getBearing())// Направление
//                    .build();
//
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500, null);
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
     * Получить по id ресурса картинку, окрашенную в цвет через Multiply.
     * При многократных вызовах лучше использовать {@link GoogleMapsActivity#getColoredImage}.
     *
     * @param id ID ресурса с картинкой
     * @param color цвет
     * @return BitmapDescriptor (нужен для создания значков маркеров)
     */
    private BitmapDescriptor getColoredImage(@DrawableRes int id, @ColorInt int color) {
        return getColoredImage(BitmapFactory.decodeResource(getResources(), id), color);
    }
    
    /**
     * Покрасить картинку в цвет через Multiply
     *
     * @param bitmap Картинка которую нужно покрасить
     * @param color цвет
     * @return BitmapDescriptor (нужен для создания значков маркеров)
     */
    private BitmapDescriptor getColoredImage(Bitmap bitmap, @ColorInt int color) {
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return BitmapDescriptorFactory.fromBitmap(bitmapResult);
    }
    
    /**
     * Обработка нажатий на маркеры
     *
     * @param marker маркер
     * @return возвращает обработано ли нажатие на маркер
     * (если обработано, то не надо открывать окно с информацией о маркере)
     */
    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {
        if (marker.getTag() == "player-location")
            return true;
        if (marker.getTag() == "player") {
            return false;
        }
        
        // если маркер - флажок
        
        
        double dist = myLastLocation.distanceTo(llToLoc(marker.getPosition()));
        final Flag flag = markerToFlagMap.get(marker);
        if (flag != null) {
            Toast.makeText(GoogleMapsActivity.this, "flag " + flag.number + "\ndistance: " + dist, Toast.LENGTH_SHORT).show();
            if (dist < 100 && flag.teamColor == myTeamColor && !flag.activated) {
//                addMessageToChat("I have captured the flag " + flag.number + "!!!", GoogleMapsActivity.this.myLogin);
//                addMessageToChat("Oh no!!", "somePlayer");
                flag.activate();
                // перекрашиваем флаг в свой цвет
                // flag.teamColor = myTeamColor;
                
                // Красим кешированную картинку
                marker.setIcon(getColoredImage(whiteFlagBitmap, flag.getColorWithActivation()));
                
                
                // Анимация флага - подпрыгивание
                Handler mHandler = new Handler();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler();
                        final long start = SystemClock.uptimeMillis();
                        final long duration = 1000;
                        
                        final Interpolator interpolator = new BounceInterpolator();
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                long elapsed = SystemClock.uptimeMillis() - start;
                                float t = Math.max(
                                        1 - interpolator.getInterpolation((float) elapsed
                                                / duration), 0);
                                marker.setAnchor(8f / 48f, 1f + t);
                                
                                if (t > 0.0) {
                                    // Post again 16ms later.
                                    handler.postDelayed(this, 32);
                                }
                            }
                        });
                    }
                });
                
                
                // Отправка сообщения об активации флага
                
                JSONObject jo = new JSONObject();
                try {
                    jo.put("type", "activateFlag");
                    jo.put("index", flag.number);
                    jo.put("color", myTeamColor);
                } catch (JSONException ignored) {
                }
                
                
                TcpClientFake.getInstance().httpRequest(jo, new HttpListener() {
                    @Override
                    public void onResponse(String msgStr) {
                        try {
                            JSONObject message = new JSONObject(msgStr);
                            // если не удалось обновить флажок на сервере
                            if (message.getInt("response") == 0) {
                                Toast.makeText(GoogleMapsActivity.this, "error: " + message.optString("error"), Toast.LENGTH_SHORT).show();
                                // всё отменяем
                                flag.deactivate();
                                marker.setIcon(getColoredImage(R.drawable.white_flag, flag.getColorWithActivation()));
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        
                    }
                });
            }
        } else {
            Toast.makeText(GoogleMapsActivity.this, "unknown flag", Toast.LENGTH_SHORT).show();
        }
        return true;
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
        stopLocationUpdates();
        stopPlayersUpdates();
        TcpClient.getInstance().stopClient();
        
        super.onDestroy();
    }
    
    
    // обработка нажатия кнопок поворота маркера с игроком
    
    public void btnTurnLeftClick(View v) {
        // в градусах
        float rotation = myLocationMarker.getRotation() - 30;
        myLocationMarker.setRotation(rotation);
        // чтобы при нажатии на кнопку текущего расположения сразу поворачиалось
        myLastLocation.setBearing(rotation);
        if (fakeGps != null) {
            fakeGps.setBearing(rotation);// куда будем двигаться
        }
    }
    
    public void btnTurnRightClick(View v) {
        // в градусах
        float rotation = myLocationMarker.getRotation() + 30;
        myLocationMarker.setRotation(rotation);
        // чтобы при нажатии на кнопку текущего расположения сразу поворачиалось
        myLastLocation.setBearing(rotation);
        if (fakeGps != null) {
            fakeGps.setBearing(rotation);// куда будем двигаться
        }
    }
    
    
    public void btnChatSendClick(View v) {
        if (etChatBox.getText().toString().equals(""))
            return;
        
        try {
            JSONObject jo = new JSONObject();
            jo.put("type", "message_chat");
            jo.put("name", myProfile.getName());
            jo.put("message", etChatBox.getText().toString());
            
            TcpClient.getInstance().sendMessage(jo);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // напишется в ответе сервера
//        addMessageToChat(etChatBox.getText().toString(), myLogin);
        etChatBox.setText("");
    }
    
    private void addMessageToChat(String message, String name) {
        messageList.add(new UserMessage(message, name));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        mMessageRecycler.scrollToPosition(messageList.size() - 1);
        
        // если сообщения скрыты
        if (rlChat.getVisibility() == View.GONE) {
            missedMsgCount++;
            // показать количество непрочитанных сообщений
            tvMissedMsg.setText("+" + missedMsgCount);
            if (tvMissedMsg.getVisibility() == View.INVISIBLE)
                tvMissedMsg.setVisibility(View.VISIBLE);
            
            // анимация тряски
            tvMissedMsg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        }
//        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
    }
    
    
    public void btnExpandChatClick(View v) {
        Button b = (Button) v;
        if (rlChat.getVisibility() == View.VISIBLE) {// скрыть 
            rlChat.setVisibility(View.GONE);
            b.setText("показ. чат");
            b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more_black_24dp, 0);
            
            // сброс пропущенных сообщений
            missedMsgCount = 0;
            // не показываем значок до первого пропущенного сообщения
//            tvMissedMsg.setText("");
//            tvMissedMsg.setVisibility(View.VISIBLE);
            
        } else {// показать
            rlChat.setVisibility(View.VISIBLE);
            b.setText("скрыть чат");
            b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_black_24dp, 0);
            
            tvMissedMsg.setVisibility(View.INVISIBLE);
        }
    }
    
    //------------------ПЕРЕМЕННЫЕ С РЕАЛИЗОВАННЫМИ ИНТЕРФЕЙСАМИ------------------
    
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

//                // собираем флаги
//                JSONArray jFlags = new JSONArray();
//                for (Flag flag : flags) {
//                    jFlags.put(flag.getJson());
//                }
//                jo.put("flags", jFlags);
                
            } catch (JSONException ignored) {
            }
            // отправляем запрос на получение данных игроков
            TcpClientFake.getInstance().httpRequest(jo, new HttpListener() {
                @Override
                public void onResponse(String msgStr) {
                    try {
                        JSONObject message = new JSONObject(msgStr);
                        // обновляем игроков
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
                            } else {
                                addPlayerMarker(p);
                            }
                        }
                        
                        // обновляем флажки (присланы только изменённые)
                        JSONArray jFlags = message.getJSONArray("flags");
                        for (int i = 0; i < jFlags.length(); i++) {
                            JSONObject jo = jFlags.getJSONObject(i);
                            
                            // создаём новый объект пользователя по json
                            Flag flag = new Flag(jo);
                            Log.d("my_tag", "flag received: " + flag);
                            
                            
                            // Если флажок есть на карте (и в массиве флажков)
                            if (flagMarkers.size() > flag.number) {
                                flags.set(flag.number, flag);
                                flagMarkers.get(flag.number).setIcon(getColoredImage(R.drawable.white_flag, flag.getColorWithActivation()));
                            }
                        }
                        
                        
                    } catch (JSONException ignored) {
                    }
                    
                    // ПОЧЕМУ БЕЗ ЭТОГО ВСЁ ВИСНЕТ?????????? (наверное потому что это было в цикле...)
                    updatePlayersHandler.removeCallbacks(updatePlayersRunnable);
                    
                    updatePlayersHandler.postDelayed(updatePlayersRunnable, 2000);
                }
                
                @Override
                public void onFailure(String error) {
                    
                }
            });
        }
    };
    
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
                myLocationMarker.setRotation(location.getBearing());
                
                // Анимация маркера
                ValueAnimator markerAnimator = ObjectAnimator.ofObject(myLocationMarker, "position",
                        new LatLngEvaluator(), myLocationMarker.getPosition(), locToLL(myLastLocation));
                markerAnimator.setDuration(500);
                markerAnimator.start();
            }
        }
    };
    
}
