package com.perc.pavel.sportgeolocationgame.serverworking

import okhttp3.HttpUrl
import org.json.JSONObject

class HttpClientFake : AbstractHttpClient() {
    override fun httpGetRequest(url: HttpUrl, httpListener: HttpListener?) {
        httpListener?.onFailure("Get request is not implemented", "HttpFake error")
    }
    
    override fun httpPostRequest(action: String, json: JSONObject, httpListener: HttpListener?) {
        when (action) {
            "authorization" -> {
                val login = json.optString("login", "pavel_123")
                
                val message = JSONObject()
                
                message.put("status", true)
                message.put("name", login[0].toUpperCase() + login.substring(1))
                
                httpListener?.onResponse(message)
            }
            
            "create_room" -> {
                val message = JSONObject()
                
                message.put("room_id", 12345)
                
                httpListener?.onResponse(message)
                
            }
            else -> {
                httpListener?.onFailure("Action $action not implemented", "HttpFake error")
            }
        }
    }
    
    override fun httpPostRequest(url: HttpUrl, json: JSONObject?, httpListener: HttpListener?) {
        httpListener?.onFailure("Post request is not implemented", "HttpFake error")
    }
}