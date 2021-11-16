package com.example.deaf_alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {

    private boolean is_ON=false;
    private boolean permission=false;
    private int tryb1=0;
    private Button on_off_btn;
    private Button btn_1;
    private Button btn_2;
    private Button btn_3;
    private Button btn_4;
    private Button btn_5;
    private Button menu;
    private Button btnTranslateActivity;
    private AudioManager mAudioManager;



    private RequestPermissionHandler mRequestPermissionHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo_NoActionBar_TranslucentDecor);
        setContentView(R.layout.activity_main);

        mRequestPermissionHandler = new RequestPermissionHandler();
        permissionCheck();

        on_off_btn = findViewById(R.id.on_off_btn);
        btn_1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        btn_4 = findViewById(R.id.btn_4);
        btn_5 = findViewById(R.id.btn_5);
        menu = findViewById(R.id.menu);
        btnTranslateActivity= findViewById(R.id.translate_btn);

        checkStatusService();
        viewIcons();
    }

    public void onResume() {
        super.onResume();

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.startBluetoothSco();

        checkStatusService();
        viewIcons();
/*
        ToggleButton ON_OFF_toggle = (ToggleButton) findViewById(R.id.ON_OFF_btn);
        ON_OFF_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Context context = getApplicationContext();
                    Toast.makeText(context,"vib one- uruchomiono",Toast.LENGTH_SHORT).show();
                    //Start_Service();
                    is_ON=true;
                    // The toggle is enabled

                } else {
                    Stop_Service();
                    is_ON=false;
                    // The toggle is disabled
                }
            }
        });
        */
        on_off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!permission){

                    Context context = getApplicationContext();
                    Toast.makeText(context,"Nie udzielono zezwolenia na nagrywanie",Toast.LENGTH_SHORT).show();

                }

                if (!is_ON & permission){
                    tryb1=3;
                    on_off_btn.setBackgroundResource(R.drawable.poweron);
                    viewIcons();
                    //Context context = getApplicationContext();
                    //Toast.makeText(context,"vib one- uruchomiono",Toast.LENGTH_SHORT).show();
                    Start_Service();
                }
                else{
                    tryb1 = 0;
                    on_off_btn.setBackgroundResource(R.drawable.poweroff);
                    viewIcons();
                    Stop_Service();
                }
            }
        });
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyService.isrunning){
                    tryb1 = 1;
                    //tryb1txt = String.valueOf(tryb1);
                    //Context context = getApplicationContext();
                    //Toast.makeText(context,"Tryb 1",Toast.LENGTH_SHORT).show();
                    Start_Service();
                    //checkStatusService();
                    viewIcons();
                }
            }
        });
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyService.isrunning){
                    tryb1 = 2;
                    //tryb1txt = String.valueOf(tryb1);
                    //Context context = getApplicationContext();
                    //Toast.makeText(context,"Tryb 2",Toast.LENGTH_SHORT).show();
                    Start_Service();
                    //checkStatusService();
                    viewIcons();
                }
            }
        });
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyService.isrunning){
                    tryb1 = 3;
                    //tryb1txt = String.valueOf(tryb1);
                    //Context context = getApplicationContext();
                    //Toast.makeText(context,"Tryb 3",Toast.LENGTH_SHORT).show();
                    Start_Service();
                    //checkStatusService();
                    viewIcons();
                }
            }
        });
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyService.isrunning){
                    tryb1 = 4;
                    //tryb1txt = String.valueOf(tryb1);
                    //Context context = getApplicationContext();
                    //Toast.makeText(context,"Tryb 4",Toast.LENGTH_SHORT).show();
                    Start_Service();
                    //checkStatusService();
                    viewIcons();
                }
            }
        });
        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyService.isrunning){
                    tryb1 = 5;
                    //tryb1txt = String.valueOf(tryb1);
                    //Context context = getApplicationContext();
                    //Toast.makeText(context,"Tryb 5",Toast.LENGTH_SHORT).show();
                    Start_Service();
                    //checkStatusService();
                    viewIcons();
                }
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(menuIntent);
            }
        });
        btnTranslateActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent menuIntent = new Intent(MainActivity.this, TranslateActivity.class);
                startActivity(menuIntent);
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        mAudioManager.stopBluetoothSco();
        mAudioManager.setBluetoothScoOn(false);
        finish();

    }

    private void Start_Service() {

        //-----> metoda uruchamia MyService (usługa w tle)
        Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
        //-----> wysyła string z wartością czułości do MyService
        String tryb1txt = String.valueOf(tryb1);
        serviceIntent.putExtra("message", tryb1txt);
        //-----> start działania MyService
        this.startService(serviceIntent);
        is_ON = true;
    }

    //metoda zatrzymuje działanie MyService
    private void Stop_Service() {
        stopService(new Intent(getBaseContext(),MyService.class));
        is_ON = false;
    }

    private void checkStatusService(){
        if(MyService.isrunning){
            is_ON=true;
            tryb1=MyService.tryb;

            on_off_btn.setBackgroundResource(R.drawable.poweron);

        }else{
            is_ON=false;
            on_off_btn.setBackgroundResource(R.drawable.poweroff);
        }
    }
    private void viewIcons(){
        if (tryb1==1){
            btn_1.setBackgroundResource(R.drawable.on1);
            btn_2.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==2){
            btn_1.setBackgroundResource(R.drawable.off1);
            btn_2.setBackgroundResource(R.drawable.on2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==3){
            btn_1.setBackgroundResource(R.drawable.off1);
            btn_2.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.on3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==4){
            btn_1.setBackgroundResource(R.drawable.off1);
            btn_2.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.on4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==5){
            btn_1.setBackgroundResource(R.drawable.off1);
            btn_2.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.on5);
        }
        else{
            btn_1.setBackgroundResource(R.drawable.off1);
            btn_2.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
    }

    private void permissionCheck(){
        mRequestPermissionHandler.requestPermission(this, new String[] {
                Manifest.permission.RECORD_AUDIO
        }, 123, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() {
                //Toast.makeText(MainActivity.this, "request permission success", Toast.LENGTH_SHORT).show();
                permission=true;
            }

            @Override
            public void onFailed() {
                //Toast.makeText(MainActivity.this, "request permission failed", Toast.LENGTH_SHORT).show();
                permission=false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestPermissionHandler.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
    }


}

