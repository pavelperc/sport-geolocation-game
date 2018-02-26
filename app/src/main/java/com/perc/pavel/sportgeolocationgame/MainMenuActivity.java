package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainMenuActivity extends AppCompatActivity {
    
    TextView profileName;
    Button btnPlayGame;
    Button btnGameAuthors;
    Button btnGameRules;
    Button btnSupportAuthors;
    Profile profile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        profile = (Profile) getIntent().getSerializableExtra("profile");


        profileName = (TextView) findViewById(R.id.profileName);
        profileName.setText(profile.getName());

        btnPlayGame = (Button) findViewById(R.id.btnPlayGame);
        btnGameRules = (Button) findViewById(R.id.btnGameRules);
        btnGameAuthors = (Button) findViewById(R.id.btnGameAuthors);
        btnSupportAuthors = (Button) findViewById(R.id.btnSupportAuthors);

        Typeface Matiz = Typeface.createFromAsset(getAssets(), "fonts/Matiz.ttf");
        profileName.setTypeface(Matiz);
        
        Typeface PhosphateSolid = Typeface.createFromAsset(getAssets(), "fonts/PhosphateSolid.ttf");
        btnPlayGame.setTypeface(PhosphateSolid);
        btnGameRules.setTypeface(PhosphateSolid);
        btnGameAuthors.setTypeface(PhosphateSolid);
        btnSupportAuthors.setTypeface(PhosphateSolid);
        
    }


    public void btnPlayGameClick(View v) {
        Intent intent = new Intent(MainMenuActivity.this, StartGameActivity.class);
        intent.putExtra("profile", (Serializable)profile);
        
        startActivity(intent);
    }
    
    public void btnGameRulesClick(View v) {
        Intent intent = new Intent(MainMenuActivity.this, RulesActivity.class);
        startActivity(intent);
    }
}
