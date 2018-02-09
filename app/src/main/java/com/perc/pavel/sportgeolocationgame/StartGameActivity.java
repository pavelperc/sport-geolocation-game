package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.HttpUrl;

public class StartGameActivity extends AppCompatActivity {
    
    Profile profile;
    Location location;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        profile = (Profile) getIntent().getSerializableExtra("profile");
    }
    
    public void btnCreateGameClick(View v) {
        final ArrayList<Integer> teamColors = new ArrayList<>();
        teamColors.add(Color.parseColor("#972EFF"));
        teamColors.add(Color.parseColor("#FFF00D"));
    
        
        // отправка запроса на создание комнаты
        
        JSONObject json = new JSONObject();
        try {
            json.put("login", profile.getLogin());
            JSONArray ja = new JSONArray();
            
            for (Integer teamColor : teamColors) {
                ja.put(teamColor);
            }
            json.put("colors", ja);
        } catch (JSONException ignored) {}
        
        TcpClient.getInstance().httpPostRequest("create_room", json, new HttpListener() {
            @Override
            public void onResponse(JSONObject message) {
                try {
                    if (message.has("error")) {
                        Toast.makeText(StartGameActivity.this, "Error: " + message.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent intent = new Intent(StartGameActivity.this, GoogleMapsActivity.class);
                        intent.putExtra("profile", profile);
                        intent.putExtra("teamColors", teamColors);
//                        intent.putExtra("myTeamColor", teamColors.get(0));
                        
                        intent.putExtra("roomId", message.getInt("room_id"));
                        intent.putExtra("createGame", true);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(StartGameActivity.this, "JSONException: " + e, Toast.LENGTH_SHORT).show();
                }
            }
    
            @Override
            public void onFailure(String error) {
                Toast.makeText(StartGameActivity.this, "Failure: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    public void btnJoinGameClick(View v) {
        EditText etRoomId = (EditText) findViewById(R.id.etRoomId);
        final int roomId = Integer.parseInt(etRoomId.getText().toString());
        
        // отправка запроса на подключение к комнате
        
        JSONObject json = new JSONObject();
        try {
            json.put("login", profile.getLogin());
            json.put("room_id", roomId);
        } catch (JSONException ignored) {}
        
        TcpClient.getInstance().httpPostRequest("connect_to_room", json, new HttpListener() {
            @Override
            public void onResponse(JSONObject message) {
                try {
                    if (message.has("error")) {
                        Toast.makeText(StartGameActivity.this, "Error: " + message.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        ArrayList<Integer> teamColors = new ArrayList<>();
                        JSONArray ja = message.getJSONArray("colors");
                        for (int i = 0; i < ja.length(); i++) {
                            teamColors.add(ja.getInt(i));
                        }
                        
                        Intent intent = new Intent(StartGameActivity.this, GoogleMapsActivity.class);
                        intent.putExtra("profile", profile);
                        intent.putExtra("teamColors", teamColors);
                                                
                        intent.putExtra("roomId", roomId);
                        intent.putExtra("createGame", false);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(StartGameActivity.this, "JSONException: " + e, Toast.LENGTH_SHORT).show();
                }
            }
        
            @Override
            public void onFailure(String error) {
                Toast.makeText(StartGameActivity.this, "Failure: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
