package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StartGameActivity extends AppCompatActivity {
    
    Profile profile;
    Location location;
    SeekBar sbTeamsNumber;
    TextView tvTeams;
    TextView tvTeamsNumber;
    TextView profileName;
    EditText etRoomId;
    Button btnJoinGame;
    Button btnCreateGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        profile = (Profile) getIntent().getSerializableExtra("profile");
        
        sbTeamsNumber = (SeekBar) findViewById(R.id.sbTeamsNumber);
        tvTeams = (TextView) findViewById(R.id.tvTeams);
        tvTeamsNumber = (TextView) findViewById(R.id.tvTeamsNumber);

        profileName = (TextView) findViewById(R.id.profileName);
        profileName.setText(profile.getName());

        etRoomId = (EditText) findViewById(R.id.etRoomId);
        btnJoinGame = (Button) findViewById(R.id.btnJoinGame);
        btnCreateGame = (Button) findViewById(R.id.btnCreateGame);

        Typeface Matiz = Typeface.createFromAsset(getAssets(), "fonts/Matiz.ttf");
        etRoomId.setTypeface(Matiz);
        tvTeams.setTypeface(Matiz);
        tvTeamsNumber.setTypeface(Matiz);
        profileName.setTypeface(Matiz);

        Typeface PhosphateSolid = Typeface.createFromAsset(getAssets(), "fonts/PhosphateSolid.ttf");
        btnJoinGame.setTypeface(PhosphateSolid);
        btnCreateGame.setTypeface(PhosphateSolid);
        
        sbTeamsNumber.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTeamsNumber.setText(String.valueOf(progress + 1));
            }
    
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
        
            }
    
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
        
            }
        });
        
    }
    
    public void btnCreateGameClick(View v) {
        List<Integer> teamColorsFull = new ArrayList<>();
    
        teamColorsFull.add(Color.parseColor("#972eff"));// фиолет
        teamColorsFull.add(Color.parseColor("#faed00"));// желтый
        teamColorsFull.add(Color.parseColor("#00a8f3"));// голуб
        teamColorsFull.add(Color.parseColor("#ff7f27"));// оранж
        teamColorsFull.add(Color.parseColor("#0ed145"));// зелен
        teamColorsFull.add(Color.parseColor("#ffaec8"));// розов
        teamColorsFull.add(Color.parseColor("#d81118"));// красн
        teamColorsFull.add(Color.parseColor("#3f48cc"));// синий
        
        Collections.shuffle(teamColorsFull);
        
        final ArrayList<Integer> teamColors = new ArrayList<>(teamColorsFull.subList(0, sbTeamsNumber.getProgress() + 1));
        
        
        
        
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
        if (etRoomId.getText().toString().equals("")) {
            Toast.makeText(this, "Номер комнаты не введён.", Toast.LENGTH_SHORT).show();
            return;
        }
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
