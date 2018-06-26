package com.perc.pavel.sportgeolocationgame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast


import kotlinx.android.synthetic.main.activity_register.*


import com.perc.pavel.sportgeolocationgame.serverworking.HttpClient
import com.perc.pavel.sportgeolocationgame.serverworking.HttpListener
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject

/**
 * Регистрация
 */
class RegisterActivity : AppCompatActivity() {
    
    
    private val httpClient = HttpClient().inUiThread
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        
        val Matiz = Typeface.createFromAsset(assets, "fonts/Matiz.ttf")
        etName!!.typeface = Matiz
        etLogin!!.typeface = Matiz
        etPassword!!.typeface = Matiz
        
        val PhosphateSolid = Typeface.createFromAsset(assets, "fonts/PhosphateSolid.ttf")
        btnRegister!!.typeface = PhosphateSolid
        
        val animLogin = findViewById(R.id.animLogin) as TextInputLayout
        animLogin.isHintEnabled = false
        
        val animPassword = findViewById(R.id.animPassword) as TextInputLayout
        animPassword.isHintEnabled = false
        
        val animName = findViewById(R.id.animName) as TextInputLayout
        animName.isHintEnabled = false
        
        etPassword!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                btnRegisterClick(null)
                return@OnEditorActionListener true
            }
            false
        })
    }
    
    
    fun btnRegisterClick(v: View?) {
        val login = etLogin!!.text.toString()
        val password = etPassword!!.text.toString()
        val name = etName!!.text.toString()
        
        if (login == "" || password == "" || name == "") {
            toast("Все поля должны быть заполнены.")
            return
        }
        
        val json = JSONObject()
        json.put("login", login)
        json.put("password", password)
        json.put("name", name)
        
        
        pbLoading.visibility = View.VISIBLE
        
        httpClient.httpPostRequest("registration", json, object : HttpListener {
            override fun onResponse(message: JSONObject) {
                pbLoading.visibility = View.GONE
                try {
                    if (message.getBoolean("status")) {// если регистрация успешна
                        // Сохранение последних данных
                        val pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                        val editor = pref.edit()
                        editor.putString("login", login)
                        editor.putString("password", password)
                        editor.putString("name", name)
                        editor.apply()
                        
                        val intent = Intent()
                        setResult(Activity.RESULT_OK, intent)
                        toast("Регистрация успешна.")
                        this@RegisterActivity.finish()
                    } else {
                        onFailure(message.getString("error"), "Ошибка регистрации:\n")
                    }
                } catch (e: JSONException) {
                    onFailure(e.message!!, "JSONException")
                }
                
            }
            
            override fun onFailure(error: String, title: String) {
                pbLoading!!.visibility = View.GONE
                alert(error, title).show()
//                Toast.makeText(this@RegisterActivity, "Ошибка http запроса:\n$error", Toast.LENGTH_SHORT).show()
            }
            
        })
    }
}
