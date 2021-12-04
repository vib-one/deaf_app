package com.example.deaf_alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.Intent;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean is_ON=false;
    private boolean permission=false;
    private int tryb1=0;
    private Button onOffButton;
    private Button noiseLevelButton;
    private Button youtubeButton;
    private Button btn_3;
    private Button btn_4;
    private Button btn_5;
    private Button menuButton;
    private Button recordButton;
    private RequestPermissionHandler mRequestPermissionHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_main);

        mRequestPermissionHandler = new RequestPermissionHandler();
        permissionCheck();

        onOffButton = findViewById(R.id.onOffButton);
        onOffButton.setOnClickListener(this);
        noiseLevelButton = findViewById(R.id.noiseLevelButton);
        noiseLevelButton.setOnClickListener(this);
        youtubeButton = findViewById(R.id.youtubeButton);
        youtubeButton.setOnClickListener(this);
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this);
        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this);

        checkStatusService();
        //viewIcons();
    }

    public void onResume() {
        super.onResume();

        checkStatusService();
        //viewIcons();
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
        youtubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyService.isrunning){
                    tryb1 = 2;
                    //tryb1txt = String.valueOf(tryb1);
                    //Context context = getApplicationContext();
                    //Toast.makeText(context,"Tryb 2",Toast.LENGTH_SHORT).show();
                    //checkStatusService();
                }
            }
        });
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(menuIntent);
            }
        });
        */
    }

    @Override
    public void onPause(){
        super.onPause();

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

            //onOffBtn.setBackgroundResource(R.drawable.poweron);

        }else{
            is_ON=false;
            //onOffBtn.setBackgroundResource(R.drawable.poweroff);
        }
    }
 /*   private void viewIcons(){
        if (tryb1==1){
            noiseLevelButton.setBackgroundResource(R.drawable.on1);
            youtubeButton.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==2){
            noiseLevelButton.setBackgroundResource(R.drawable.off1);
            youtubeButton.setBackgroundResource(R.drawable.on2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==3){
            noiseLevelButton.setBackgroundResource(R.drawable.off1);
            youtubeButton.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.on3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==4){
            noiseLevelButton.setBackgroundResource(R.drawable.off1);
            youtubeButton.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.on4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
        else if (tryb1==5){
            noiseLevelButton.setBackgroundResource(R.drawable.off1);
            youtubeButton.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.on5);
        }
        else{
            noiseLevelButton.setBackgroundResource(R.drawable.off1);
            youtubeButton.setBackgroundResource(R.drawable.off2);
            btn_3.setBackgroundResource(R.drawable.off3);
            btn_4.setBackgroundResource(R.drawable.off4);
            btn_5.setBackgroundResource(R.drawable.off5);
        }
    }
*/
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onOffButton:
                if (!permission){
                    Context context = getApplicationContext();
                    Toast.makeText(context,"Nie udzielono zezwolenia na nagrywanie",Toast.LENGTH_SHORT).show();
                }
                if (!is_ON & permission){
                    tryb1=3;
                    Start_Service();
                }
                else{
                    tryb1 = 0;
                    //onOffBtn.setBackgroundResource(R.drawable.poweroff);
                    //viewIcons();
                    Stop_Service();
                }
                break;
            case R.id.noiseLevelButton:
                if(tryb1<4){
                    tryb1++;
                }
                else tryb1=0;
                Start_Service();
                break;
            case R.id.youtubeButton:
                // do your code
                break;
            case R.id.menuButton:
                Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(menuIntent);
                break;
            default:
                break;
        }

    }
}

