package com.perc.pavel.sportgeolocationgame;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Интерфейс для получения ответов от tcp сервера
 */
interface TcpConnectionListener {
    void onConnected();
    void onConnectionError(String error);
}

interface TcpMessageListener {
    /**
     * Вызывается при ответе сервера.
     * @param message Ответ сервера.
     */
    void onTCPMessageReceived(JSONObject message);
}
