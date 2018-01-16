package com.perc.pavel.sportgeolocationgame;

import org.json.JSONObject;

/**
 * Created by pavel on 12.09.2017.
 * <p>
 * Класс для работы с сервером через TCP.
 * 
 * В отдельном потоке ловит сообщения сервера и отсылает их всем подключённым слушателям.
 * <p>
 * Всё частично взято отсюда:
 * https://stackoverflow.com/questions/38162775/really-simple-tcp-client
 */

class TcpClient {
    
    // ПОМЕНЯТЬ
    private static final String SERVER_IP = "62.109.23.138"; //server IP address
    
    private static final int SERVER_PORT = 9090;
    
    
    private static TcpClient instance;
    
    
    static TcpClient getInstance() {
        if (instance == null)
            instance = new TcpClient();
        return instance;
    }
    
    
    /**
     * Отправить сообщение на сервер.
     *
     * @param message JSON объект, отправляемый клиентом.
     */
    void sendMessage(final JSONObject message) {
        
    }
    
    /**
     * Отключиться от сервера.
     */
    void stopClient() {
        
    }
    
    
    /**
     * Подключиться к серверу.
     *
     * @param messageListener Интерфейс для получения ответов от сервера и состояния о подключении
     */
    void startAsync(TcpListener messageListener) {
        
    }
    
    /**
     * Добавить ещё один messageListener
     *
     * @param messageListener интерфейс для обратного вызова.
     */
    void addMessageListener(TcpListener messageListener) {
        
    }
    
    /**
     * Удалить все messageListener
     */
    void clearAllMessageListeners() {
        
    }
    
    /**
     * Запрос сервера в режиме http.
     * @param message Сообщение серверу.
     * @param onResult Интерфейс, в который сервер отправляет ответ. (Вызывается в основном потоке через Handler.post)
     */
    void httpRequest(final JSONObject message, final HttpListener onResult){
        
    }
}