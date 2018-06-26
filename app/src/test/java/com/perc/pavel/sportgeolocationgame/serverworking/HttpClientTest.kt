package com.perc.pavel.sportgeolocationgame.serverworking

import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*

class HttpClientTest {
    
    @Test
    fun httpGetRequest() {
        
    
    }
    
    @Test
    fun testAuthorization() {
    
        val httpClient = HttpClient()
    
        
        val json = JSONObject()
        json.put("login", "pavel")
        json.put("password", "1234")
        
        
        httpClient.httpPostRequest("authorization", json, object : HttpListener {
            override fun onResponse(message: JSONObject) {
                print("returned message:\n$message")
            }
    
            override fun onFailure(error: String, title: String) {
                print("Failure:\n\ttitle = $title\n\t error = $error")
            }
    
        })
        
    }
    
    @Test
    fun httpPostRequest1() {
    }
}