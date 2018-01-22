package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.Request;

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
    
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    btnRegisterClick(null);
                    return true;
                }
                return false;
            }
        });
    }
    

    public void btnRegisterClick(View v) {
        final String login = etLogin.getText().toString();
        final String password = etPassword.getText().toString();
        final String name = etName.getText().toString();

        if (login.equals("") || password.equals("") || name.equals("")) {
            Toast.makeText(this, "Все поля должны быть заполнены.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        JSONObject send = new JSONObject();
        try {
            send.put("type", "register");
            send.put("login", login);
            send.put("password", password);
            send.put("name", name);
        } catch (JSONException ignored){}
        
        
        pbLoading.setVisibility(View.VISIBLE);
    
        HttpUrl url = TcpClient.getUrlBuilder()
                .addPathSegment("registration")
                .addQueryParameter("login", login)
                .addQueryParameter("name", name)
                .addQueryParameter("password", password)
                .build();
        TcpClient.getInstance().httpGetRequest(url, new HttpListener() {
            @Override
            public void onResponse(String message) {
                pbLoading.setVisibility(View.GONE);
                try {
                    JSONObject jMessage = new JSONObject(message);
                    if (jMessage.getBoolean("status")) {// если регистрация успешна
                        // Сохранение последних данных
                        SharedPreferences pref = getSharedPreferences("Settings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("login", login);
                        editor.putString("password", password);
                        editor.putString("name", name);
                        editor.apply();
                        
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        Toast.makeText(RegisterActivity.this, "Регистрация успешна.", Toast.LENGTH_SHORT).show();
                        RegisterActivity.this.finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации:\n" + jMessage.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ignored){}
            }
        
            @Override
            public void onFailure(String error) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Ошибка http запроса:\n" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
