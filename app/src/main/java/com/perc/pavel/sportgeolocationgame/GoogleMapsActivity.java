package com.perc.pavel.sportgeolocationgame;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TimerTask;
import java.util.TreeMap;

import okhttp3.HttpUrl;

public class GoogleMapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, TcpMessageListener {
    
    private int roomId;
    boolean createGame;
    
    private Random rnd = new Random();
    GoogleMap googleMap;
    SupportMapFragment mapFragment;
    
    private FakeGps fakeGps;
    
    Map<String, Player> playersMap = new HashMap<>();
    SortedList<Player> players;
    
    /** Служит для хранения команды, логина и имени, Location никогда не заполняется
     * TeamColor по умолчанию серый (Player.NO_TEAM_COLOR)*/
    Player myPlayer;
    
    
    /** Объекты флажков по их номеру*/
    Map<Integer, Flag> flags = new HashMap<>();
    private Map<Marker, Flag> markerToFlagMap = new HashMap<>();
    
    ArrayList<Integer> teamColors = new ArrayList<>();
    Circle circle;
    
    BottomSheetHandler bottomSheetHandler;
    EnergyBlockHandler energyBlockHandler;
        
    //    private int myTeamColor = Player.NO_TEAM_COLOR;
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
    TextView tvRoomId;
    TextView tvTimer;
    private int missedMsgCount = 0;
    
    
    MyTeammatesAdapter myTeammatesAdapter;
    List<Player> myTeammates;
    
//    ProgressBar pbLoading;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
//        pbLoading = (ProgressBar) findViewById(R.id.pbLoading); 
    
        // Загружаем в оперативную память значок флажка
        whiteFlagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.white_flag);
        
        
        // полуаем параметры из intent-а
        
        // Профиль текущего игрока.
        Profile myProfile = (Profile) getIntent().getSerializableExtra("profile");
        teamColors = (ArrayList<Integer>) getIntent().getSerializableExtra("teamColors");
//        myName = getIntent().getStringExtra("name");
        roomId = getIntent().getIntExtra("roomId", -1);
        createGame = getIntent().getBooleanExtra("createGame", false);
        
//        if (createGame) {
//            myPlayer.teamColor = teamColors.get(0);
//        }

        Typeface PhosphateInline = Typeface.createFromAsset(getAssets(), "fonts/PhosphateInline.ttf");

        tvRoomId = (TextView) findViewById(R.id.tvRoomId);
        tvTimer = (TextView) findViewById(R.id.tvTimer);
        tvTimer.setVisibility(View.GONE);
        
        tvRoomId.setTypeface(PhosphateInline);
        tvRoomId.setText("ROOM ID: " + roomId);
        tvRoomId.setVisibility(View.VISIBLE);
        
        
        // Создание справа списка с моими сокомандниками
        
        RecyclerView rvTeammates = (RecyclerView) findViewById(R.id.rvTeammates);
        rvTeammates.setLayoutManager(new LinearLayoutManager(this));
        
        myTeammates = new ArrayList<>();
        myTeammatesAdapter = new MyTeammatesAdapter(this, myTeammates);
        rvTeammates.setAdapter(myTeammatesAdapter);
        
        
        
        // добавляем себя в список игроков
        myPlayer = new Player(myProfile.getLogin(), myProfile.getName(), Player.NO_TEAM_COLOR);
        playersMap.put(myPlayer.login, myPlayer);
        
        // создание выдвигающегося экрана снизу и списка игроков, привязанного к teamSharingAdapter
        bottomSheetHandler = new BottomSheetHandler(this);
        players = bottomSheetHandler.teamSharingAdapter.getPlayers();
        addPlayer(myPlayer);
    
    
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
                params.height = (int) (mapFragment.getView().getHeight() * 1.4);
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
        mMessageAdapter = new MessageListAdapter(this, messageList, myPlayer.name);
        
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
        
        rlChat = (RelativeLayout) findViewById(R.id.rlChat);
        rlChat.setVisibility(View.GONE);
        
        
        
        
        // добавлениие всех пользователей, подключившихся ранее
        
        if (!createGame) {
            HttpUrl url = TcpClient.getUrlBuilder()
                    .addPathSegment("get_players_in_room")
                    .addQueryParameter("room_id", String.valueOf(roomId))
                    .build();
            TcpClient.getInstance().httpGetRequest(url, new HttpListener() {
                @Override
                public void onResponse(JSONObject message) {
                    Log.d("my_tag", "Returned get_players_in_room: " + message.toString());
                    try {
                        if (!message.getBoolean("status")) {
                            Toast.makeText(GoogleMapsActivity.this, "server error in get_players_in_room:\n" 
                                    + message.getString("error"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        JSONArray ja = message.getJSONArray("players");
                        
                        for (int i = 0; i < ja.length(); i++) {
                            Player p = new Player(ja.getJSONObject(i));
                            // не допускаем замены текущего игрока, так как потом невозможно будет найти объект myPLayer
                            if (p.login.equals(myPlayer.login))
                                continue;
                            
                            addPlayer(p);
                        }
                        
                        
                    } catch (JSONException e) {
                        Toast.makeText(GoogleMapsActivity.this, "JSONException in get_players_in_room:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // настраиваем TCP после того, как добавили всех пользователей в комнате
                    setupTCP();
                }
    
                @Override
                public void onFailure(String error) {
                    setupTCP();
                }
            });
    
        } else {
            setupTCP();
        }
        
        
    }
    
    private void setupTCP() {
        TcpClient.getInstance().startAsync(new TcpConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TcpClient.SERVER_LOG, "in onConnected.");
                Toast.makeText(GoogleMapsActivity.this, "connected tcp", Toast.LENGTH_SHORT).show();
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("type", "connection");
                    jo.put("login", myPlayer.login);
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
        
        TcpClient.getInstance().addMessageListener(this);
        
    }
    
    
    /**
     * Вывести диалоговое окно
     *
     * @param error              Сообщение об ошибке
     * @param connectionListener Слушатель для переподключения. может быть null
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
                        if (connectionListener != null)
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
                        this, R.raw.dark_blue_map_style));
            
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
        
        Log.d("my_tag", "location update started");
        
    }
    
    /** Остановка насовсем*/
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback);
        if (fakeGps != null) {
            fakeGps.destroy();
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
        
        for (int i = 0; i < flagsNumber; i++) {
            
//            LatLng ll = new LatLng(lat + rnd.nextDouble() * delta - delta / 2, lng + rnd.nextDouble() * delta - delta / 2);
            
            final int INNER_CIRCLE = 40;// не генерировать флажки ближе чем на 40 м
            LatLng llNew = FakeGps.getNextCoord(locToLL(myLastLocation), rnd.nextInt(360), rnd.nextInt(radius - INNER_CIRCLE) + INNER_CIRCLE);
            
            int color = teamColors.get(i % teamColors.size());
            Flag flag = new Flag(llNew.latitude, llNew.longitude, color, i);
            
            flags.put(flag.number, flag);
            createFlagMarker(flag);
        }
    
    
    }
    
    /** Создаёт объект маркера и добавляет его к флажку и в markerToFlagMap.*/
    private void createFlagMarker(Flag flag) {
        Marker fm = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(flag.lat, flag.lng))
                .anchor(8f / 48f, 1f)
                .zIndex(2)
                .icon(getColoredImage(R.drawable.white_flag, flag.getColorWithActivation())));
        fm.setTag("flag");
        flag.setMarker(fm);
        markerToFlagMap.put(fm, flag);
    }
    
    
    /**
     * Вызывается после первого получения геолокации игрока
     * Содержит старт и настройку новой игры, создание флажков и команд
     */
    private void onFirstLocationUpdate() {
        Log.d("my_tag", "in onFirstLocationUpdate");
        // Создаём маркер игрока
        myLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(locToLL(myLastLocation))
                .anchor(0.5f, 0.5f)
                .icon(getColoredImage(R.drawable.white_arrow_me, myPlayer.teamColor))
                .zIndex(1)
                .rotation(0)
                .flat(true));
        myLocationMarker.setTag("player-location");
        
//        generateFlags(30, 100);
        
        // обработка нажатия на маркер
        googleMap.setOnMarkerClickListener(this);
        
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetHandler.hideFlagBar();
            }
        });
        
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
        
        
        if (createGame) {
            // создание кружка, внутри которого будут флажки
            circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude()))
                    .radius((bottomSheetHandler.sbCircleSize.getProgress() + 1) * 100)
                    .strokeColor(getResources().getColor(R.color.colorPrimary)));
        }
        
        //startPlayersUpdates();
    }
    
    private void startPlayersUpdates() {
        Log.d("my_tag", "in startPlayersUpdates");
        TcpClientFake.getInstance().startAsync(this, new TcpConnectionListener() {
            @Override
            public void onConnected() {
        
            }
    
            @Override
            public void onConnectionError(String error) {
        
            }
        }, teamColors, locToLL(myLastLocation));
//        updatePlayersRunnable.run();
    }
    
    private void stopPlayersUpdates() {
//        updatePlayersHandler.removeCallbacks(updatePlayersRunnable);
//        Log.d("my_tag", "players update stopped");
        TcpClientFake.getInstance().stopClient();
    }
    
    private void createPlayerMarker(Player player) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(player.getCoords())
                .anchor(0.5f, 0.5f)
                .icon(getColoredImage(R.drawable.white_arrow, player.teamColor))
                .title(player.login)
                .zIndex(0)
                .flat(true));
        marker.setTag("player");
        player.setMarker(marker);
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
        Location newPos = llToLoc(p.getCoords());
    
        // проверка на то, что игрок сдвинулся
        if (oldPos.getLatitude() == newPos.getLatitude() && oldPos.getLongitude() == newPos.getLongitude())
            return;
        
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
    
    Location llToLoc(LatLng latLng) {
        Location res = new Location("");
        res.setLatitude(latLng.latitude);
        res.setLongitude(latLng.longitude);
        return res;
    }
    
    public void btnMyLocationClick(View v) {
        if (myLastLocation != null) {
            
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .tilt(googleMap.getCameraPosition().tilt)// Наклон
                    .target(locToLL(myLastLocation))
                    .zoom(18)
                    .bearing(myLastLocation.getBearing())// Направление
                    .build()));
            
        }
    }
    
    public void btnZoomInClick(View v) {
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
    }
    
    public void btnZoomOutClick(View v) {
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
            bottomSheetHandler.hideFlagBar();
            return false;
        }
        
        // если маркер - флажок
        
        final Flag flag = markerToFlagMap.get(marker);
        if (flag != null) {
    
            // Анимация флага - подпрыгивание
            Handler mHandler = new Handler();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler();
                    final long start = SystemClock.uptimeMillis();
                    final long duration = 200;
            
                    final Interpolator interpolator = new AnticipateInterpolator();
            
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            long elapsed = SystemClock.uptimeMillis() - start;
                            float t = Math.max(
                                    1 - interpolator.getInterpolation((float) elapsed
                                            / duration), 0);
                            flag.getMarker().setAnchor(8f / 48f, 1f + t * 0.2f);
                    
                            if (t > 0.0) {
                                // Post again 16ms later.
                                handler.postDelayed(this, 32);
                            }
                        }
                    });
                }
            });
            
            bottomSheetHandler.openFlagBar(flag);
        } else {
            Toast.makeText(GoogleMapsActivity.this, "unknown flag", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    
    @Override
    public void onTCPMessageReceived(JSONObject jo) {
//        pbLoading.setVisibility(View.GONE);
        try {
            Log.d("my_tag", "tcp received: " + jo.toString());
            if (jo.toString().equals("{\"status\":\"Connection successfull\"}"))
                return;
            
            switch (jo.getString("type")) {
                case "message_chat":
                    addMessageToChat(jo.getString("message"), jo.getString("name"));
                    break;
                case "new_player_in_room":
                    Player p = new Player(jo);
                    
                    // не допускаем замены текущего игрока, так как потом невозможно будет найти объект myPLayer
                    if (p.login.equals(myPlayer.login))
                        return;
                    
                    addPlayer(p);
                    
                    // если есть куда ставить маркер
                    if (p.hasCoords()) {
                        // обновление маркера
                        if (p.hasMarker()) {
                            updatePlayerMarker(p.getMarker(), p);
                        } else {
                            createPlayerMarker(p);
                        }
                    }
                    
                    break;
                case "start_game":
                    // если мы не создавали игру и не генерировали флажки - создаём их
                    if (!createGame) {
                        // удаляем все старые флажки
                        for (Marker marker : markerToFlagMap.keySet()) {
                            marker.remove();
                        }
                        markerToFlagMap.clear();
                        flags.clear();
    
                        JSONArray ja = jo.getJSONArray("flags");
                        for (int i = 0; i < ja.length(); i++) {
                            Flag flag = new Flag(ja.getJSONObject(i));
                            flags.put(flag.number, flag);
                            createFlagMarker(flag);
                        }
                    }
                    if (circle != null)
                        circle.remove();
    
                    // закрываем bottom_sheet
                    bottomSheetHandler.hide();
                    bottomSheetHandler.prepareForFlags();
                    
                    energyBlockHandler = new EnergyBlockHandler(this);
                    
                    Toast.makeText(this, "GAME STARTED!!!", Toast.LENGTH_SHORT).show();
    
    
                    tvRoomId.setVisibility(View.GONE);
                    tvTimer.setVisibility(View.VISIBLE);
                    
                    
                    // начало таймера обратного отсчёта до конца игры
                    
                    new CountDownTimer(1000 * 60 * 30, 1) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            tvTimer.setText(new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));
                        }
        
                        @Override
                        public void onFinish() {
                            finishGame();
                        }
                    }.start();
                    
                    break;
                case "cords":
                    String login = jo.getString("login");
                    // если отослали не мы
                    if (!login.equals(myPlayer.login)) {
                        Player pl;
                        // если такой игрок присутствует
                        if (playersMap.containsKey(login)) {
                            pl = playersMap.get(login);
                            pl.setCoords(jo.getDouble("lat"), jo.getDouble("lng"));
                        }
                        else {// прислал координаты неизвестный игрок
                            // UNREACHABLE
                            Log.d("my_tag", "IN UNREACHABLE");
                            pl = new Player(jo);
                            addPlayer(pl);
                        }
                        
                        // обновление маркера
                        if (pl.hasMarker()) {
                            updatePlayerMarker(pl.getMarker(), pl);
                        } else {
                            createPlayerMarker(pl);
                        }
                    }
                    break;
                case "choose_team":
                    
                    if (jo.getString("login").equals(myPlayer.login)) {
                        changeMyTeamColor(jo.getInt("team_color"));
                        return;
                    }
                    
                    Player pll = playersMap.get(jo.getString("login"));
                    updatePlayer(pll, jo.getInt("team_color"));
                    
                    if (pll.hasCoords())
                        pll.getMarker().setIcon(getColoredImage(R.drawable.white_arrow, pll.teamColor));
                    
                    break;
                case "activate_flag":
                    if (jo.has("sync_energy") && jo.has("sync_energy_speed")) {
                        energyBlockHandler.setEnergy(jo.getInt("sync_energy"));
                        energyBlockHandler.setSpeed(jo.getInt("sync_energy_speed"));
                    }
                    Flag flag = flags.get(jo.getInt("number"));
                    
                    pickFlag(flag, jo.optInt("cost", 0), jo.optInt("color_to_change", flag.teamColor));
                    
                    // проверка что все флажки одного цвета и активированы и окончание игры
                    boolean endGame = true;
                    for (Flag flag1 : flags.values()) {
                        if (!flag1.activated || flag1.teamColor != flag.teamColor) {
                            endGame = false;
                            break;
                        }
                    }
                    if (endGame)
                        finishGame();
                    break;
                default:
                    Toast.makeText(GoogleMapsActivity.this, "Unknown TCP message:\n" + jo.toString(), Toast.LENGTH_LONG).show();
            }
            
        
        } catch (JSONException e) {
            Toast.makeText(this, "JSONException:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void finishGame() {
        // цвет->количество флажков
        Map<Integer, Integer> map = new HashMap<>();
        
        for (Integer teamColor : teamColors) {
            map.put(teamColor, 0);
        }
        
        for (Flag flag : flags.values()) {
            if (flag.activated) {
                map.put(flag.teamColor, map.get(flag.teamColor) + 1);
            }
        }
        
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(lp);
        
        TextView title = new TextView(this);
//        title.setLayoutParams(lp);
        title.setText("Распределение флажков по командам:");
        ll.addView(title);
        
        for (Map.Entry<Integer,Integer> pair : map.entrySet()) {
            TextView tv = new TextView(this);
            tv.setText(pair.getValue().toString());
            tv.setBackgroundColor(pair.getKey());
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//            tv.setLayoutParams(lp);
            ll.addView(tv);
        }
        
        
        
        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapsActivity.this);
        builder.setTitle("Конец игры!")
                .setView(ll)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        GoogleMapsActivity.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    /** Добавляем в players и обновляем все списки. Маркеры не обновляются!
     * Перед вызовом этого метода необходимо проверить, что мы не передаём клон myPlayer*/
    private void addPlayer(Player p) {
        // SortedList вызовет соответствующий notify если нужно
        players.add(p);
        playersMap.put(p.login, p);
        Log.d("my_tag", "put player in map: " + p.login);
        
        // исключаем тех, кто так же как и мы не выбрал цвет, себя и другие цвета
        if (p.hasTeam() && p.teamColor == myPlayer.teamColor && !Objects.equals(p.login, myPlayer.login)) {
            myTeammates.add(p);
            myTeammatesAdapter.notifyItemInserted(myTeammates.size() - 1);
        }
    }
    
    /** Обновляем все списки в bottomSheet и myTeammates. Маркеры не обновляются!*/
    private void updatePlayer(Player item, int teamColor) {
        
        // !!!Обязательно нужно найти позиции элементов в SortedList до их изменения!!!
        int playersPosition = players.indexOf(item);
        // Меняем сам элемент
        item.setTeamColor(teamColor);
        // Обновляем в players
        players.updateItemAt(playersPosition, item);
        
        
        // пытаемся добавить или удалить элемент из myTeammates
        
        int teammatesPosition = myTeammates.indexOf(item);
        
        // если элемент был, но цвет поменялся на неправильный
        if (item.teamColor != myPlayer.teamColor && teammatesPosition != -1) {
            myTeammates.remove(teammatesPosition);
            myTeammatesAdapter.notifyItemRemoved(teammatesPosition);
        }
        // если элемента не было, но цвет стал правильный
        else if (item.teamColor == myPlayer.teamColor && teammatesPosition == -1) {
            myTeammates.add(item);
            myTeammatesAdapter.notifyItemInserted(myTeammates.size() - 1);
        }
    }
    
    
    /** Обновляет все списки и маркер, но не посылает запрос серверу. (Предполагается, что запрос уже отправлен и подтверждён)*/
    void changeMyTeamColor(int newColor) {
        myLocationMarker.setIcon(getColoredImage(R.drawable.white_arrow_me, newColor));
        myTeammates.clear();
        
        // Обновляем элемент в списке всех людей в bottomsheet
        
        // !!!Обязательно нужно найти позиции элементов в SortedList до их изменения!!!
        int playersPosition = players.indexOf(myPlayer);
        // Меняем сам элемент
        myPlayer.setTeamColor(newColor);
        // Обновляем в players
        players.updateItemAt(playersPosition, myPlayer);
        
        
        // Обновляем teammates
        for (Player player : playersMap.values()) {
            if(player == myPlayer)
                continue;
            
            if (player.teamColor == newColor) {
                myTeammates.add(player);
            }
        }
        myTeammatesAdapter.notifyDataSetChanged();
    }
    
    /** Запускает анимацию захвата флага и обновляет все поля, но не посылает запрос серверу.
     * (Предполагается, что запрос уже отправлен и подтверждён)
     * @param cost Стоимость флага в энергии
     * @param colorToChange Цвет команды, в который должен быть перекрашен флаг*/
    public void pickFlag(final Flag flag, int cost, int colorToChange) {
        bottomSheetHandler.hideFlagBar();
    
        // изменение энергии, если это наша команда
        if (colorToChange == myPlayer.teamColor) {
            energyBlockHandler.addSpeed(1);
            energyBlockHandler.addEnergy(-cost);
        }
        // если это был активированный флаг из нашей команды, а его хотят забрать
        else if (flag.teamColor == myPlayer.teamColor && flag.activated) {
            // уменьшаем скорость нашей энергии
            energyBlockHandler.addSpeed(-1);
        }
        
        
        flag.activate();
        // перекрашиваем флаг в нужный цвет
        flag.teamColor = colorToChange;
        
        // Красим кешированную картинку
        flag.getMarker().setIcon(getColoredImage(whiteFlagBitmap, flag.getColorWithActivation()));
        
        
        
        
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
                        flag.getMarker().setAnchor(8f / 48f, 1f + t);

                        if (t > 0.0) {
                            // Post again 16ms later.
                            handler.postDelayed(this, 32);
                        }
                    }
                });
            }
        });
        
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
        TcpClient.instance = null;
        TcpClientFake.getInstance().stopClient();
        TcpClientFake.instance = null;
        
        if (energyBlockHandler != null)
            energyBlockHandler.timer.cancel();
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
            jo.put("name", myPlayer.name);
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
            b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_arrow_down_black_24dp, 0);
            
            // сброс пропущенных сообщений
            missedMsgCount = 0;
            // не показываем значок до первого пропущенного сообщения
//            tvMissedMsg.setText("");
//            tvMissedMsg.setVisibility(View.VISIBLE);
            
        } else {// показать
            rlChat.setVisibility(View.VISIBLE);
            b.setText("скрыть чат");
            b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_arrow_up_black_24dp, 0);
            
            tvMissedMsg.setVisibility(View.INVISIBLE);
        }
    }
    
    //------------------ПЕРЕМЕННЫЕ С РЕАЛИЗОВАННЫМИ ИНТЕРФЕЙСАМИ------------------
    
    
    /**
     * Регулярное обновление местоположения.
     */
    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            
//            Log.d("my_tag", "got location result: ("
//                    + location.getLatitude() + "," + location.getLongitude()
//                    + ") bearing = " + location.getBearing()
//                    + "accuracy = " + location.getAccuracy());
//            
            
            if (myLastLocation == null) {
                myLastLocation = location;
                onFirstLocationUpdate();
                
            }
            else {
                // отпраляем координаты на сервер, пропустив первую отправку, так как серверу сначала нужно отправить connection
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("type", "cords");
                    jo.put("lat", location.getLatitude());
                    jo.put("lng", location.getLongitude());
                    jo.put("login", myPlayer.login);
        
                    TcpClient.getInstance().sendMessage(jo);
                } catch (JSONException e) {}
                
                // если поменялось моё местоположение - обновляем его на карте
                if (myLastLocation.getLatitude() != location.getLatitude() || myLastLocation.getLongitude() != location.getLongitude()) {
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
            
        }
    };
    
}
