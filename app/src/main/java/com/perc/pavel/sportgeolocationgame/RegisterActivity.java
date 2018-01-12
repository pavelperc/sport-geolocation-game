package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Регистрация
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin;
    private EditText etPassword;
    private EditText etName;
    private ProgressBar pbLoading;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etName = (EditText) findViewById(R.id.etName);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        
                
//        client.startAsync(new TcpListener() {
//            @Override
//            public void onMessageReceived(String message) {
//                Toast.makeText(RegisterActivity.this, "Server answer:\n" + message, Toast.LENGTH_SHORT).show();
//                Log.d("my_tag", "Server answered: " + message);
//            }
//
//            @Override
//            public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
//                Log.d("my_tag", "Server is connected: " + isConnectedNow);
//            }
//        });
        
    }
    

    public void btnRegisterClick(View v) {
        final String login = etLogin.getText().toString();
        final String password = etPassword.getText().toString();
        final String name = etName.getText().toString();

        if (login.equals("") || password.equals("") || name.equals("")) {
            Toast.makeText(this, "Все поля должны быть заполнены.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        //Toast.makeText(this, "name = " + name + "\nlogin = " + login + "\npassword = " + password , Toast.LENGTH_SHORT).show();
        //Log.d("my_tag",  "name = " + name + "\tlogin = " + login + "\tpassword = " + password);
        
        JSONObject send = new JSONObject();
        try {
            send.put("type", "register");
            send.put("login", login);
            send.put("password", password);
            send.put("name", name);
        } catch (JSONException ignored){}
        
        
        pbLoading.setVisibility(View.VISIBLE);
        TcpClientFake.getInstance().httpRequest(send, new HttpListener() {
            @Override
            public void onMessageReceived(JSONObject message) {
                pbLoading.setVisibility(View.GONE);
                try {
                    if (message.getInt("response") > 0) {
                        // Сохранение последних данных НЕ РАБОТАЕТ!!!!!!!
                        // решено: надо использовать getSharedPreferences вместо getPreferences
                        SharedPreferences pref = getSharedPreferences("Settings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("login", login);
                        editor.putString("password", password);
                        editor.apply();
//                        //boolean res = editor.commit();// ASYNC
//                        
//                        pref = getPreferences(MODE_PRIVATE);
//                        
//                        //Toast.makeText(RegisterActivity.this, "saved = " + res, Toast.LENGTH_SHORT).show();
//                        Toast.makeText(RegisterActivity.this, "saved1 login = " + pref.getString("login", "-"), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("login", login);
                        intent.putExtra("password", password);
                        intent.putExtra("name", name);
                        
                        
                        setResult(RESULT_OK, intent);
                        Toast.makeText(RegisterActivity.this, "Регистрация успешна.", Toast.LENGTH_SHORT).show();
                        RegisterActivity.this.finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации.\nError: " + message.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ignored){}
            }
        });
    }

    @Override
    protected void onDestroy() {
//        if (client != null)
//            client.stopClient();
        super.onDestroy();
    }
}
