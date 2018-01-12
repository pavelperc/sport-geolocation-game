package com.perc.pavel.sportgeolocationgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

public class StartGameActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
    
        TcpClient.getInstance().clearAllMessageListeners();
        TcpClient.getInstance().addMessageListener(new TcpListener() {
            @Override
            public void onTCPMessageReceived(JSONObject message) {
        
            }
    
            @Override
            public void onConnected() {
        
            }
    
            @Override
            public void onDisconnected() {
        
            }
        });
    }
    
    public void btnCreateGameClick(View v) {
        
    }
    
    public void btnJoinGameClick(View v) {
        EditText etRoomId = (EditText) findViewById(R.id.etRoomId);
        
    }
}
