package com.perc.pavel.sportgeolocationgame;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.HttpUrl;

/**
 * Вход
 */
public class EnterActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int REGISTRATION_REQUEST = 2;
    EditText etLogin;
    EditText etPassword;
    ProgressBar pbLoading;
    
    CheckBox chbFakeGps;
    EditText etStartGps;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        chbFakeGps = (CheckBox) findViewById(R.id.chbFakeGps);
        etStartGps = (EditText) findViewById(R.id.etStartGps);
    
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    btnEnterClick(null);
                    return true;
                }
                return false;
            }
        });
        askPermissions();
        checkGoogleServices();
        restoreLastLoginData();
    }

    /**
     * Если сохранены последние данные входа - они восстанавливаются из SharedPreferences.
     */
    private void restoreLastLoginData() {
        SharedPreferences pref = getSharedPreferences("Settings", MODE_PRIVATE);
        etLogin.setText(pref.getString("login", ""));
        etPassword.setText(pref.getString("password", ""));
        
        chbFakeGps.setChecked(pref.getBoolean("fakeGps", false));
        etStartGps.setText(pref.getString("startGps", ""));
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
                            .setCancelable(false)
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
        final String login = etLogin.getText().toString();
        final String password = etPassword.getText().toString();
        
        if (login.equals("") || password.equals("")) {
            Toast.makeText(this, "Все поля должны быть заполнены.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        pbLoading.setVisibility(View.VISIBLE);
    
        
        JSONObject json = new JSONObject();
        try {
            json.put("login", login);
            json.put("password", password);
        } catch (JSONException ignored) {}
        
        TcpClient.getInstance().httpPostRequest("authorization", json, new HttpListener() {
            @Override
            public void onResponse(JSONObject message) {
                pbLoading.setVisibility(View.GONE);
                try {
                    if (message.getBoolean("status")) {// если вход успешен
                        Toast.makeText(EnterActivity.this, "Вход успешен.", Toast.LENGTH_SHORT).show();
                        
                        String name = message.getString("name");
                        
                        // Сохранение последних данных
                        SharedPreferences pref = getSharedPreferences("Settings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("login", login);
                        editor.putString("password", password);
                        editor.putString("name", name);
                        
                        editor.putBoolean("fakeGps", chbFakeGps.isChecked());
                        editor.putString("startGps", etStartGps.getText().toString());
                        editor.apply();
                        
                        Intent intent = new Intent(EnterActivity.this, StartGameActivity.class);
                        Profile profile = new Profile(name, login, password);
                        intent.putExtra("profile",profile);
                        startActivity(intent);
                    } else {
                        Toast.makeText(EnterActivity.this, "Ошибка входа:\n" + message.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ignored){
                    Toast.makeText(EnterActivity.this, "JSONException:\n" + ignored.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
    
            @Override
            public void onFailure(String error) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(EnterActivity.this, "Ошибка http запроса:\n" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTRATION_REQUEST && resultCode == RESULT_OK) {
            restoreLastLoginData();
        }
    }
    
    
    public void btnRegisterClick(View v) {
        startActivityForResult(new Intent(this, RegisterActivity.class), REGISTRATION_REQUEST);
    }
    
    public void btnVkRegisterClick(View v) {
//        Intent intent = new Intent(EnterActivity.this, StartGameActivity.class);
//        intent.putExtra("profile", new Profile("name", "login", "password"));
//        startActivity(intent);
    
    
        final ArrayList<Integer> teamColors = new ArrayList<>();
        teamColors.add(Color.parseColor("#972EFF"));
        teamColors.add(Color.parseColor("#FFF00D"));
        
        Intent intent = new Intent(EnterActivity.this, GoogleMapsActivity.class);
        intent.putExtra("profile", new Profile("my_name", "my_login", "123"));
        intent.putExtra("teamColors", teamColors);
//                        intent.putExtra("myTeamColor", teamColors.get(0));
    
        intent.putExtra("roomId", 123);
        intent.putExtra("createGame", true);
        startActivity(intent);
    }
}
