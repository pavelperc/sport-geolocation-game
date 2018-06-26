package com.perc.pavel.sportgeolocationgame.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.perc.pavel.sportgeolocationgame.Profile
import com.perc.pavel.sportgeolocationgame.R
import com.perc.pavel.sportgeolocationgame.serverworking.AbstractHttpClient
import com.perc.pavel.sportgeolocationgame.serverworking.HttpClient
import com.perc.pavel.sportgeolocationgame.serverworking.HttpClientFake

import com.perc.pavel.sportgeolocationgame.serverworking.HttpListener
import kotlinx.android.synthetic.main.activity_start_game.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.toast

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

import java.util.ArrayList


class StartGameActivity : AppCompatActivity() {
    
    internal lateinit var profile: Profile
    internal var location: Location? = null
    
    //    private val httpClient: AbstractHttpClient = HttpClient().inUiThread
    private val httpClient: AbstractHttpClient = HttpClientFake()
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_game)
        profile = intent.getSerializableExtra("profile") as Profile
        
        profileName.text = profile.name
        
        
        val Matiz = Typeface.createFromAsset(assets, "fonts/Matiz.ttf")
        etRoomId.typeface = Matiz
        tvTeams.typeface = Matiz
        tvTeamsNumber.typeface = Matiz
        profileName.typeface = Matiz
        
        val PhosphateSolid = Typeface.createFromAsset(assets, "fonts/PhosphateSolid.ttf")
        btnJoinGame.typeface = PhosphateSolid
        btnCreateGame.typeface = PhosphateSolid
        
        
        
        
        sbTeamsNumber.onSeekBarChangeListener {
            onProgressChanged { seekBar, progress, fromUser ->
                tvTeamsNumber.text = (progress + 1).toString()
            }
        }

//        sbTeamsNumber.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                tvTeamsNumber.text = (progress + 1).toString()
//            }
//            
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//                
//            }
//            
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                
//            }
//        })
        
    }
    
    fun btnCreateGameClick(v: View) {
        val teamColorsFull = listOf(
                Color.parseColor("#972eff"),// фиолет
                Color.parseColor("#faed00"),// желтый
                Color.parseColor("#00a8f3"),// голуб
                Color.parseColor("#ff7f27"),// оранж
                Color.parseColor("#0ed145"),// зелен
                Color.parseColor("#ffaec8"),// розов
                Color.parseColor("#d81118"),// красн
                Color.parseColor("#3f48cc")// синий
        ).shuffled()
        
        val teamColors = teamColorsFull.take(sbTeamsNumber.progress + 1)
        
        
        pbLoading.visibility = View.VISIBLE
        
        // отправка запроса на создание комнаты
        
        val json = JSONObject()
        
        json.put("login", profile.login)
        val ja = JSONArray()
        
        teamColors.forEach { ja.put(it) }
        
        json.put("colors", ja)
        
        
        httpClient.httpPostRequest("create_room", json, object : HttpListener {
            override fun onResponse(message: JSONObject) {
                pbLoading.visibility = View.GONE
                try {
                    if (message.has("error")) {
                        onFailure(message.getString("error"), "Server returned error")
                        return
                    }
                    
                    val intent = Intent(this@StartGameActivity, GoogleMapsActivity::class.java)
                    intent.putExtra("profile", profile)
                    intent.putExtra("teamColors", teamColors as Serializable)
//                        intent.putExtra("myTeamColor", teamColors.get(0));
                    
                    intent.putExtra("roomId", message.getInt("room_id"))
                    intent.putExtra("createGame", true)
                    startActivity(intent)
                    
                } catch (e: JSONException) {
                    onFailure(e.message!!, "JSONException")
//                    Toast.makeText(this@StartGameActivity, "JSONException: $e", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(error: String, title: String) {
                pbLoading.visibility = View.GONE
                alert(error, title).show()
//                Toast.makeText(this@StartGameActivity, "Failure: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    fun btnJoinGameClick(v: View) {
        if (etRoomId.text.isEmpty()) {
            toast("Номер комнаты не введён.")
            return
        }
        val roomId = etRoomId.text.toString().toInt()
        
        pbLoading.visibility = View.VISIBLE
        
        // отправка запроса на подключение к комнате
        
        val json = JSONObject()
        json.put("login", profile.login)
        json.put("room_id", roomId)
        
        
        httpClient.httpPostRequest("connect_to_room", json, object : HttpListener {
            override fun onResponse(message: JSONObject) {
                pbLoading.visibility = View.GONE
                
                try {
                    if (message.has("error")) {
                        onFailure(message.getString("error"), "Error from server")
                        return
                    }
                    
                    val teamColors = ArrayList<Int>()
                    val ja = message.getJSONArray("colors")
                    for (i in 0 until ja.length()) {
                        teamColors.add(ja.getInt(i))
                    }
                    
                    val intent = Intent(this@StartGameActivity, GoogleMapsActivity::class.java)
                    intent.putExtra("profile", profile)
                    intent.putExtra("teamColors", teamColors)
                    
                    intent.putExtra("roomId", roomId)
                    intent.putExtra("createGame", false)
                    startActivity(intent)
                    
                } catch (e: JSONException) {
                    onFailure(e.message!!, "JSONException")
//                    Toast.makeText(this@StartGameActivity, "JSONException: $e", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(error: String, title: String) {
                pbLoading.visibility = View.GONE
                alert(error, title).show()
//                Toast.makeText(this@StartGameActivity, "Failure: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
