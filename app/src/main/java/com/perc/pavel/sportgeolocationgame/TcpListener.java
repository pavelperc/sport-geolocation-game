package com.perc.pavel.sportgeolocationgame;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Интерфейс для получения ответов от tcp сервера
 */
interface TcpListener {
    /**
     * Вызывается при ответе сервера.
     * @param message Ответ сервера.
     */
	void onTCPMessageReceived(JSONObject message);

//    /**
//     * Вызывается при подключении или отключении от сервера.
//     * @param isConnectedNow True - сервер подключён, False - сервер отключён.
//     */
//	void onTCPConnectionStatusChanged(boolean isConnectedNow);
//    
    
    void onConnected();
    void onDisconnected();
}
