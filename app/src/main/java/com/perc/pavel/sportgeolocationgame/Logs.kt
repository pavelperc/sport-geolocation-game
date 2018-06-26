package com.perc.pavel.sportgeolocationgame

import android.util.Log


/** Calls [Log.d] with tag "my_tag"*/
fun commonLog(message: String) {
    Log.d("my_tag", message)
}

/** Calls [Log.d] with tag "server_tag"*/
fun serverLog(message: String) {
    Log.d("server_tag", message)
}