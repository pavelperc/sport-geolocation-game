package com.perc.pavel.sportgeolocationgame;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by pavel on 23.01.2018.
 */

public class BottomSheetHandler {
    private final BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private GoogleMapsActivity activity;
    PlayerListAdapter teamSharingAdapter;
    
    SeekBar sbCircleSize;
    SeekBar sbFlagsCount;

    TextView tvCircle;
    TextView tvCirceSizeOut;
    TextView tvFlags;
    TextView tvFlagsCountOut;
    TextView tvPlayersName;
    TextView tvSelectTeam;
    Button btnGenerateFlags;
    Button btnStartGame;

    ProgressBar pbLoadingBottomSheet;
    
    TextView tvFlagInfo;
    Button btnPickFlag;
    private Flag selectedFlag;
    int energyFlagCost;


    private int botsCount = 0;
    
    
    // для текстуры
    public class BitmapDrawableNoMinimumSize extends BitmapDrawable {
        
        public BitmapDrawableNoMinimumSize(Resources res, int resId) {
            super(res, ((BitmapDrawable)res.getDrawable(resId)).getBitmap());
        }
        
        @Override
        public int getMinimumHeight() {
            return 0;
        }
        @Override
        public int getMinimumWidth() {
            return 0;
        }
    }
    
    
    /**
     * Если стоит true - то при случайном закрытии botton sheet снова открывается в состояние collapsed.
     */
    boolean keepNotHidden = false;
    
    public BottomSheetHandler(final GoogleMapsActivity activity) {
        this.activity = activity;
        // получение вью нижнего экрана
        LinearLayout llBottomSheet = (LinearLayout) activity.findViewById(R.id.bottom_sheet);
        final ImageView arrow = (ImageView) activity.findViewById(R.id.arrow);
        arrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        // Экран активити с картой
        final RelativeLayout rlMainScreen = (RelativeLayout) activity.findViewById(R.id.rlMainScreen);
        
//        // Настройка повторяющейся текстуры фона для bottomSheet
//        BitmapDrawable bmpd =new BitmapDrawableNoMinimumSize(activity.getResources(), R.drawable.metal_texture);
//        bmpd.setTileModeX(Shader.TileMode.REPEAT);
//        bmpd.setTileModeY(Shader.TileMode.REPEAT);
//        activity.findViewById(R.id.ll_background).setBackground(bmpd);
        
        
        
        // настройка поведения нижнего экрана
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        // максимальный подъём relative layout. при скрытом bottom sheet - -8dp из-за тени на b. sh.
        final int shadowHeight = convertDpToPixels(0);
        final int maxMarginPx = bottomSheetBehavior.getPeekHeight() - shadowHeight;
        
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && keepNotHidden) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
//                    // удаление линии от нашего игрока до флажка
//                    if (line != null)
//                        line.remove();
                }
                
                
                // если мы слишком быстро крутили вверх и окно не успело продвинуться
                if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rlMainScreen.getLayoutParams();
                    params.bottomMargin = maxMarginPx;
                    rlMainScreen.setLayoutParams(params);
                }
                
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    arrow.setImageResource(R.drawable.ic_expand_arrow_down_black_24dp);
                }
                
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    arrow.setImageResource(R.drawable.ic_expand_arrow_up_black_24dp);
                }
                
            }
            
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.d("b_tag", "slideOffset: " + slideOffset);
                // при движении из hidden в collapsed анимируем bottomMargin у основного layout-а
                if (slideOffset < 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rlMainScreen.getLayoutParams();
                    params.bottomMargin = (int) (maxMarginPx + slideOffset * (maxMarginPx + shadowHeight));
                    rlMainScreen.setLayoutParams(params);
                }
            }
        });
        
        
        pbLoadingBottomSheet = (ProgressBar) activity.findViewById(R.id.pbLoadingBottomSheet); 
        
        // Пока скрываем bottom sheet при вызове конструктора
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        
        if (activity.createGame) {
            activity.findViewById(R.id.ll_create_game).setVisibility(View.VISIBLE);
//            activity.findViewById(R.id.ll_choose_team).setVisibility(View.GONE);
            setupCreateGame();
            
        } else {
            activity.findViewById(R.id.ll_create_game).setVisibility(View.GONE);
//            activity.findViewById(R.id.ll_choose_team).setVisibility(View.VISIBLE);
        }
        activity.findViewById(R.id.ll_flag_info).setVisibility(View.GONE);
        
        
        setupChooseTeam();
        
        setupTeamSharing();
    }

    
    private void setupCreateGame() {
        sbCircleSize = (SeekBar) activity.findViewById(R.id.sbCircleSize);
        sbFlagsCount = (SeekBar) activity.findViewById(R.id.sbFlagsCount);
        final TextView tvCircleSize = (TextView) activity.findViewById(R.id.tvCircleSize);
        final TextView tvFlagsCount = (TextView) activity.findViewById(R.id.tvFlagsCount);
        Button btnGenerateFlags = (Button) activity.findViewById(R.id.btnGenerateFlags);
        final Button btnStartGame = (Button) activity.findViewById(R.id.btnStartGame);


        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Player player : activity.playersMap.values()) {
                    if (!player.hasTeam()) {
                        Toast.makeText(activity, "Не все игроки выбрали команду.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                
                activity.tvRoomId.setVisibility(View.GONE);
                hide();
                
                // отправить флажки
                
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("type", "start_game");
                    
                    JSONArray ja = new JSONArray();
                    for (Flag flag : activity.flags.values()) {
                        ja.put(flag.getJson());
                    }
                    jo.put("flags", ja);
                    
                    
//                    Log.d("my_tag", "JSON WITH FLAGS:\n");
//                    int maxLogSize = 1000;
//                    String veryLongString = jo.toString();
//                    for(int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
//                        int start = i * maxLogSize;
//                        int end = (i+1) * maxLogSize;
//                        end = end > veryLongString.length() ? veryLongString.length() : end;
//                        Log.d("my_tag", veryLongString.substring(start, end));
//                    }
                    
                    activity.pbLoading.setVisibility(View.VISIBLE);
                    TcpClient.getInstance().sendMessage(jo);
//                    activity.onTCPMessageReceived(jo);
                    
                    
                    // Упрощённый http запрос начала игры
                    
                    jo = new JSONObject();
                    jo.put("room_id", activity.roomId);
                    
                    TcpClient.getInstance().httpPostRequest("start_game", jo, null);
                    
                    
                } catch (JSONException ignored) {
                }
            }
        });
        
        btnGenerateFlags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // генерируем флажки
                activity.generateFlags((sbFlagsCount.getProgress() + 1) * activity.teamColors.size(), (sbCircleSize.getProgress() + 1) * 100);
                // обновляем центр кружка
                activity.circle.setCenter(activity.locToLL(activity.myLastLocation));
                
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                btnStartGame.setEnabled(true);
            }
        });
        
        sbCircleSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCircleSize.setText("" + (progress + 1) * 100);
                if (activity.circle != null)
                    activity.circle.setRadius((progress + 1) * 100);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                
            }
        });
        
        sbFlagsCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvFlagsCount.setText("" + (progress + 1) * activity.teamColors.size());
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                
            }
        });
        sbCircleSize.setProgress(1);// 200 м
        
        // максимальное количество флажков будет всегда 100, интервалы - различны 
        sbFlagsCount.setMax((100 - 1) / activity.teamColors.size());
        sbFlagsCount.setProgress(2);// value = (2+1)*teamsCount
        
        
        
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
    private void setupChooseTeam() {
        Spinner spChooseTeam = (Spinner) activity.findViewById(R.id.spChooseTeam);

        
        final List<Integer> extendedTeamColors = new ArrayList<>(activity.teamColors);
        extendedTeamColors.add(0, Player.NO_TEAM_COLOR);

        tvCircle = (TextView) activity.findViewById(R.id.tvCircle);
        tvCirceSizeOut = (TextView) activity.findViewById(R.id.tvCircleSize);
        tvFlags = (TextView) activity.findViewById(R.id.tvFlags);
        tvFlagsCountOut = (TextView) activity.findViewById(R.id.tvFlagsCount);
        tvPlayersName = (TextView) activity.findViewById(R.id.tvPlayersNames);
        tvSelectTeam = (TextView) activity.findViewById(R.id.tvSelectTeam);
        btnGenerateFlags = (Button) activity.findViewById(R.id.btnGenerateFlags);
        btnStartGame = (Button) activity.findViewById(R.id.btnStartGame);

        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(activity, android.R.layout.simple_spinner_item, extendedTeamColors) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                v.setBackgroundColor(getItem(position));
                return v;
            }
            
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                v.setBackgroundColor(getItem(position));
                return v;
            }
        };
        
        spChooseTeam.setAdapter(adapter);
        
        spChooseTeam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstTime = true;
            
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstTime) {
                    firstTime = false;
                    return;
                }
//                activity.changeMyTeamColor(extendedTeamColors.get(position));
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("login", activity.myPlayer.login);
                    jo.put("type", "choose_team");
                    jo.put("team_color", extendedTeamColors.get(position));
                    
                    
                    
                    pbLoadingBottomSheet.setVisibility(View.VISIBLE);
                    TcpClient.getInstance().httpPostRequest("choose_team", jo, null);
//                    activity.onTCPMessageReceived(jo);
                } catch (JSONException e) {
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
        });
    }
    
    /**
     * Настройка области со списком подключившихся игроков
     */
    private void setupTeamSharing() {
        RecyclerView rvTeamSharing = (RecyclerView) activity.findViewById(R.id.rv_team_sharing);
        
        rvTeamSharing.setLayoutManager(new GridLayoutManager(activity, 3, GridLayoutManager.HORIZONTAL, false));
//        rvTeamSharing.setLayoutManager(new LinearLayoutManager(activity));
        teamSharingAdapter = new PlayerListAdapter(activity);
//        activity.players = teamSharingAdapter.getPlayers();
        rvTeamSharing.setAdapter(teamSharingAdapter);
    }
    
    
    boolean isPreparedForFlags() {
        return tvFlagInfo != null && btnPickFlag != null;
    }
    
    void prepareForFlags() {
        if (isPreparedForFlags())
            return;
        
        keepNotHidden = false;
        activity.findViewById(R.id.ll_flag_info).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.ll_create_game).setVisibility(View.GONE);
        activity.findViewById(R.id.ll_choose_team).setVisibility(View.GONE);
        activity.findViewById(R.id.ll_team_sharing).setVisibility(View.GONE);
        
        tvFlagInfo = (TextView) activity.findViewById(R.id.tvFlagInfo);
        btnPickFlag = (Button) activity.findViewById(R.id.btnPickFlag);
        btnPickFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFlag == null) {
                    return;
                }
                
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("type", "activate_flag");
                    jo.put("number", selectedFlag.number);
                    // в цвет какой команды нужно перекрашивать
                    jo.put("color_to_change", activity.myPlayer.teamColor);
                    // количество энергии, нужное на захват флага
                    jo.put("cost", energyFlagCost);
//                    // текущая энергия команды до захвата флага
//                    jo.put("sync_energy", activity.energyBlockHandler.getEnergy());
//                    // скорость восстановления энергии без учёта нового флага
//                    jo.put("sync_energy_speed", activity.energyBlockHandler.getSpeed());
                    jo.put("old_color", selectedFlag.teamColor);
                    jo.put("was_activated", selectedFlag.activated);
                    jo.put("room_id", activity.roomId);
                    activity.pbLoading.setVisibility(View.VISIBLE);
                    TcpClient.getInstance().httpPostRequest("activate_flag", jo, null);
//                    TcpClient.getInstance().sendMessage(jo);
//                    activity.onTCPMessageReceived(jo);
                } catch (JSONException e) {
                }
                
            }
        });
    }
    
    void openFlagBar(Flag flag) {
        // если не был вызван метод prepareForFlags
        if (!isPreparedForFlags())
            return;
        
        selectedFlag = flag;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    
    
        // обновление кнопки, tvFlagInfo и расчёт расстояния и стоимости флага
        updateSelectedFlagBar();
        
    }
    
    
    /** Обновление стоимости флага, текста с информацией о флаге, состояния кнопки активация флага*/
    void updateSelectedFlagBar() {
        if (selectedFlag == null)
            return;
        
        double dist = activity.myLastLocation.distanceTo(activity.llToLoc(selectedFlag.getPosition()));
        energyFlagCost = EnergyBlockHandler.getFlagCost(selectedFlag, dist, activity.myPlayer.teamColor);
        
        tvFlagInfo.setText(String.format("Информация о флаге %d:\nСтоимость в энергии: %d\n(стоимость пропорциональна расстоянию)\nрасстояние: %.2fм\nактивирован: %s"
                , selectedFlag.number, energyFlagCost, dist, selectedFlag.activated ? "да" : "нет"));
        
        
        // флаг не может быть уже активированным нашей командой
        // и на его захват должно быть достаточно энергии
        boolean canPickFlag = !(selectedFlag.activated
                && selectedFlag.teamColor == activity.myPlayer.teamColor)
                && activity.energyBlockHandler.getEnergy() >= energyFlagCost;
        
        btnPickFlag.setEnabled(canPickFlag);
        
//        // обновление линии от нашего игрока до флага
//        if (line != null)
//            line.remove();
//    
//        line = activity.googleMap.addPolyline(new PolylineOptions()
//                .add(activity.locToLL(activity.myLastLocation), selectedFlag.getPosition())
//                .width(10)
//                .color(activity.getResources().getColor(R.color.energy_background_blue)));
    }
    
    private int convertDpToPixels(float dp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, activity.getResources().getDisplayMetrics());
        return px;
    }
    
    /**
     * открытие выдвигающегося экрана снизу на полную величину
     */
    void expand() {
        keepNotHidden = true;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    
    /**
     * открытие выдвигающегося экрана снизу на неполную величину
     */
    void showCollapsed() {
        keepNotHidden = true;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    
    
    void hide() {
        keepNotHidden = false;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    
    void hideFlagBar() {
        if (!isPreparedForFlags())
            return;
        selectedFlag = null;
        hide();
    }
}
