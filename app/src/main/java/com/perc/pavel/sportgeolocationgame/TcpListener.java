package com.perc.pavel.sportgeolocationgame;

public interface TcpListener {
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
