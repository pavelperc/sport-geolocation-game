package com.perc.pavel.sportgeolocationgame.activities

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.CountDownTimer
import android.os.Handler
import android.os.SystemClock
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentActivity
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.util.SortedList
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.perc.pavel.sportgeolocationgame.*
import com.perc.pavel.sportgeolocationgame.serverworking.*
import kotlinx.android.synthetic.main.activity_google_maps.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
//import kotlinx.android.synthetic.main.bottom_sheet.*

import org.json.JSONException
import org.json.JSONObject

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.Random

class GoogleMapsActivity :
        FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, TcpMessageListener {
    
    val roomId: Int by lazy {
        intent.getIntExtra("roomId", -1)
    }
    val createGame: Boolean by lazy {
        intent.getBooleanExtra("createGame", false)
    }
    
    private val rnd = Random()
    lateinit var googleMap: GoogleMap
    lateinit var mapFragment: SupportMapFragment
    
    private var fakeGps: FakeGps? = null
    
    val playersMap: MutableMap<String, Player> = mutableMapOf()
    
    
    // TODO avoid strong connection with sortedList of players
    private val players: SortedList<Player>
        get() = bottomSheetFragment.teamSharingAdapter.players
    
    /** Служит для хранения команды, логина и имени, Location никогда не заполняется!!!!
     * TeamColor по умолчанию серый (Player.NO_TEAM_COLOR) */
    lateinit var myPlayer: Player
    
    
    /** Объекты флажков по их номеру */
    var flags = mutableMapOf<Int, Flag>()
    
    private val markerToFlagMap = mutableMapOf<Marker, Flag>()
    
    val teamColors: List<Int> by lazy {
        intent.getSerializableExtra("teamColors") as ArrayList<Int>
    }
    
    var circle: Circle? = null
    
    lateinit var bottomSheetFragment: BottomSheetFragment
    
    var energyBlockHandler: EnergyBlockHandler? = null
    
    //    private int myTeamColor = Player.NO_TEAM_COLOR;
    private var myLocationMarker: Marker? = null
    
    /** Последнее местоположение. */
    var myLastLocation: Location? = null
    
    private val whiteFlagBitmap: Bitmap by lazy {
        // Загружаем в оперативную память значок флажка
        BitmapFactory.decodeResource(resources, R.drawable.white_flag)
    }
    
    
    /** Layout, interacting with [CoordinatorLayout] as bottom sheet.
     * Needed for initialization of [BottomSheetFragment.bottomSheetBehavior]
     * with [BottomSheetBehavior.from]*/
    val containerForBottomSheet: LinearLayout
        get() = llBottomSheetContainer

//    /**
//     * Управляет отложенными потоками для обновления данных с сервера
//     */
//    private val updatePlayersHandler = Handler()
    
    /**
     * Нужен для запроса на старт и остановку обновлений геолокации
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    // Для чата
    
    //    private var mMessageRecycler: RecyclerView? = null
    private lateinit var messageListAdapter: MessageListAdapter
    
    private val messageList = mutableListOf<UserMessage>()
    //    private var etChatBox: EditText? = null
//    private var rlChat: RelativeLayout? = null
//    private var tvMissedMsg: TextView? = null
    private var missedMsgCount = 0
    
    
    val tcpClient = TcpClient.inUiThread()
    // TODO replace constructor with factory
    val httpClient = HttpClient().inUiThread
    
    private val myTeammates = mutableListOf<Player>()
    private val myTeammatesAdapter: MyTeammatesAdapter by lazy {
        MyTeammatesAdapter(this, myTeammates)
    }
    
    //------------------ПЕРЕМЕННЫЕ С РЕАЛИЗОВАННЫМИ ИНТЕРФЕЙСАМИ------------------
    
    
    /**
     * Регулярное обновление местоположения.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            
            //            Log.d("my_tag", "got location result: ("
            //                    + location.getLatitude() + "," + location.getLongitude()
            //                    + ") bearing = " + location.getBearing()
            //                    + "accuracy = " + location.getAccuracy());
            //            
            
            if (myLastLocation == null) {
                myLastLocation = location
                onFirstLocationUpdate(location)
                
            } else {
                // отпраляем координаты на сервер, пропустив первую отправку, так как серверу сначала нужно отправить connection
                
                val jo = JSONObject()
                jo.put("type", "cords")
                jo.put("lat", location.latitude)
                jo.put("lng", location.longitude)
                jo.put("login", myPlayer.login)
                
                tcpClient.sendMessage(jo)
                
                val oldLocation = myLastLocation!!
                
                // если поменялось моё местоположение - обновляем его на карте
                if (oldLocation.latitude != location.latitude || oldLocation.longitude != location.longitude) {
                    // Сами вычисляем направление текущего игрока
                    val newBearing = oldLocation.bearingTo(location)
                    
                    myLastLocation = location
                    myLastLocation?.bearing = newBearing
                    
                    
                    // Animating my location marker (if it exists)
                    myLocationMarker?.let { marker ->
                        // Поворот маркера
                        marker.rotation = location.bearing
                        
                        // Анимация маркера
                        val markerAnimator = ObjectAnimator.ofObject(marker, "position",
                                LatLngEvaluator(), marker.position, location.asLL)
                        markerAnimator.duration = 500
                        markerAnimator.start()
                    }
                    
                }
                
                // обновление стоимости флажка, если он выбран
                bottomSheetFragment.updateSelectedFlagBar()
            }
            
        }
    }
    
    /** Behaviour of activity when the [bottomSheetFragment] changes.
     * Contains the part of callback, that concerns ONLY!!! this activity.
     * Another part of callback should be defined inside [BottomSheetFragment]*/
    val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // при движении из hidden в collapsed анимируем bottomMargin у основного layout-а
            if (slideOffset < 0) {
                val params = rlMainScreen.layoutParams as CoordinatorLayout.LayoutParams
                with(bottomSheetFragment) {
                    params.bottomMargin = (maxMarginPx + slideOffset * (maxMarginPx + shadowHeight)).toInt()
                }
                rlMainScreen.layoutParams = params
            }
        }
        
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // если мы слишком быстро крутили вверх и окно не успело продвинуться
            if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                val params = rlMainScreen.layoutParams as CoordinatorLayout.LayoutParams
                params.bottomMargin = bottomSheetFragment.maxMarginPx
                rlMainScreen.layoutParams = params
            }
        }
    }
    
    
    fun setRoomIdVisible(visible: Boolean) {
        tvRoomId.visibility = if (visible) View.VISIBLE else View.GONE
    }
    
    fun setLoadingVisible(visible: Boolean) {
        pbLoading.visibility = if (visible) View.VISIBLE else View.GONE
    }
    
    
    private fun setupFonts() {
        val PhosphateSolid = Typeface.createFromAsset(assets, "fonts/PhosphateSolid.ttf")
        
        btnExpandChat.typeface = PhosphateSolid
        
        val PhosphateInline = Typeface.createFromAsset(assets, "fonts/PhosphateInline.ttf")
        
        tvEnergyValue.typeface = PhosphateInline
        tvEnergySpeed.typeface = PhosphateInline
        
        tvRoomId.typeface = PhosphateInline
        tvTimer.typeface = PhosphateInline
        tvRoomId.text = roomId.toString()
        llRoomId.visibility = View.VISIBLE
        
        
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_maps)
        
        setupFonts()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    
        // ... params from intent we receive in lazy way init (look above) ...
        
        
        // Current player profile
        val myProfile = intent.getSerializableExtra("profile") as Profile
        
        
        llTimer.visibility = View.GONE
        
        
        // Creating a list with my teammates on the right
        
        rvTeammates.layoutManager = LinearLayoutManager(this)


//        myTeammatesAdapter = MyTeammatesAdapter(this, myTeammates)
        rvTeammates.adapter = myTeammatesAdapter
        
        
        // добавляем себя в список игроков
        myPlayer = Player(myProfile.login, myProfile.name)
        //        playersMap.put(myPlayer.login, myPlayer);
        
        // создание выдвигающегося экрана снизу и списка игроков, привязанного к teamSharingAdapter
        bottomSheetFragment = supportFragmentManager.findFragmentById(R.id.bottom_sheet_fragment) as BottomSheetFragment
        
        // TODO understand how all these players' containers work
        
        addPlayer(myPlayer)
        
        
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        
        
        // Треть карты должна уехать вниз за экран когда mapFragment.getView будет отрисован
        mapFragment.view!!.post {
            val params = mapFragment.view!!.layoutParams
            // params.height изначально равна -1 (MATCH_PARENT)
            // а вот mapFragment.getView().getHeight() выдаёт настоящую высоту
            params.height = (mapFragment.view!!.height * 1.4).toInt()
            mapFragment.view!!.layoutParams = params
        }
        
        mapFragment.getMapAsync(this)

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        
        // Создание и настройка чата
        
        tvMissedMsg.visibility = View.INVISIBLE// чтобы btnExpandChatClick не прыгала
        // добавляем реакцию на нажатие кнопки отправки сообщения на клавиатуре
        etChatBox.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                btnChatSendClick(null)
                return@setOnEditorActionListener true
            }
            false
        }
        
        messageListAdapter = MessageListAdapter(this, messageList, myPlayer.name)
        
        rvMessageList.layoutManager = LinearLayoutManager(this)
        rvMessageList.adapter = messageListAdapter
        
        rlChat.visibility = View.GONE
        
        
        // добавлениие всех пользователей, подключившихся ранее
        if (!createGame) {
            val url = httpClient.urlBuilder
                    .addPathSegment("get_players_in_room")
                    .addQueryParameter("room_id", roomId.toString())
                    .build()
            
            httpClient.httpGetRequest(url, object : HttpListener {
                override fun onResponse(message: JSONObject) {
                    commonLog("Returned get_players_in_room: $message")
                    try {
                        if (!message.getBoolean("status")) {
                            onFailure(message.getString("error"), "server error in get_players_in_room")
                            return
                        }
                        // TODO json parser in separate file
                        
                        val ja = message.getJSONArray("players")
                        
                        for (i in 0 until ja.length()) {
                            val p = Player(ja.getJSONObject(i))
                            // не допускаем замены текущего игрока, так как потом невозможно будет найти объект myPLayer
                            if (p.login == myPlayer.login)
                                continue
                            
                            addPlayer(p)
                        }
                        
                        // настраиваем TCP после того, как добавили всех пользователей в комнате
                        setupTCP()
                        
                    } catch (e: JSONException) {
                        onFailure(e.message!!, "JSONException in get_players_in_room")
                        setupTCP()
                    }
                }
                
                override fun onFailure(error: String, title: String) {
//                    setupTCP()
                    alert(error, title).show()
                }
            })
            
        } else {
            setupTCP()
        }
        
    }
    
    private fun setupTCP() {
        tcpClient.addMessageListener(this)
        
        tcpClient.startAsync(ConnectionListener(false))
    }
    
    
    /**
     * Вывести диалоговое окно
     *
     * @param error              Сообщение об ошибке
     * param connectionListener Слушатель для переподключения. может быть null
     */
    private fun showConnectionErrorAlert(error: String, title: String) {
        
        alert {
            val serverIsStopped = if (tcpClient.isTcpRunning) "Сервер не остановлен." else "Сервер остановлен."
            isCancelable = false
            this.title = "Ошибка работы сервера!\n$serverIsStopped"
            message = "$title:\n$error"
            
            cancelButton { dialog -> dialog.cancel() }
            positiveButton("Reconnect") { dialog ->
                // если мы не закончили tcp connection, заканчиваем
                // потом ещё раз запускаемся заново с другим листенером.
                //                        if (connectionListener != null)
                tcpClient.reconnect(ConnectionListener(true))
                dialog.cancel()
            }
        }.show()
    }
    
    
    inner class ConnectionListener(private val reconnection: Boolean) : TcpConnectionListener {
        
        override fun onConnected() {
            serverLog("in onConnected.")
            toast("connected tcp")
            val jo = JSONObject()
            
            
            if (reconnection) {
                jo.put("type", "reconnection")
            } else {
                jo.put("type", "connection")
            }
            jo.put("login", myPlayer.login)
            tcpClient.sendMessage(jo)
            
            
        }
        
        override fun onConnectionError(error: String, title: String) {
            
            try { // на случай если активти уже закрыта.
                
                
                showConnectionErrorAlert(error, title)
                serverLog("showed alertDialog")
                
                // отключаем все кружочки загрузки
                bottomSheetFragment?.setLoadingVisible(false)
                
            } catch (e: Exception) {
                serverLog("exception in activity onConnectionError: " + e.message)
            }
            
        }
    }
    
    /** Callback for [SupportMapFragment.getMapAsync]*/
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        // загрузка стиля карты
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_blue_map_style))
        
        googleMap.uiSettings.isCompassEnabled = false
        //googleMap.setBuildingsEnabled(false);
        
        
        // -----настройка управления геолокациией-----
        
        val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val needFakeGps = pref.getBoolean("fakeGps", false)
        
        llRotate.visibility = if (needFakeGps) View.VISIBLE else View.GONE
        if (needFakeGps) {
            // Настройка fakeGps и начального местоположения
            var lat: Double
            var lng: Double
            try {
                val locationStr = pref.getString("startGps", "")
                val coords = locationStr.split("; ").map { it.toDouble() }
                lat = coords[0]
                lng = coords[1]
            } catch (e: Exception) {
//                // New York
//                lat = 40.730610;
//                lng = -73.935242;
                
                // Moscow
                lat = 55.7513367
                lng = 37.6255067
            }
            
            // SENDS FIRST CALLBACK RIGHT AFTER INITIALIZATION
            fakeGps = FakeGps(lat, lng, mLocationCallback)
            //fakeGps.start();
            
            // настройка переключателя ходьбы
            swchGo.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                    fakeGps?.start()
                else
                    fakeGps?.stop()
            }
            swchSuperSpeed.setOnCheckedChangeListener { buttonView, isChecked ->
                fakeGps?.superSpeed = isChecked
            }
            
        } else {
            // считываем реальную геолокацию
            startLocationUpdates()
        }
    }
    
    
    private fun startLocationUpdates() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 3000
        mLocationRequest.fastestInterval = 3000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        try {
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        } catch (ignored: SecurityException) {
        }
        commonLog("location update started")
    }
    
    /** Остановка насовсем */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
        fakeGps?.destroy()
        
        commonLog("location update stopped")
    }
    
    fun generateFlags(flagsNumber: Int, radius: Int) {
        // удаляем все старые флажки
        markerToFlagMap.keys.forEach { marker -> marker.remove() }
        
        markerToFlagMap.clear()
        flags.clear()
        
        val centerLL = myLastLocation?.asLL ?: return
        
        for (i in 0 until flagsNumber) {
            
            //            LatLng ll = new LatLng(lat + rnd.nextDouble() * delta - delta / 2, lng + rnd.nextDouble() * delta - delta / 2);
            
            val INNER_CIRCLE = 40// не генерировать флажки ближе чем на 40 м
            val llNew = FakeGps.getNextCoord(centerLL, rnd.nextInt(360), rnd.nextInt(radius - INNER_CIRCLE) + INNER_CIRCLE)
            
            val color = teamColors[i % teamColors.size]
            val flag = Flag(llNew.latitude, llNew.longitude, color, i)
            
            flags[flag.number] = flag
            createFlagMarker(flag)
        }
        
        
    }
    
    /** Создаёт объект маркера и добавляет его к флажку и в markerToFlagMap. */
    private fun createFlagMarker(flag: Flag) {
        val fm = googleMap.addMarker(MarkerOptions()
                .position(LatLng(flag.lat, flag.lng))
                .anchor(8f / 48f, 1f)
                .zIndex(2f)
                .icon(getColoredImage(whiteFlagBitmap, flag.colorWithActivation)))
        fm.tag = "flag"
        flag.marker = fm
        markerToFlagMap[fm] = flag
    }
    
    
    /**
     * Вызывается после первого получения геолокации игрока
     * Содержит старт и настройку новой игры, создание флажков и команд
     */
    private fun onFirstLocationUpdate(location: Location) {
        commonLog("in onFirstLocationUpdate")
        // Создаём маркер игрока
        myLocationMarker = googleMap.addMarker(MarkerOptions()
                .position(location.asLL)
                .anchor(0.5f, 0.5f)
                .icon(getColoredImage(R.drawable.white_arrow_me, myPlayer.teamColor))
                .zIndex(1f)
                .rotation(0f)
                .flat(true))
        myLocationMarker!!.tag = "player-location"
        
        //        generateFlags(30, 100);
        
        // обработка нажатия на маркер
        googleMap.setOnMarkerClickListener(this)
        
        googleMap.setOnMapClickListener { bottomSheetFragment.hideFlagBar() }
        
        // Мнгновенное перемещение камеры в центр локации с заданным масштабом
        //        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locToLL(myLastLocation), 14));
        
        // Анимация камеры
        val position = CameraPosition.Builder()
                .tilt(60f)// Наклон
                .target(location.asLL)
                .zoom(18f)
                .bearing(location.bearing)// Направление
                .build()
        
        //        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2500, null);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
        
        
        // открытие выдвигающегося экрана снизу
        Handler().postDelayed({ bottomSheetFragment.showCollapsed() }, 500)
        
        
        if (createGame) {
            // создание кружка, внутри которого будут флажки
            circle = googleMap.addCircle(CircleOptions()
                    .center(location.asLL)
                    .radius(bottomSheetFragment.circleRadiusFromSeekBar.toDouble())
                    .strokeColor(resources.getColor(R.color.colorPrimary)))
        }
        
        //        startPlayersUpdates();
    }

//    private fun startPlayersUpdates() {
//        Log.d("my_tag", "in startPlayersUpdates")
//        TcpClientFake.getInstance().startAsync(this, object : TcpConnectionListener {
//            override fun onConnected() {
//                
//            }
//            
//            fun onConnectionError(error: String) {
//                
//            }
//        }, teamColors, myLastLocation?.asLL)
//        //        updatePlayersRunnable.run();
//    }

//    private fun stopPlayersUpdates() {
//        //        updatePlayersHandler.removeCallbacks(updatePlayersRunnable);
//        //        Log.d("my_tag", "players update stopped");
//        
//        TcpClientFake.getInstance().stopClient()
//    }
    
    private fun createPlayerMarker(player: Player) {
        val marker = googleMap.addMarker(MarkerOptions()
                .position(player.coords)
                .anchor(0.5f, 0.5f)
                .icon(getColoredImage(R.drawable.white_arrow, player.teamColor))
                .title(player.login)
                .zIndex(0f)
                .flat(true))
        marker.tag = "player"
        player.marker = marker
    }
    
    /**
     * Обновление маркеров других игроков
     *
     * @param marker
     * @param p
     */
    private fun updatePlayerMarker(marker: Marker, p: Player) {
        // Сами вычисляем направление игрока
        val oldPos = marker.position.asLoc
        val newPos = p.coords.asLoc
        
        // проверка на то, что игрок сдвинулся
        if (oldPos.latitude == newPos.latitude && oldPos.longitude == newPos.longitude)
            return
        
        val newBearing = oldPos.bearingTo(newPos)
        
        // Поворот маркера
        marker.rotation = newBearing
        
        // Анимация маркера
        val markerAnimator = ObjectAnimator.ofObject(marker, "position",
                LatLngEvaluator(), marker.position, newPos.asLL)
        markerAnimator.duration = 500
        markerAnimator.start()
    }
    
    /** Converts [Location] to [LatLng]*/
    val Location.asLL: LatLng
        get() = LatLng(latitude, longitude)
    
    /** Converts [LatLng] to [Location]*/
    val LatLng.asLoc: Location
        get() = Location("").also { it.latitude = latitude; it.longitude = longitude }
    
    
    fun animateCameraToLocation(latLng: LatLng, bearing: Float? = null) {
        
        val builder = CameraPosition.Builder()
                .tilt(googleMap.cameraPosition.tilt)// Наклон
                .target(latLng)
                .zoom(18f)
        
        if (bearing != null)
            builder.bearing(bearing)// Направление
        
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()))
    }
    
    
    fun btnMyLocationClick(v: View) {
        myLastLocation?.also { loc ->
            animateCameraToLocation(loc.asLL, loc.bearing)
        }
    }
    
    fun btnZoomInClick(v: View) {
        googleMap.animateCamera(CameraUpdateFactory.zoomIn())
    }
    
    fun btnZoomOutClick(v: View) {
        googleMap.animateCamera(CameraUpdateFactory.zoomOut())
    }
    
    /**
     * Demonstrates converting a [Drawable] to a [BitmapDescriptor],
     * for use as a marker icon.
     */
    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth * 2,
                vectorDrawable.intrinsicHeight * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.MULTIPLY)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    
    
    /**
     * Получить по id ресурса картинку, окрашенную в цвет через Multiply.
     * При многократных вызовах лучше использовать [GoogleMapsActivity.getColoredImage].
     *
     * @param id ID ресурса с картинкой
     * @param color цвет
     * @return BitmapDescriptor (нужен для создания значков маркеров)
     */
    private fun getColoredImage(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        return getColoredImage(BitmapFactory.decodeResource(resources, id), color)
    }
    
    /**
     * Покрасить картинку в цвет через Multiply
     *
     * @param bitmap Картинка которую нужно покрасить
     * @param color цвет
     * @return BitmapDescriptor (нужен для создания значков маркеров)
     */
    private fun getColoredImage(bitmap: Bitmap, @ColorInt color: Int): BitmapDescriptor {
        //        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        val bitmapResult = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResult)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return BitmapDescriptorFactory.fromBitmap(bitmapResult)
    }
    
    /**
     * Обработка нажатий на маркеры
     *
     * @param marker маркер
     * @return возвращает обработано ли нажатие на маркер
     * (если обработано, то не надо открывать окно с информацией о маркере)
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.tag == "player-location")
            return true
        if (marker.tag == "player") {
            bottomSheetFragment.hideFlagBar()
            return false
        }
        
        // если маркер - флажок
        
        val flag = markerToFlagMap[marker]
        if (flag == null) {
            toast("unknown flag")
        } else {
            
            // Анимация флага - подпрыгивание
            val mHandler = Handler()
            mHandler.post {
                val handler = Handler()
                val start = SystemClock.uptimeMillis()
                val duration: Long = 200
                
                val interpolator = AnticipateInterpolator()
                
                handler.post(object : Runnable {
                    override fun run() {
                        val elapsed = SystemClock.uptimeMillis() - start
                        val t = Math.max(
                                1 - interpolator.getInterpolation(elapsed.toFloat() / duration), 0f)
                        flag.marker!!.setAnchor(8f / 48f, 1f + t * 0.2f)
                        
                        if (t > 0.0) {
                            // Post again 16ms later.
                            handler.postDelayed(this, 32)
                        }
                    }
                })
            }
            bottomSheetFragment.openFlagBar(flag)
        }
        return true
    }
    
    override fun onTCPMessageReceived(message: JSONObject) {
        try {
            if (message.toString() == "{\"status\":\"Connection successfull\"}")
                return
            
            when (message.getString("type")) {
                "message_chat" -> {
                    addMessageToChat(message.getString("message"), message.getString("name"))
                }
                "new_player_in_room" -> {
                    val p = Player(message)
                    
                    // не допускаем замены текущего игрока, так как потом невозможно будет найти объект myPLayer
                    if (p.login == myPlayer.login)
                        return
                    
                    addPlayer(p)
                    
                    // если есть куда ставить маркер
                    if (p.hasCoords()) {
                        // обновление маркера
                        if (p.hasMarker()) {
                            updatePlayerMarker(p.marker!!, p)
                        } else {
                            createPlayerMarker(p)
                        }
                    }
                }
                "start_game" -> {
                    pbLoading.visibility = View.GONE
                    
                    // если мы не создавали игру и не генерировали флажки - создаём их
                    if (!createGame) {
                        // удаляем все старые флажки
                        markerToFlagMap.keys.forEach { marker -> marker.remove() }
                        markerToFlagMap.clear()
                        flags.clear()
                        
                        val ja = message.getJSONArray("flags")
                        for (i in 0 until ja.length()) {
                            val flag = Flag(ja.getJSONObject(i))
                            flags[flag.number] = flag
                            createFlagMarker(flag)
                        }
                    }
                    circle?.remove()
                    
                    
                    // закрываем bottom_sheet
                    bottomSheetFragment.hide()
                    bottomSheetFragment.prepareForFlags()

//                    val PhosphateInline = Typeface.createFromAsset(assets, "fonts/PhosphateInline.ttf")
                    
                    energyBlockHandler = EnergyBlockHandler(this)

//                    energyBlockHandler!!.tvEnergyValue.typeface = PhosphateInline
//                    energyBlockHandler!!.tvEnergySpeed.typeface = PhosphateInline
                    
                    toast("GAME STARTED!!!")
                    
                    // maybe it's an extra
                    llRoomId.visibility = View.GONE
                    llTimer.visibility = View.VISIBLE
                    
                    
                    // начало таймера обратного отсчёта до конца игры
                    
                    object : CountDownTimer((1000 * 60 * 30).toLong(), 1) {
                        override fun onTick(millisUntilFinished: Long) {
                            tvTimer.text = SimpleDateFormat("mm:ss").format(Date(millisUntilFinished))
                        }
                        
                        override fun onFinish() {
                            finishGame()
                        }
                    }.start()
                }
                "cords" -> {
                    val login = message.getString("login")
                    // если отослали не мы
                    if (login != myPlayer.login) {

//                        val player = playersMap[login]?.also { foundPlayer ->
//                            // update coords in found player
//                            foundPlayer.setCoords(message.getDouble("lat"), message.getDouble("lng"))
//                        } ?: run {
//                            // UNREACHABLE
//                            commonLog("IN UNREACHABLE: unknown player sent coords")
//                            Player(message).also { newPlayer ->
//                                addPlayer(newPlayer)
//                            }
//                        }
                        
                        
                        val player: Player
                        
                        if (login in playersMap) {
                            player = playersMap.getValue(login)
                            player.setCoords(message.getDouble("lat"), message.getDouble("lng"))
                        } else {
                            // UNREACHABLE
                            commonLog("IN UNREACHABLE: unknown player sent coords")
                            player = Player(message)
                            addPlayer(player)
                        }
                        
                        // обновление маркера
                        
                        player.marker?.also { marker ->
                            updatePlayerMarker(marker, player)
                        } ?: createPlayerMarker(player)

//                        if (player.hasMarker()) {
//                            updatePlayerMarker(player.marker!!, player)
//                        } else {
//                            createPlayerMarker(player)
//                        }
                    }
                }
                "choose_team" -> {
                    val login = message.getString("login")
                    val teamColor = message.getInt("team_color")
                    
                    if (login == myPlayer.login) {
                        changeMyTeamColor(teamColor)
                        bottomSheetFragment.setLoadingVisible(false)
                        return
                    }
                    
                    val player = playersMap.getValue(login)
                    
                    updatePlayer(player, teamColor)
                }
                "activate_flag" -> {
                    pbLoading.visibility = View.GONE
                    
                    if (message.has("sync_energy") && message.has("sync_energy_speed")) {
                        
                        energyBlockHandler?.apply {
                            energy = message.getInt("sync_energy")
                            speed = message.getInt("sync_energy_speed")
                        }
                    }
                    
                    val flag = flags[message.getInt("number")]!!
                    
                    pickFlag(flag, message.optInt("cost", 0), message.optInt("color_to_change", flag.teamColor))
                    
                    // проверка что все флажки одного цвета и активированы и окончание игры
                    val endGame = flags.values.all {
                        it.activated && it.teamColor == flag.teamColor
                    }
                    
                    if (endGame)
                        finishGame()
                }
                "update_energy" -> {
                    energyBlockHandler?.apply {
                        energy = message.getInt("energy")
                        speed = message.getInt("speed")
                    }
                }
                else -> {
                    longToast("Unknown TCP message:\n$message")
                }
            }
            
            
        } catch (e: JSONException) {
            alert(e.message!!, "JSONException")
//            Toast.makeText(this, "JSONException:\n" + e.message, Toast.LENGTH_SHORT).show()
        }
        
    }
    
    private fun finishGame() {
        
        // цвет->количество флажков
        
        val map = flags.values
                .groupBy { it.teamColor }
                .mapValues { (_, flags) -> flags.count() }
        
        
        // TODO simplify game end alert dialog
        
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        ll.layoutParams = lp
        
        val title = TextView(this)
        //        title.setLayoutParams(lp);
        title.text = "Распределение флажков по командам:"
        ll.addView(title)
        
        for ((key, value) in map) {
            val tv = TextView(this)
            tv.text = value.toString()
            tv.setBackgroundColor(key)
            tv.textAlignment = View.TEXT_ALIGNMENT_CENTER
            //            tv.setLayoutParams(lp);
            ll.addView(tv)
        }
        
        
        val builder = AlertDialog.Builder(this@GoogleMapsActivity)
        builder.setTitle("Конец игры!")
                .setView(ll)
                .setPositiveButton("OK") { dialog, which ->
                    dialog.cancel()
                    this@GoogleMapsActivity.finish()
                }
        val alert = builder.create()
        alert.show()
    }
    
    /** Добавляем в players и обновляем все списки. Маркеры не обновляются!
     * Перед вызовом этого метода необходимо проверить, что мы не передаём клон myPlayer */
    private fun addPlayer(p: Player) {
        // Считаем, что если логин есть в playersMap, то он есть и в players
        if (p.login in playersMap) {
            // в старый элемент добавляем новый цвет
            updatePlayer(playersMap.getValue(p.login), p.teamColor)
            return
        } else {
            // SortedList вызовет соответствующий notify если нужно
            players.add(p)
        }
        
        playersMap[p.login] = p
        commonLog("put player in map: ${p.login}")
        
        // исключаем тех, кто так же как и мы не выбрал цвет, себя и другие цвета
        if (p.hasTeam() && p.teamColor == myPlayer.teamColor && p.login != myPlayer.login) {
            myTeammates.add(p)
            myTeammatesAdapter.notifyItemInserted(myTeammates.size - 1)
        }
    }
    
    /** Обновляем все списки в bottomSheet и myTeammates. Маркеры ОБНОВЛЯЮТСЯ! */
    private fun updatePlayer(item: Player, teamColor: Int) {
        
        // !!!Обязательно нужно найти позиции элементов в SortedList до их изменения!!!
        val playersPosition = players.indexOf(item)
        // Меняем сам элемент
        item.teamColor = teamColor
        // Обновляем в players
        players.updateItemAt(playersPosition, item)
        
        
        // пытаемся добавить или удалить элемент из myTeammates
        
        val teammatesPosition = myTeammates.indexOf(item)
        
        // если элемент был, но цвет поменялся на неправильный
        if (item.teamColor != myPlayer.teamColor && teammatesPosition != -1) {
            myTeammates.removeAt(teammatesPosition)
            myTeammatesAdapter.notifyItemRemoved(teammatesPosition)
        } else if (item.teamColor == myPlayer.teamColor && teammatesPosition == -1) {
            myTeammates.add(item)
            myTeammatesAdapter.notifyItemInserted(myTeammates.size - 1)
        }// если элемента не было, но цвет стал правильный
        
        if (item.hasCoords() && item.hasMarker())
            item.marker!!.setIcon(getColoredImage(R.drawable.white_arrow, item.teamColor))
    }
    
    
    /** Обновляет все списки и маркер, но не посылает запрос серверу. (Предполагается, что запрос уже отправлен и подтверждён) */
    fun changeMyTeamColor(newColor: Int) {
        myLocationMarker?.setIcon(getColoredImage(R.drawable.white_arrow_me, newColor))
        myTeammates.clear()
        
        // Обновляем элемент в списке всех людей в bottomsheet
        
        // !!!Обязательно нужно найти позиции элементов в SortedList до их изменения!!!
        val playersPosition = players.indexOf(myPlayer)
        // Меняем сам элемент
        myPlayer.teamColor = newColor
        // Обновляем в players
        players.updateItemAt(playersPosition, myPlayer)
        
        
        // Обновляем teammates
        for (player in playersMap.values) {
            if (player == myPlayer)
                continue
            
            if (player.teamColor == newColor) {
                myTeammates.add(player)
            }
        }
        myTeammatesAdapter.notifyDataSetChanged()
    }
    
    /** Запускает анимацию захвата флага и обновляет все поля, но не посылает запрос серверу.
     * (Предполагается, что запрос уже отправлен и подтверждён)
     * @param cost Стоимость флага в энергии
     * @param colorToChange Цвет команды, в который должен быть перекрашен флаг
     */
    fun pickFlag(flag: Flag, cost: Int, colorToChange: Int) {
        bottomSheetFragment.hideFlagBar()
        
        //        // изменение энергии, если это наша команда
        //        if (colorToChange == myPlayer.teamColor) {
        //            energyBlockHandler.addSpeed(1);
        //            energyBlockHandler.addEnergy(-cost);
        //        }
        //        // если это был активированный флаг из нашей команды, а его хотят забрать
        //        else if (flag.teamColor == myPlayer.teamColor && flag.activated) {
        //            // уменьшаем скорость нашей энергии
        //            energyBlockHandler.addSpeed(-1);
        //        }
        
        
        flag.activated = true
        // перекрашиваем флаг в нужный цвет
        flag.teamColor = colorToChange
        
        // Красим кешированную картинку
        flag.marker!!.setIcon(getColoredImage(whiteFlagBitmap, flag.colorWithActivation))
        
        
        // Анимация флага - подпрыгивание
        val mHandler = Handler()
        mHandler.post {
            val handler = Handler()
            val start = SystemClock.uptimeMillis()
            val duration: Long = 1000
            
            val interpolator = BounceInterpolator()
            
            handler.post(object : Runnable {
                override fun run() {
                    val elapsed = SystemClock.uptimeMillis() - start
                    val t = Math.max(
                            1 - interpolator.getInterpolation(elapsed.toFloat() / duration), 0f)
                    flag.marker!!.setAnchor(8f / 48f, 1f + t)
                    
                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 32)
                    }
                }
            })
        }
    }
    
    
    /**
     * Настраивает анимацию маркера.
     */
    private class LatLngEvaluator : TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.
        
        override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
            val latitude = startValue.latitude + (endValue.latitude - startValue.latitude) * fraction
            val longitude = startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
            
            return LatLng(latitude, longitude)
        }
    }
    
    override fun onDestroy() {
        stopLocationUpdates()
//        stopPlayersUpdates()
        
        tcpClient.stopClient()
//        TcpClientFake.getInstance().stopClient()
//        TcpClientFake.instance = null
        
        //        if (energyBlockHandler != null)
        //            energyBlockHandler.timer.cancel();
        
        
        // call super onDestroy after all our stopping actions!
        super.onDestroy()
    }
    
    
    // обработка нажатия кнопок поворота маркера с игроком
    
    fun btnTurnLeftClick(v: View) {
        myLocationMarker?.apply {
            // in degrees
            rotation -= 30
            // for instant camera turn right after current location button click
            myLastLocation?.bearing = rotation
            
            fakeGps?.bearing = rotation.toDouble()
        }
    }
    
    fun btnTurnRightClick(v: View) {
        myLocationMarker?.apply {
            // in degrees
            rotation += 30
            // for instant camera turn right after current location button click
            myLastLocation?.bearing = rotation
            
            fakeGps?.bearing = rotation.toDouble()
        }
    }
    
    
    fun btnChatSendClick(v: View?) {
        if (etChatBox.text.toString() == "")
            return
        
        val jo = JSONObject()
        jo.put("type", "message_chat")
        jo.put("name", myPlayer.name)
        jo.put("message", etChatBox!!.text.toString())
        
        tcpClient.sendMessage(jo)
        
        // напишется в ответе сервера
        //        addMessageToChat(etChatBox.getText().toString(), myLogin);
        etChatBox.setText("")
    }
    
    private fun addMessageToChat(message: String, name: String) {
        
        // TODO move all chat in separate fragment
        
        messageList.add(UserMessage(message, name))
        messageListAdapter.notifyItemInserted(messageList.size - 1)
        rvMessageList.scrollToPosition(messageList.size - 1)
        
        // если сообщения скрыты
        if (rlChat.visibility == View.GONE) {
            missedMsgCount++
            // показать количество непрочитанных сообщений
            tvMissedMsg.text = "+$missedMsgCount"
            if (tvMissedMsg.visibility == View.INVISIBLE)
                tvMissedMsg.visibility = View.VISIBLE
            
            // анимация тряски
            tvMissedMsg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
        }
        //        messageListAdapter.notifyItemInserted(messageList.size() - 1);
    }
    
    
    fun btnExpandChatClick(v: View) {
        val b = v as Button
        if (rlChat.visibility == View.VISIBLE) {// скрыть 
            rlChat.visibility = View.GONE
            //b.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat));
            
            // сброс пропущенных сообщений
            missedMsgCount = 0
            // не показываем значок до первого пропущенного сообщения
            //            tvMissedMsg.setText("");
            //            tvMissedMsg.setVisibility(View.VISIBLE);
            
        } else {// показать
            rlChat.visibility = View.VISIBLE
            //b.setBackgroundDrawable(getResources().getDrawable(R.drawable.close));
            
            tvMissedMsg.visibility = View.INVISIBLE
        }
    }
    
    
    /** Updates center of the circle around flags with my last location*/
    fun updateCircleCenter() {
        circle?.center = myLastLocation?.asLL ?: return
    }
    
    fun updateCircleRadius(radius: Int) {
        circle?.radius = radius.toDouble()
    }
    
}
