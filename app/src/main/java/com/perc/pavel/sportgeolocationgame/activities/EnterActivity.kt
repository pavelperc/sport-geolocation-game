package com.perc.pavel.sportgeolocationgame.activities

import android.Manifest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.android.gms.common.ConnectionResult

import com.google.android.gms.common.GoogleApiAvailability
import com.perc.pavel.sportgeolocationgame.Profile
import com.perc.pavel.sportgeolocationgame.R
import com.perc.pavel.sportgeolocationgame.RegisterActivity
import com.perc.pavel.sportgeolocationgame.serverworking.AbstractHttpClient
import com.perc.pavel.sportgeolocationgame.serverworking.HttpClient
import com.perc.pavel.sportgeolocationgame.serverworking.HttpClientFake
import com.perc.pavel.sportgeolocationgame.serverworking.HttpListener
import kotlinx.android.synthetic.main.activity_enter.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import org.json.JSONException

import org.json.JSONObject
import java.util.ArrayList

/**
 * Вход
 */
class EnterActivity : AppCompatActivity() {
    
    companion object {
        private const val MY_PERMISSIONS_REQUEST = 1
        private const val REGISTRATION_REQUEST = 2
    }
    
//    private val httpClient: AbstractHttpClient = HttpClient().inUiThread
    private val httpClient: AbstractHttpClient = HttpClientFake()
    
    
    private fun setupFonts() {
        val Matiz = Typeface.createFromAsset(assets, "fonts/Matiz.ttf")
        etLogin.typeface = Matiz
        etPassword.typeface = Matiz
    
        val PhosphateInline = Typeface.createFromAsset(assets, "fonts/PhosphateInline.ttf")
        btnForgot.typeface = PhosphateInline
        btnRegister.typeface = PhosphateInline
    
        val PhosphateSolid = Typeface.createFromAsset(assets, "fonts/PhosphateSolid.ttf")
        btnEnter.typeface = PhosphateSolid
    }
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)
        
        
        setupFonts()
        
        
        animLogin.isHintEnabled = false
        animPassword.isHintEnabled = false
        
        etPassword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                btnEnterClick(null)
                true
            } else
                false
        }
        
        askPermissions()
        checkGoogleServices()
        restoreLastLoginData()
    }
    
    /** If we have saved login data - restore it from SharedPreferences.*/
    private fun restoreLastLoginData() {
        
        val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        
        etLogin.setText(pref.getString("login", ""))
        etPassword.setText(pref.getString("password", ""))
        
        chbFakeGps.isChecked = pref.getBoolean("fakeGps", false)
        etStartGps.setText(pref.getString("startGps", ""))
    }
    
    /**
     * Check available google services on the device. (For gps)
     */
    private fun checkGoogleServices() {
        val instance = GoogleApiAvailability.getInstance()
        val res = instance.isGooglePlayServicesAvailable(this)
        
        if (res != ConnectionResult.SUCCESS) {
            instance.getErrorDialog(this, res, 0).create()
        }
    }
    
    /** Request permission for geolocation if it hasn't been received yet and android version > 6.0 */
    private fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST)
        }
    }
    
    /** Handle the result of geolocation permission request */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    
                    
                    // create dialog window with access error and finish activity
                    alert {
                        message = "Приложение будет остановлено."
                        title = "Ошибка доступа к геолокации."
                        okButton { this@EnterActivity.finish() }
                    }.show()
                }
            }
        }
    }
    
    fun btnEnterClick(v: View?) {
        val login = etLogin.text.toString()
        val password = etPassword.text.toString()
        
        if (login == "" || password == "") {
            toast("Все поля должны быть заполнены.")
            return
        }
        
        pbLoading.visibility = View.VISIBLE
        
        
        val json = JSONObject()
        json.put("login", login)
        json.put("password", password)
        
        
        httpClient.httpPostRequest("authorization", json, object : HttpListener {
            override fun onResponse(message: JSONObject) {
                pbLoading.visibility = View.GONE
                try {
                    if (!message.getBoolean("status")) {
                        onFailure(message.getString("error"), "Ошибка входа")
                    }
                    toast("Вход успешен.")
                    
                    val name = message.getString("name")
                    
                    // Сохранение последних данных
                    val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                    val editor = pref.edit()
                    editor.putString("login", login)
                    editor.putString("password", password)
                    editor.putString("name", name)
                    
                    editor.putBoolean("fakeGps", chbFakeGps.isChecked)
                    editor.putString("startGps", etStartGps.text.toString())
                    editor.apply()
                    
                    val intent = Intent(this@EnterActivity, MainMenuActivity::class.java)
                    
                    val profile = Profile(name, login, password)
                    intent.putExtra("profile", profile)
                    
                    startActivity(intent)
                } catch (e: JSONException) {
                    onFailure(e.message!!, "JSONException")
                }
                
            }
            
            override fun onFailure(error: String, title: String) {
                pbLoading.visibility = View.GONE
                alert(error, title).show()
//                Toast.makeText(this@EnterActivity, "Ошибка http запроса:\n$error", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REGISTRATION_REQUEST && resultCode == Activity.RESULT_OK) {
            restoreLastLoginData()
        }
    }
    
    fun btnRegisterClick(v: View) {
        startActivityForResult(Intent(this, RegisterActivity::class.java), REGISTRATION_REQUEST)
    }
    
    fun btnVkRegisterClick(v: View) {
        //        Intent intent = new Intent(EnterActivity.this, StartGameActivity.class);
        //        intent.putExtra("profile", new Profile("name", "login", "password"));
        //        startActivity(intent);
        
        
        // just saving fakeGps setting
        getSharedPreferences("Settings", Context.MODE_PRIVATE).edit().apply {
            putBoolean("fakeGps", chbFakeGps.isChecked)
            putString("startGps", etStartGps.text.toString())
        }.apply()
        
        
        val teamColors = ArrayList<Int>()
        teamColors.add(Color.parseColor("#972EFF"))
        teamColors.add(Color.parseColor("#FFF00D"))
        
        val intent = Intent(this@EnterActivity, GoogleMapsActivity::class.java)
        intent.putExtra("profile", Profile("my_name", "my_login", "123"))
        intent.putExtra("teamColors", teamColors)
        //                        intent.putExtra("myTeamColor", teamColors.get(0));
        
        intent.putExtra("roomId", 123)
        intent.putExtra("createGame", true)
        startActivity(intent)
    }
}
