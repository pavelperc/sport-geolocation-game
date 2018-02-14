package com.perc.pavel.sportgeolocationgame;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

/**
 * Created by pavel on 14.02.2018.
 */

public class EnergyBlockHandler {
    
    static int INTERVAL_MS = 1000;
    static int getFlagCost(double distance) {
        return (int) distance;
    }
    
    
    GoogleMapsActivity activity;
    
    private int energy = 0;
    private int speed = 1;
    
    TextView tvEnergyValue;
    TextView tvEnergySpeed;
    
    CountDownTimer timer;
    
    EnergyBlockHandler(GoogleMapsActivity activity) {
        this.activity = activity;
        
        activity.findViewById(R.id.ll_energy_block).setVisibility(View.VISIBLE);
    
        tvEnergyValue = (TextView) activity.findViewById(R.id.tvEnergyValue);
        tvEnergySpeed = (TextView) activity.findViewById(R.id.tvEnergySpeed);
        
        timer = new CountDownTimer(86400000L/*24h*/, INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                addEnergy(getSpeed());
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
