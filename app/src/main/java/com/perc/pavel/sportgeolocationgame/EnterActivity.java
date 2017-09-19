package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Вход
 */
public class EnterActivity extends AppCompatActivity {

    EditText etLogin;
    EditText etPassword;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }
    
    
    public void btnEnterClick(View v) {
        String login = etLogin.getText().toString();
        String password = etPassword.getText().toString();
        
//        Toast.makeText(this, "login = " + login + "\npassword = " + password, Toast.LENGTH_SHORT).show();
//        Log.d("my_tag", "login = " + login + "\tpassword = " + password);
        
        
        JSONObject send = new JSONObject();
        try {
            send.put("type", "authorization");
            send.put("login", login);
            send.put("password", password);
        } catch (JSONException ignored){}


        new TcpClientFake().httpRequest(send, this, new HttpListener() {
            @Override
            public void onMessageReceived(JSONObject message) {
                try {
                    if (message.getInt("response") > 0) {
                        Toast.makeText(EnterActivity.this, "Вход успешен.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EnterActivity.this, MapsActivity.class));
                    } else {
                        Toast.makeText(EnterActivity.this, "Ошибка входа.\nError: " + message.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ignored){}
            }
        });
    }
    
    
    public void btnRegisterClick(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
    
    public void btnVkRegisterClick(View v) {
        
    }
}
