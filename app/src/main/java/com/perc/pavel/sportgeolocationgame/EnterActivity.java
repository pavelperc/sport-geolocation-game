package com.perc.pavel.sportgeolocationgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        
        Toast.makeText(this, "login = " + login + "\npassword = " + password, Toast.LENGTH_SHORT).show();
        Log.d("my_tag", "login = " + login + "\tpassword = " + password);

    }
    public void btnRegisterClick(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    
}
