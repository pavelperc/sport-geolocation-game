package com.perc.pavel.sportgeolocationgame.serverworking


import android.os.Handler
import android.util.Log
import okhttp3.*
import org.json.JSONObject


const val SERVER_IP = "92.63.105.60"
const val SERVER_PORT_HTTP = 7070

val SERVER_PORT_TCP: Int
    get() = SERVER_PORT_HTTP + 1

abstract class AbstractHttpClient {
    
    /**Returns url builder, filled with http, server_ip, server_port*/
    val urlBuilder: HttpUrl.Builder
        get() = HttpUrl.Builder()
                .scheme("http")
                .host(SERVER_IP)
                .port(SERVER_PORT_HTTP)
    
    abstract fun httpGetRequest(url: HttpUrl, httpListener: HttpListener?)
    
    
    /**
     * Перегрузка метода post запроса, для случая когда нужен только один параметр http - pathSegment
     * @param action параметр pathSegment для url
     */
    abstract fun httpPostRequest(action: String, json: JSONObject, httpListener: HttpListener?)
    
    abstract fun httpPostRequest(url: HttpUrl, json: JSONObject?, httpListener: HttpListener?)
    
    
    /** Extension function for interface,
     * returns new [HttpListener] implementation, wrapped in ui thread with [Handler].*/
    private fun HttpListener.inUiThread(): HttpListener {
        val handler = Handler()
        
        return object : HttpListener {
            override fun onResponse(message: JSONObject) {
                handler.post {
                    this@inUiThread.onResponse(message)
                }
            }
            
            override fun onFailure(error: String, title: String) {
                handler.post {
                    this@inUiThread.onFailure(error, title)
                }
            }
        }
    }
    
    /** [AbstractHttpClient] instance, which returns http result in ui thread.*/
    val inUiThread
        get() = object : AbstractHttpClient() {
            override fun httpGetRequest(url: HttpUrl, httpListener: HttpListener?) {
                this@AbstractHttpClient.httpGetRequest(url, httpListener?.inUiThread())
            }
            
            override fun httpPostRequest(action: String, json: JSONObject, httpListener: HttpListener?) {
                this@AbstractHttpClient.httpPostRequest(action, json, httpListener?.inUiThread())
            }
            
            override fun httpPostRequest(url: HttpUrl, json: JSONObject?, httpListener: HttpListener?) {
                this@AbstractHttpClient.httpPostRequest(url, json, httpListener?.inUiThread())
            }
        }
}


abstract class AbstractTcpClient {
    
    
    abstract var isTcpRunning: Boolean
        protected set
    
    protected val messageListeners = mutableListOf<TcpMessageListener>()
    
    
    /**
     * Подключиться к серверу.
     *
     * @param tcpConnectionListener Интерфейс для получения состояния о подключении
     */
    abstract fun startAsync(tcpConnectionListener: TcpConnectionListener)
    
    /**
     * Отключиться от сервера, если подключён.
     *
     * @param connectionListener Слушатель подключения для повторного старта.
     */
    abstract fun reconnect(connectionListener: TcpConnectionListener)
    
    /**
     * Отправить сообщение на сервер.
     *
     * @param message JSON объект, отправляемый клиентом.
     */
    abstract fun sendMessage(message: JSONObject)
    
    /**
     * Отключает от сервера. Закрывает все in/out потоки,
     * ждёт пока поток слушания сообщения завершится, если он не завершён.
     */
    abstract fun stopClient()
    
    /** Add one more [TcpMessageListener]*/
    fun addMessageListener(messageListener: TcpMessageListener) {
        messageListeners.add(messageListener)
        
    }
    
    /** Clear all [messageListeners]*/
    fun clearAllMessageListeners() {
        messageListeners.clear()
    }
    
    
    /** Extension function for interface,
     * returns new [TcpConnectionListener] implementation, wrapped in ui thread with [Handler].*/
    private fun TcpConnectionListener.inUiThread(): TcpConnectionListener {
        val handler = Handler()
        
        return object : TcpConnectionListener {
            override fun onConnected() {
                handler.post {
                    this@inUiThread.onConnected()
                }
            }
            
            override fun onConnectionError(error: String, title: String) {
                handler.post {
                    this@inUiThread.onConnectionError(error, title)
                }
            }
        }
    }
    
    /** Extension function for interface,
     * returns new [TcpMessageListenerp] implementation, wrapped in ui thread with [Handler].*/
    private fun TcpMessageListener.inUiThread(): TcpMessageListener {
        val handler = Handler()
        
        return object : TcpMessageListener {
            override fun onTCPMessageReceived(message: JSONObject) {
                handler.post {
                    this@inUiThread.onTCPMessageReceived(message)
                }
            }
        }
    }
    
    /** [AbstractTcpClient] instance, which returns tcp connection result in ui thread.*/
    protected fun inUiThread() = object : AbstractTcpClient() {
        override var isTcpRunning: Boolean
            get() = this@AbstractTcpClient.isTcpRunning
            set(value) {
                this@AbstractTcpClient.isTcpRunning = value
            }
        
        override fun startAsync(tcpConnectionListener: TcpConnectionListener) {
            this@AbstractTcpClient.startAsync(tcpConnectionListener.inUiThread())
        }
        
        override fun reconnect(connectionListener: TcpConnectionListener) {
            this@AbstractTcpClient.reconnect(connectionListener.inUiThread())
        }
        
        override fun sendMessage(message: JSONObject) {
            this@AbstractTcpClient.sendMessage(message)
        }
        
        override fun stopClient() {
            this@AbstractTcpClient.stopClient()
        }
        
    }
}

    


