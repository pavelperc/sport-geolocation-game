package com.perc.pavel.sportgeolocationgame.serverworking

import com.perc.pavel.sportgeolocationgame.commonLog
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HttpClient : AbstractHttpClient() {
    
    companion object {
        private val client = OkHttpClient()
    }
    
    override fun httpGetRequest(url: HttpUrl, httpListener: HttpListener?) {
        httpPostRequest(url, null, httpListener)
    }
    
    override fun httpPostRequest(action: String, json: JSONObject, httpListener: HttpListener?) {
        val url = urlBuilder.addPathSegment(action).build()
        httpPostRequest(url, json, httpListener)
    }
    
    override fun httpPostRequest(url: HttpUrl, json: JSONObject?, httpListener: HttpListener?) {
        val mediaTypeJSON = MediaType.parse("application/json; charset=utf-8")
        
        val requestBuilder = Request.Builder().url(url)
        
        if (json != null)
            requestBuilder.post(RequestBody.create(mediaTypeJSON, json.toString()))
        
        
        // send request and wrap default Callback with ours.
        client.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                httpListener?.onFailure(e.message!!, "Http request failure")
            }
            
            override fun onResponse(call: Call, response: Response) {
                if (httpListener == null)
                    return
                
                try {
                    val str = response.body()!!.string()
                    response.close()
    
                    commonLog("Http received: $str")
                    
                    val jo = JSONObject(str)
                    
                    httpListener.onResponse(jo)
                    
                } catch (e: IOException) {
                    httpListener.onFailure(response.toString(), "Unexpected http code")
                } catch (e: JSONException) {
                    httpListener.onFailure(e.message!!, "JSONException")
                } catch (e: Exception) {
                    httpListener.onFailure(e.message!!, "Another http exception")
                }
            }
        })
    }
}