package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.HttpUrl;

public class StartGameActivity extends AppCompatActivity {
    
    Profile profile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        profile = (Profile) getIntent().getSerializableExtra("profile");
    }
    
    public void btnCreateGameClick(View v) {
        
        HttpUrl url = TcpClient.getUrlBuilder()
                .addPathSegment("")
                .build();
        
        
        ArrayList<Integer> teamColors = new ArrayList<>();
        teamColors.add(Color.parseColor("#972EFF"));
        teamColors.add(Color.parseColor("#FFF00D"));
    
        Intent intent = new Intent(StartGameActivity.this, GoogleMapsActivity.class);
        intent.putExtra("profile", profile);
        intent.putExtra("teamColors", teamColors);
        intent.putExtra("myTeamColor", teamColors.get(0));
        
        intent.putExtra("roomId", 123);
        intent.putExtra("createGame", true);
        startActivity(intent);
    }
    
    public void btnJoinGameClick(View v) {
        EditText etRoomId = (EditText) findViewById(R.id.etRoomId);
        
        ArrayList<Integer> teamColors = new ArrayList<>();
        teamColors.add(Color.parseColor("#972EFF"));
        teamColors.add(Color.parseColor("#FFF00D"));
        
        Intent intent = new Intent(StartGameActivity.this, GoogleMapsActivity.class);
        intent.putExtra("profile", profile);
        intent.putExtra("teamColors", teamColors);
        intent.putExtra("myTeamColor", teamColors.get(0));
        intent.putExtra("roomId", Integer.parseInt(etRoomId.getText().toString()));
        intent.putExtra("createGame", false);
    
        startActivity(intent);
    }
}
