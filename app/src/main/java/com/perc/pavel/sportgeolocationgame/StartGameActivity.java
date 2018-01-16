package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

public class StartGameActivity extends AppCompatActivity {
    
    String login;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        login = getIntent().getStringExtra("login");
        
    }
    
    public void btnCreateGameClick(View v) {
        JSONObject send = new JSONObject();
    
    
        Intent intent = new Intent(StartGameActivity.this, GoogleMapsActivity.class);
        intent.putExtra("login", login);
        intent.putExtra("roomId", 123);
        intent.putExtra("createGame", true);
        startActivity(intent);
    }
    
    public void btnJoinGameClick(View v) {
        EditText etRoomId = (EditText) findViewById(R.id.etRoomId);
    
        Intent intent = new Intent(StartGameActivity.this, GoogleMapsActivity.class);
        intent.putExtra("login", login);
        intent.putExtra("roomId", Integer.parseInt(etRoomId.getText().toString()));
        intent.putExtra("createGame", false);
    
        startActivity(intent);
    }
}
