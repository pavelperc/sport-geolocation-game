package com.perc.pavel.sportgeolocationgame;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

/**
 * Created by pavel on 14.02.2018.
 */

public class EnergyBlockHandler {
    
    static int INTERVAL_MS = 1000;
    
    static int getFlagCost(Flag flag, double distance, int myTeamColor) {
        int cost = (int) distance + 10;
        if (flag.teamColor != myTeamColor)// если это флаг чужой команды
            cost *= 2;
        if (flag.activated)// если флаг уже активирован чужой комадой
            cost *= 2;
        return cost;
    }
    
    
    GoogleMapsActivity activity;
    
    private int energy = 0;
    private int speed = 1;
    
    TextView tvEnergyValue;
    TextView tvEnergySpeed;
    
    CountDownTimer timer;
    
    EnergyBlockHandler(final GoogleMapsActivity activity) {
        this.activity = activity;
        
        activity.findViewById(R.id.ll_energy_block).setVisibility(View.VISIBLE);
    
        tvEnergyValue = (TextView) activity.findViewById(R.id.tvEnergyValue);
        tvEnergySpeed = (TextView) activity.findViewById(R.id.tvEnergySpeed);
        
        timer = new CountDownTimer(86400000L/*24h*/, INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                addEnergy(getSpeed());
                
                activity.bottomSheetHandler.updateSelectedFlagBar();
                
            }
    
            @Override
            public void onFinish() {
                
            }
        };
        timer.start();
        
    }
    
    void setEnergy(int energy) {
        this.energy = energy;
        tvEnergyValue.setText("" + energy);
    }
    
    void setSpeed(int speed) {
        this.speed = speed;
        tvEnergySpeed.setText("+" + speed);
    }
    
    int getEnergy() {
        return energy;
    }
    
    int getSpeed() {
        return speed;
    }
    
    void addEnergy(int energy) {
        setEnergy(getEnergy() + energy);
    }
    
    void addSpeed(int speed) {
        setSpeed(getSpeed() + speed);
    }
    
    
}
