package com.perc.pavel.sportgeolocationgame.serverworking

import org.json.JSONObject

/**
 * Created by pavel on 19.09.2017.
 *
 * Интерфейс для получения ответов от http сервера
 */
interface HttpListener {
    /**
     * Вызывается при ответе сервера.
     * @param message Ответ сервера.
     */
    fun onResponse(message: JSONObject)
    
    fun onFailure(error: String, title: String = "")
}
