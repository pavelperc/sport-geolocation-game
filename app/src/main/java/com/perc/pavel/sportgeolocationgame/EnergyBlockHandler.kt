package com.perc.pavel.sportgeolocationgame

import android.view.View
import android.widget.TextView
import com.perc.pavel.sportgeolocationgame.activities.GoogleMapsActivity
import kotlinx.android.synthetic.main.activity_google_maps.*
/**
 * Created by pavel on 14.02.2018.
 */

class EnergyBlockHandler(private var activity: GoogleMapsActivity) {
    
    companion object {
        
        const val INTERVAL_MS = 1000
        
        fun getFlagCost(flag: Flag, distance: Double, myTeamColor: Int): Int {
            var cost = (distance.toInt() + 10) * 2// все стоимости увеличиваем в 2 раза.
            
            if (flag.teamColor != myTeamColor)
            // если это флаг чужой команды
                cost *= 2
            if (flag.activated)
            // если флаг уже активирован чужой комадой
                cost *= 2
            return cost
        }
    }
    
//    CountDownTimer timer;
    var energy = 0
        set(energy) {
            field = energy
            tvEnergyValue.text = energy.toString()
        }
    
    // начальная скорость равна 5
    var speed = 5
        set(speed) {
            field = speed
            tvEnergySpeed.text = "+$speed"
        }
    val tvEnergyValue: TextView = activity.tvEnergyValue
    
    val tvEnergySpeed: TextView = activity.tvEnergySpeed
    
    init {
        // TODO very bad practice of accessing activity view
        activity.ll_energy_block.visibility = View.VISIBLE
        
        tvEnergySpeed.text = "+$speed"
        
        //        timer = new CountDownTimer(86400000L/*24h*/, INTERVAL_MS) {
        //            @Override
        //            public void onTick(long millisUntilFinished) {
        //                addEnergy(getSpeed());
        //                
        //                activity.bottomSheetFragment.updateSelectedFlagBar();
        //                
        //            }
        //    
        //            @Override
        //            public void onFinish() {
        //                
        //            }
        //        };
        //        timer.start();
        
    }
}
