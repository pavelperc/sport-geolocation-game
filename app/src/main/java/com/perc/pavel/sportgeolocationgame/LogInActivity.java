package com.perc.pavel.sportgeolocationgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Регистрация
 */
public class LogInActivity extends AppCompatActivity {

    private EditText etLogin;
    private EditText etPassword;
    private EditText etName;
    
    private TcpClient client;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        
        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etName = (EditText) findViewById(R.id.etName);
        
        client = new TcpClient();
        
        client.startAsync(new TcpListener() {
            @Override
            public void onTCPMessageReceived(String message) {
                Toast.makeText(LogInActivity.this, "Server answer:\n" + message, Toast.LENGTH_SHORT).show();
                Log.d("my_tag", "Server ansewred: " + message);
            }

            @Override
            public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
                Log.d("my_tag", "Server is connected: " + isConnectedNow);
            }
        });
        
    }
    

    public void btnLogInClick(View v) throws JSONException {
        String login = etLogin.getText().toString();
        String password = etPassword.getText().toString();
        String name = etName.getText().toString();
        
        //Toast.makeText(this, "name = " + name + "\nlogin = " + login + "\npassword = " + password , Toast.LENGTH_SHORT).show();
        Log.d("my_tag",  "name = " + name + "\tlogin = " + login + "\tpassword = " + password);
        
        JSONObject send = new JSONObject();
        send.put("type", "register");
        send.put("login", login);
        send.put("password", password);
        send.put("name", name);
        
        
        client.sendMessage(send);
    }

    @Override
    protected void onDestroy() {
        if (client != null)
            client.stopClient();
        super.onDestroy();
    }
}
