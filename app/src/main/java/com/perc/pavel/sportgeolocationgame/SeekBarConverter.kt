package com.perc.pavel.sportgeolocationgame

import android.widget.SeekBar

/** Special converter class for [SeekBar] progress and maximum*/
class SeekBarConverter(
        val seekBar: SeekBar,
        val progressToValue: (progress: Int) -> Int,
        val valueToProgress: (value: Int) -> Int
) {
    /** Progress value of [SeekBar], converted through lambdas*/
    /** Progress value of [SeekBar], converted through lambdas*/
    var progr: Int
        get() = progressToValue(seekBar.progress)
        set(value) {
            seekBar.progress = valueToProgress(value)
        }
    
    /** Maximum value of [SeekBar], converted through lambdas*/
    var max: Int
        get() = progressToValue(seekBar.max)
        set(value) {
            seekBar.max = valueToProgress(value)
        }
    
    /** Zero progress value of [SeekBar], converted through lambdas*/
    val min: Int
        get() = progressToValue(0)
}