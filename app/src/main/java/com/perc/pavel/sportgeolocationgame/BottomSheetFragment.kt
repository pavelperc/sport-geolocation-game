package com.perc.pavel.sportgeolocationgame

import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.perc.pavel.sportgeolocationgame.activities.GoogleMapsActivity
import kotlinx.android.synthetic.main.activity_google_maps.llBottomSheetContainer

//import kotlinx.android.synthetic.main.activity_google_maps.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import org.jetbrains.anko.support.v4.toast

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by pavel on 23.01.2018.
 */

class BottomSheetFragment() : Fragment() {
    
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    lateinit var teamSharingAdapter: PlayerListAdapter
    
    
    private var selectedFlag: Flag? = null
    var energyFlagCost: Int = 0
    
    
    private val botsCount = 0
    
    
    /**
     * Если стоит true - то при случайном закрытии botton sheet снова открывается в состояние collapsed.
     */
    var keepNotHidden = false
    
    
    var isPreparedForFlags = false
    
    /** Shadow above the bottom sheet border*/
    val shadowHeight: Int by lazy {
        convertDpToPixels(0f)
    }
    
    val googleMapsActivity: GoogleMapsActivity by lazy {
        activity as GoogleMapsActivity
    }
    
    /** максимальный подъём relative layout. при скрытом bottom sheet - -8dp из-за тени на b. sh.*/
    val maxMarginPx: Int
        get() = bottomSheetBehavior.peekHeight - shadowHeight
    
    
    // для текстуры
    inner class BitmapDrawableNoMinimumSize(res: Resources, resId: Int) : BitmapDrawable(res, (res.getDrawable(resId) as BitmapDrawable).bitmap) {
        
        override fun getMinimumHeight(): Int {
            return 0
        }
        
        override fun getMinimumWidth(): Int {
            return 0
        }
        
        
    }
    
    /** this callback is only concerned with bottom sheet. Another part of callback is inside GoogleMapsActivity*/
    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN && keepNotHidden) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                //                    // удаление линии от нашего игрока до флажка
                //                    if (line != null)
                //                        line.remove();
            }
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                arrow.setImageResource(R.drawable.ic_expand_arrow_down_black_24dp)
            }
            
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                arrow.setImageResource(R.drawable.ic_expand_arrow_up_black_24dp)
            }
            
            googleMapsActivity.bottomSheetCallback.onStateChanged(bottomSheet, newState)
        }
        
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // do not delete this ?
            // because it crashes sometimes
            googleMapsActivity?.bottomSheetCallback.onSlide(bottomSheet, slideOffset)
            
        }
    }
    
    
    private fun setupFonts() {
        val Matiz = Typeface.createFromAsset(context.assets, "fonts/Matiz.ttf")
        
        tvSelectTeam.typeface = Matiz
        tvPlayersNames.typeface = Matiz
        tvFlagsCount.typeface = Matiz
        tvFlags.typeface = Matiz
        tvCircleSize.typeface = Matiz
        tvCircle.typeface = Matiz
        
        val PhosphateSolid = Typeface.createFromAsset(context.assets, "fonts/PhosphateSolid.ttf")
        
        btnStartGame.typeface = PhosphateSolid
        btnGenerateFlags.typeface = PhosphateSolid
        
    }
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.bottom_sheet, container, true)
    }
    
    
    /** After inflating fragment and before [GoogleMapsActivity.onCreate]*/
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        
        setupFonts()
        
        if (googleMapsActivity.createGame) {
            
            llCreateGame.visibility = View.VISIBLE
            
            setupCreateGame()
            
        } else {
            llCreateGame.visibility = View.GONE
        }
        
        llFlagInfo.visibility = View.GONE
        
        setupTeamSharing()
    }
    
    /** After [GoogleMapsActivity.onCreate]*/
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        setupChooseTeam()
        
        // настройка поведения нижнего экрана
        bottomSheetBehavior = BottomSheetBehavior.from(googleMapsActivity.llBottomSheetContainer)
    
        // this callback is only concerned with bottom sheet. Another part of callback is inside GoogleMapsActivity
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback)
    
    
        // Пока скрываем bottom sheet при вызове конструктора
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    
    
    
    }
    
    val teamsCount: Int
        get() = googleMapsActivity.teamColors.size
    
    
    // Converting seekbar progress to real values:
    
    // Converting itself:
    
    private val SeekBar.asFlagsCount: SeekBarConverter
        get() = SeekBarConverter(
                this,
                { progress -> (progress + 1) * teamsCount },
                { value -> value / teamsCount - 1 }
        )
    
    private val SeekBar.asCircleSize: SeekBarConverter
        get() = SeekBarConverter(
                this,
                { progress -> (progress + 1) * 100 },
                { value -> value / 100 - 1 }
        )
    
    
    // Simple version:

//    private var SeekBar.asFlagsCount: Int
//        get() = (progress + 1) * teamsCount
//        set(value) {
//            progress = value / teamsCount - 1
//        }

//    private val SeekBar.asCircleSize: Int
//        get() = (progress + 1) * 100
    
    
    val circleRadiusFromSeekBar: Int
        get() = sbCircleSize.asCircleSize.progr
    
    
    private fun setupCreateGame() {
        
        btnStartGame.setOnClickListener {
            val players = googleMapsActivity.playersMap.values.toList()
            if (!players.all { it.hasTeam() }) {
                toast("Не все игроки выбрали команду.")
                return@setOnClickListener
            }
            
            googleMapsActivity.setRoomIdVisible(false)
            hide()
            
            // отправить флажки
            
            val flags = googleMapsActivity.flags.values.toList()
            
            
            var jo = JSONObject()
            jo.put("type", "start_game")
            
            val ja = JSONArray()
            flags.forEach { ja.put(it.json) }
            jo.put("flags", ja)
            
            
            //                    Log.d("my_tag", "JSON WITH FLAGS:\n");
            //                    int maxLogSize = 1000;
            //                    String veryLongString = jo.toString();
            //                    for(int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
            //                        int start = i * maxLogSize;
            //                        int end = (i+1) * maxLogSize;
            //                        end = end > veryLongString.length() ? veryLongString.length() : end;
            //                        Log.d("my_tag", veryLongString.substring(start, end));
            //                    }
            
            googleMapsActivity.setLoadingVisible(true)
            
            
            googleMapsActivity.tcpClient.sendMessage(jo)
            //                    activity.onTCPMessageReceived(jo);
            
            
            // Упрощённый http запрос начала игры
            
            jo = JSONObject()
            jo.put("room_id", googleMapsActivity.roomId)
            
            googleMapsActivity.httpClient.httpPostRequest("start_game", jo, null)
            
        }
        
        btnGenerateFlags.setOnClickListener {
            // генерируем флажки
            googleMapsActivity.generateFlags(sbFlagsCount.asFlagsCount.progr, sbCircleSize.asCircleSize.progr)
            // обновляем центр кружка
            googleMapsActivity.updateCircleCenter()
            
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            btnStartGame.isEnabled = true
        }
        
        
        // short version, but not sure that the best
//        sbCircleSize.onSeekBarChangeListener {
//            onProgressChanged { seekBar, progress, fromUser ->
//                // use extension property asCircleSize
//                tvCircleSize.text = sbCircleSize.asCircleSize.toString()
//                googleMapsActivity.updateCircleRadius(sbCircleSize.asCircleSize)
//            }
//        }
        
        
        sbCircleSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // use extension property asCircleSize
                tvCircleSize.text = sbCircleSize.asCircleSize.progr.toString()
                googleMapsActivity.updateCircleRadius(sbCircleSize.asCircleSize.progr)
                
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        
        sbFlagsCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tvFlagsCount.text = sbFlagsCount.asFlagsCount.progr.toString()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        
        sbCircleSize.asCircleSize.progr = 200// 200 м
        
        // максимальное количество флажков будет всегда 100, интервалы - различны 
        sbFlagsCount.asFlagsCount.max = 100
//        sbFlagsCount.progress = 2// value = (2+1)*teamsCount
        sbFlagsCount.asFlagsCount.progr = 12 // will be rounded to multiple of teamsCount
        
        // Создание ботов
        
        //        final Spinner spBotTeam = (Spinner) activity.findViewById(R.id.spBotTeam);
        //        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(activity, android.R.layout.simple_spinner_item, activity.teamColors) {
        //            @NonNull
        //            @Override
        //            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //                View v = super.getView(position, convertView, parent);
        //                v.setBackgroundColor(getItem(position));
        //                return v;
        //            }
        //        
        //            @Override
        //            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //                View v = super.getDropDownView(position, convertView, parent);
        //                v.setBackgroundColor(getItem(position));
        //                return v;
        //            }
        //        };
        //    
        //        spBotTeam.setAdapter(adapter);
        //    
        //        Button btnAddBot = (Button) activity.findViewById(R.id.btnAddBot);
        //        btnAddBot.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                // Создание http запроса для создания бота
        //                
        //                String[] names = {"Бот Петя", "Бот Вася", "Бот Федя", "Бот Юра", "Бот Сеня"};
        //                
        //                final JSONObject jo = new JSONObject();
        //                try {
        //                    jo.put("room_id", activity.roomId);
        //                    jo.put("team_color", (int)spBotTeam.getSelectedItem());
        //                    
        //                    jo.put("name", names[botsCount % 5]);
        //                    jo.put("login", "bot" + botsCount);
        //                    jo.put("lat", activity.myLastLocation.getLatitude());
        //                    jo.put("lng", activity.myLastLocation.getLongitude());
        //                    
        //                } catch (JSONException e) {
        //                    
        //                }
        //    
        //                botsCount++;
        //                    
        //                
        //                // Вывод диалогового окна
        //                
        //                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        //                alertDialog.setTitle("Создать бота");
        //                alertDialog.setMessage("Вы уверены?\n" + jo);
        //                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
        //                        new DialogInterface.OnClickListener() {
        //                            public void onClick(DialogInterface dialog, int which) {
        //                                dialog.dismiss();
        //                                
        //                                // Получение результатов
        //                                TcpClient.getInstance().httpPostRequest("add_bot", jo, new HttpListener() {
        //                                    @Override
        //                                    public void onResponse(JSONObject message) {
        //                                        try {
        //                                            if (message.getBoolean("status")) {
        //                                                
        //                                            } else {
        //                                                Toast.makeText(activity, "server returned error:\n" + message.getString("error"), Toast.LENGTH_SHORT).show();
        //                                            }
        //                                        } catch (JSONException e) {
        //                                            Toast.makeText(activity, "JSONException:\n" + e, Toast.LENGTH_SHORT).show();
        //                                        }
        //                                    }
        //        
        //                                    @Override
        //                                    public void onFailure(String error) {
        //                                        Toast.makeText(activity, "Adding bot error:\n" + error, Toast.LENGTH_SHORT).show();
        //                                    }
        //                                });
        //                                
        //                            }
        //                        });
        //                alertDialog.show();
        //            }
        //        });
    }
    
    /**
     * Настройка области для выбора команды
     */
    private fun setupChooseTeam() {
        
        val extendedTeamColors = listOf(Player.NO_TEAM_COLOR) + googleMapsActivity.teamColors
        
        
        // creating dropdown list of squares with colors
        val adapter = object : ArrayAdapter<Int>(activity, android.R.layout.simple_spinner_item, extendedTeamColors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.setBackgroundColor(getItem(position)!!)
                tv.text = "  "
                return tv
            }
            
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getDropDownView(position, convertView, parent) as TextView
                tv.setBackgroundColor(getItem(position)!!)
                tv.text = "  "
                return tv
            }
        }
        
        spChooseTeam.adapter = adapter
        
        spChooseTeam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var firstTime = true
            
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // first time means selection by default in the beginning. We don't handle this.
                if (firstTime) {
                    firstTime = false
                    return
                }
                //                activity.changeMyTeamColor(extendedTeamColors.get(position));
                val jo = JSONObject()
                jo.put("login", googleMapsActivity.myPlayer.login)
                jo.put("type", "choose_team")
                jo.put("team_color", extendedTeamColors[position])
                
                setLoadingVisible(true)
                
                googleMapsActivity.httpClient.httpPostRequest("choose_team", jo, null)
                //                    activity.onTCPMessageReceived(jo);
                
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    
    /** Enables or disables loading circle inside bottom sheet*/
    fun setLoadingVisible(visible: Boolean) {
        pbLoadingBottomSheet.visibility = if (visible) View.VISIBLE else View.GONE
    }
    
    /**
     * Настройка области со списком подключившихся игроков
     */
    private fun setupTeamSharing() {
        rvTeamSharing.layoutManager = GridLayoutManager(googleMapsActivity, 3, GridLayoutManager.HORIZONTAL, false)
        //        rvTeamSharing.setLayoutManager(new LinearLayoutManager(activity));
        
        // we can't move teamSharingAdapter init in declaration
        teamSharingAdapter = PlayerListAdapter(context)
        rvTeamSharing.adapter = teamSharingAdapter
    }
    
    fun prepareForFlags() {
        if (isPreparedForFlags)
            return
        isPreparedForFlags = true
        
        keepNotHidden = false
        
        llFlagInfo.visibility = View.VISIBLE
        llCreateGame.visibility = View.GONE
        llChooseTeam.visibility = View.GONE
        llTeamSharing.visibility = View.GONE
        
        
        btnPickFlag.setOnClickListener {
            if (selectedFlag == null) {
                return@setOnClickListener
            }
            
            // TODO move json creation to special class
            
            val jo = JSONObject()
            jo.put("type", "activate_flag")
            jo.put("number", selectedFlag!!.number)
            // в цвет какой команды нужно перекрашивать
            jo.put("color_to_change", googleMapsActivity.myPlayer.teamColor)
            // количество энергии, нужное на захват флага
            jo.put("cost", energyFlagCost)
            //                    // текущая энергия команды до захвата флага
            //                    jo.put("sync_energy", activity.energyBlockHandler.getEnergy());
            //                    // скорость восстановления энергии без учёта нового флага
            //                    jo.put("sync_energy_speed", activity.energyBlockHandler.getSpeed());
            jo.put("old_color", selectedFlag!!.teamColor)
            jo.put("was_activated", selectedFlag!!.activated)
            jo.put("room_id", googleMapsActivity.roomId)
            googleMapsActivity.setLoadingVisible(true)
            googleMapsActivity.httpClient.httpPostRequest("activate_flag", jo, null)
            //                    TcpClient.getInstance().sendMessage(jo);
            //                    activity.onTCPMessageReceived(jo);
        }
    }
    
    fun openFlagBar(flag: Flag) {
        // если не был вызван метод prepareForFlags
        if (!isPreparedForFlags)
            return
        
        selectedFlag = flag
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        
        // обновление кнопки, tvFlagInfo и расчёт расстояния и стоимости флага
        updateSelectedFlagBar()
        
    }
    
    
    /** Обновление стоимости флага, текста с информацией о флаге, состояния кнопки активация флага */
    fun updateSelectedFlagBar() {
        // OVERRIDE mutable variable with local non-nullable and non-mutable
        val selectedFlag = selectedFlag ?: return
        
        
        val dist = googleMapsActivity.run {
            myLastLocation!!.distanceTo(selectedFlag.position.asLoc).toDouble()
        }
        
        val myTeamColor = googleMapsActivity.myPlayer.teamColor
        
        energyFlagCost = EnergyBlockHandler.getFlagCost(selectedFlag, dist, myTeamColor)
        
        
        tvFlagInfo.text = """Информация о флаге ${selectedFlag.number}:
Стоимость в энергии: $energyFlagCost
(стоимость пропорциональна расстоянию)
расстояние: ${"%.2f".format(dist)} м
активирован: ${if (selectedFlag!!.activated) "да" else "нет"}"""
        
        
        
        val currentEnergy = googleMapsActivity.energyBlockHandler?.energy?: return
        
        // флаг не может быть уже активированным нашей командой
        // и на его захват должно быть достаточно энергии
        val canPickFlag =
                !(selectedFlag.activated && selectedFlag.teamColor == myTeamColor)
                        && currentEnergy >= energyFlagCost
        
        btnPickFlag.isEnabled = canPickFlag
        
        //        // обновление линии от нашего игрока до флага
        //        if (line != null)
        //            line.remove();
        //    
        //        line = activity.googleMap.addPolyline(new PolylineOptions()
        //                .add(activity.locToLL(activity.myLastLocation), selectedFlag.getPosition())
        //                .width(10)
        //                .color(activity.getResources().getColor(R.color.energy_background_blue)));
    }
    
    private fun convertDpToPixels(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }
    
    /**
     * открытие выдвигающегося экрана снизу на полную величину
     */
    fun expand() {
        keepNotHidden = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
    
    /**
     * открытие выдвигающегося экрана снизу на неполную величину
     */
    fun showCollapsed() {
        keepNotHidden = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    
    
    fun hide() {
        keepNotHidden = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
    
    fun hideFlagBar() {
        if (!isPreparedForFlags)
            return
        selectedFlag = null
        hide()
    }
}