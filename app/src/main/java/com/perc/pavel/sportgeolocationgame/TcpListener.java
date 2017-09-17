package com.perc.pavel.sportgeolocationgame;


/**
 * Интерфейс для получения ответов от сервера
 */
interface TcpListener {
    /**
     * Вызывается при ответе сервера.
     * @param message Ответ сервера.
     */
	void onTCPMessageReceived(String message);

    /**
     * Вызывается при подключении или отключении от сервера.
     * @param isConnectedNow True - сервер подключён, False - сервер отключён.
     */
	void onTCPConnectionStatusChanged(boolean isConnectedNow);
}
