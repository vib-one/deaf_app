package com.example.deaf_alert;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    private Button back;
    private Button vib;
    private Button night;
    private Button bat;
    public static Boolean nightMode;
    private static Boolean powerSaveMode;
    public static int vibSleep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo_NoActionBar_TranslucentDecor);
        setContentView(R.layout.activity_menu);

        back = findViewById(R.id.back_btn);
        vib = findViewById(R.id.vib_delay_btn);
        night = findViewById(R.id.night_mode_btn);
        bat = findViewById(R.id.bat_save_btn);

    }

    public void onResume() {
        super.onResume();

        getConfig();
        viewIcons();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                back.setBackgroundResource(R.drawable.back_on);

                Intent backIntent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(backIntent);

            }
        });

        vib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (vibSleep==1000){
                    vibSleep=2000;
                    viewIcons();
                }
                else if (vibSleep==2000){
                    vibSleep=5000;
                    viewIcons();
                }
                else if (vibSleep==5000){
                    vibSleep=10000;
                    viewIcons();
                }
                else {
                    vibSleep=1000;
                    viewIcons();
                }
            }
        });

        night.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (nightMode){
                    nightMode=false;
                    viewIcons();
                }
                else{
                    nightMode=true;
                    viewIcons();
                }

            }
        });

        bat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (powerSaveMode){
                    powerSaveMode=false;
                    viewIcons();
                }
                else{
                    powerSaveMode=true;
                    viewIcons();
                }

            }
        });
    }


    @Override
    public void onPause(){
        super.onPause();
        setConfig();
        finish();
    }
//metoda zapisująca ustawienia
    private void setConfig(){
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("night mode", nightMode);
        editor.putBoolean("power save", powerSaveMode);
        editor.putInt("vib sleep", vibSleep);
        editor.apply();
    }
//metoda odczytująca ustawienia
    private void getConfig(){
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        nightMode = settings.getBoolean("night mode", true);
        powerSaveMode = settings.getBoolean("power save", true);
        vibSleep = settings.getInt("vib sleep", 1000);
    }

//metoda sprawdza zmienne ustawiane po kliknięciu przycisku i ustawia odpowiednie tło
    private void viewIcons(){

        vib.setBackgroundResource(R.drawable.vib_sleep_on); //tymczasowo ustawiam ikonę- narazie nie działa wyłączanie spki w trybie nocnym
        back.setBackgroundResource(R.drawable.back_off);

        if (nightMode){
            night.setBackgroundResource(R.drawable.night_mode_on);
        }
        else{
            night.setBackgroundResource(R.drawable.night_mode_off);
        }
        if (powerSaveMode){
            bat.setBackgroundResource(R.drawable.power_save_on);
        }
        else{
            bat.setBackgroundResource(R.drawable.power_save_off);
        }
        if (vibSleep==1000){
            vib.setText("1s");
        }
        else if (vibSleep==2000){
            vib.setText("2s");
        }
        else if (vibSleep==5000){
            vib.setText("5s");
        }
        else if (vibSleep==10000){
            vib.setText("10s");
        }
        else vib.setText("!");
    }
}
