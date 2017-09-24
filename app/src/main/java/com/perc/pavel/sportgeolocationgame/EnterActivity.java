package com.perc.pavel.sportgeolocationgame;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Вход
 */
public class EnterActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    EditText etLogin;
    EditText etPassword;
    ProgressBar pbLoading;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        // запрашиваем разрешение на местоположение
        askPermissions();
        checkGoogleServices();
    }

    /**
     * Проверка наличия актуальных сервисов гугл на устройстве. (Для gps)
     */
    private void checkGoogleServices() {
        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        int res = instance.isGooglePlayServicesAvailable(this);

        if (res != ConnectionResult.SUCCESS) {
            instance.getErrorDialog(this, res, 0).create();
        }
    }

    /**
     * Запрос разрешения на использование геолокации, если оно ещё не получено и если версия андроида > 6.0 
     */
    protected void askPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }
    }

    /**
     * Обработка ответа на запрос разрешения на использование геолокации.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    
                    // Создаём диалоговое окно об ошибке доступа.
                    new AlertDialog.Builder(this)
                            .setTitle("Ошибка доступа к геолокации.")
                            .setMessage("Приложение будет остановлено.")
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EnterActivity.this.finish();
                                }
                            }).create().show();
                }
            }
        }
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


        pbLoading.setVisibility(View.VISIBLE);
        new TcpClientFake().httpRequest(send, this, new HttpListener() {
            @Override
            public void onMessageReceived(JSONObject message) {
                pbLoading.setVisibility(View.GONE);
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