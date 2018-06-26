package com.perc.pavel.sportgeolocationgame.serverworking

import org.json.JSONObject

/** Interface for receiving callbacks from tcp server*/
interface TcpConnectionListener {
    fun onConnected()
    fun onConnectionError(error: String, title: String = "")
}

interface TcpMessageListener {
    /**
     * Is invoked when the server responses
     * @param message Server response
     */
    fun onTCPMessageReceived(message: JSONObject)
}
