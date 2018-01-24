package com.perc.pavel.sportgeolocationgame;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by pavel on 23.01.2018.
 */

public class BottomSheetHandler {
    private final BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private GoogleMapsActivity activity;
    private RelativeLayout rlMainScreen;
    
    /** Если стоит true - то при случайном закрытии botton sheet снова открывается в состояние collapsed.*/
    boolean keepNotHidden = false;
    
    public BottomSheetHandler(final GoogleMapsActivity activity) {
        this.activity = activity;
        // получение вью нижнего экрана
        LinearLayout llBottomSheet = (LinearLayout) activity.findViewById(R.id.bottom_sheet);
        rlMainScreen = (RelativeLayout) activity.findViewById(R.id.rlMainScreen);
        final SeekBar sbCircleSize = (SeekBar) activity.findViewById(R.id.sbCircleSize);
        final SeekBar sbFlagsCount = (SeekBar) activity.findViewById(R.id.sbFlagsCount);
        final TextView tvCircleSize = (TextView) activity.findViewById(R.id.tvCircleSize);
        final TextView tvFlagsCount = (TextView) activity.findViewById(R.id.tvFlagsCount);
        Button btnGenerateFlags = (Button) activity.findViewById(R.id.btnGenerateFlags);
        final Button btnStartGame = (Button) activity.findViewById(R.id.btnStartGame);
        
        // настройка поведения нижнего экрана
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && keepNotHidden) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    
                }
            }
            
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.d("b_tag", "slideOffset: " + slideOffset);
                // при движении из hidden в collapsed анимируем bottomMargin у основного layout-а
                if (slideOffset < 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rlMainScreen.getLayoutParams();
                    int maxMargin = convertDpToPixels(70);
                    params.bottomMargin = (int) (maxMargin + slideOffset * maxMargin);
                    rlMainScreen.setLayoutParams(params);
                }
            }
        });
        
        
        // Пока скрываем bottom sheet при вызове конструктора
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                // отправить флажки
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
        
        // максимальное количество флажков будет всегда 100, интервалы - различны 
        sbFlagsCount.setMax((100 - 1) / activity.teamColors.size());
        sbFlagsCount.setProgress(2);// value = (2+1)*teamsCount
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
}
