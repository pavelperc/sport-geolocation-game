package com.perc.pavel.sportgeolocationgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


/** Регистрация*/
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
        
        client = new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                try {
                    JSONObject received = new JSONObject(message);
                    
                    int response = received.getInt("response");
                    String error = received.getString("error");
                    
                    Log.d("my_tag", "response = " + response + "\terror = " + error);
                    Toast.makeText(LogInActivity.this, "response = " + response + "\nerror = " + error, Toast.LENGTH_SHORT).show();
                    
                } catch (JSONException e) {
                    Log.d("my_tag", "error3:\t" + e);
                }
            }
        });
        
        client.runAsync();
        client.sendMessage(send.toString());
    }

    @Override
    protected void onDestroy() {
        if (client != null)
            client.stopClient();
        super.onDestroy();
    }
}
