package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.HttpUrl;

/**
 * Регистрация
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin;
    private EditText etPassword;
    private EditText etName;
    //private ProgressBar pbLoading;
    private Button btnRegister;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etName = (EditText) findViewById(R.id.etName);
        //pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        Typeface Matiz = Typeface.createFromAsset(getAssets(), "fonts/Matiz.ttf");
        etName.setTypeface(Matiz);
        etLogin.setTypeface(Matiz);
        etPassword.setTypeface(Matiz);

        Typeface PhosphateSolid = Typeface.createFromAsset(getAssets(), "fonts/PhosphateSolid.ttf");
        btnRegister.setTypeface(PhosphateSolid);

        TextInputLayout animLogin = (TextInputLayout)findViewById(R.id.animLogin);
        animLogin.setHintEnabled(false);

        TextInputLayout animPassword = (TextInputLayout)findViewById(R.id.animPassword);
        animPassword.setHintEnabled(false);

        TextInputLayout animName = (TextInputLayout)findViewById(R.id.animName);
        animName.setHintEnabled(false);
    
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

        Toast.makeText(this, login + password + name, Toast.LENGTH_SHORT).show();


        JSONObject send = new JSONObject();
        try {
            send.put("type", "register");
            send.put("login", login);
            send.put("password", password);
            send.put("name", name);
        } catch (JSONException ignored){}



        JSONObject json = new JSONObject();
        try {
            json.put("login", login);
            json.put("password", password);
            json.put("name", name);
        } catch (JSONException ignored) {}
        
        TcpClient.getInstance().httpPostRequest("registration", json, new HttpListener() {
            @Override
            public void onResponse(JSONObject message) {
                //pbLoading.setVisibility(View.GONE);
                try {
                    if (message.getBoolean("status")) {// если регистрация успешна
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
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации:\n" + message.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ignored){}
            }
        
            @Override
            public void onFailure(String error) {
                //pbLoading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Ошибка http запроса:\n" + error, Toast.LENGTH_SHORT).show();
            }

        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
