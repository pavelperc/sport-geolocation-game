package com.perc.pavel.sportgeolocationgame.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.perc.pavel.sportgeolocationgame.Profile
import com.perc.pavel.sportgeolocationgame.R
import com.perc.pavel.sportgeolocationgame.RulesActivity
import kotlinx.android.synthetic.main.activity_main_menu.*

import java.io.Serializable

class MainMenuActivity : AppCompatActivity() {
    
    private lateinit var profile: Profile
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        profile = intent.getSerializableExtra("profile") as Profile
        
        profileName.text = profile.name
        
        val Matiz = Typeface.createFromAsset(assets, "fonts/Matiz.ttf")
        profileName.typeface = Matiz
        
        val PhosphateSolid = Typeface.createFromAsset(assets, "fonts/PhosphateSolid.ttf")
        btnPlayGame.typeface = PhosphateSolid
        btnGameRules.typeface = PhosphateSolid
        btnGameAuthors.typeface = PhosphateSolid
        btnSupportAuthors.typeface = PhosphateSolid
        
    }
    
    
    fun btnPlayGameClick(v: View) {
        val intent = Intent(this, StartGameActivity::class.java)
        intent.putExtra("profile", profile as Serializable)
        
        startActivity(intent)
    }
    
    fun btnGameRulesClick(v: View) {
        val intent = Intent(this, RulesActivity::class.java)
        startActivity(intent)
    }
}
