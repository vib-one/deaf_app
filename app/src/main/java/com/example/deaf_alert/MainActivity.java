package com.example.deaf_alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isOn =false;
    private boolean permission=false;
    private int noiseLevel=0;
    private double noiseLevelValueRMSdB=0;
    private double noiseLevelValueMAXdB =0;
    private RequestPermissionHandler mRequestPermissionHandler;

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        setContentView(R.layout.activity_main);

        mRequestPermissionHandler = new RequestPermissionHandler();
        permissionCheck();

        Button onOffButton = findViewById(R.id.onOffButton);
        onOffButton.setOnClickListener(this);
        Button noiseLevelButton = findViewById(R.id.noiseLevelButton);
        noiseLevelButton.setOnClickListener(this);
        Button youtubeButton = findViewById(R.id.youtubeButton);
        youtubeButton.setOnClickListener(this);
        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this);
        Button recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this);
        TextView speechToText = findViewById(R.id.speechToText);
        checkStatusService();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                speechToText.setText("");
            }

            @Override
            public void onBeginningOfSpeech() {
                speechToText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                speechToText.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    stopMyService();
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });

    }

    public void onResume() {
        super.onResume();

        checkStatusService();
        /*TextView noiseValueRMSdB=(TextView)findViewById(R.id.textView3);
        String noiseValueRMStxt=Double.toString(noiseLevelValueRMSdB);
        noiseValueRMSdB.setText(noiseValueRMStxt);
*/
        TextView noiseValueRMSdB=(TextView)findViewById(R.id.textView3);
        String noiseValueRMStxt=Double.toString(MyService.noiseValueLevel);
        noiseValueRMSdB.setText(noiseValueRMStxt);

        TextView noiseValueMAXdB=(TextView)findViewById(R.id.textView4);
        String noiseValueMAXdBtxt=Double.toString(noiseLevelValueMAXdB);
        noiseValueMAXdB.setText(noiseValueMAXdBtxt);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void startMyService() {

        //-----> metoda uruchamia MyService (usługa w tle)
        Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
        //-----> wysyła string z wartością czułości do MyService
        String tryb1txt = String.valueOf(noiseLevel);
        serviceIntent.putExtra("message", tryb1txt);
        //-----> start działania MyService
        this.startService(serviceIntent);
        isOn = true;
    }

    //metoda zatrzymuje działanie MyService
    private void stopMyService() {
        stopService(new Intent(getBaseContext(),MyService.class));
        isOn = false;
    }

    private void checkStatusService(){
        if(MyService.isrunning){
            isOn =true;
            noiseLevel =MyService.noiseValueLevel;
            noiseLevelValueRMSdB =MyService.noiseValueRMSdB;
            noiseLevelValueMAXdB =MyService.noiseValueMAXdB;

            //onOffBtn.setBackgroundResource(R.drawable.poweron);

        }else{
            isOn =false;
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
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onOffButton:
                if (!permission){
                    Context context = getApplicationContext();
                    Toast.makeText(context,"Nie udzielono zezwolenia na nagrywanie",Toast.LENGTH_SHORT).show();
                }
                if (!isOn & permission){
                    noiseLevel =1;
                    startMyService();
                }
                else{
                    noiseLevel = 0;
                    //onOffBtn.setBackgroundResource(R.drawable.poweroff);
                    //viewIcons();
                    stopMyService();
                }
                break;
            case R.id.noiseLevelButton:
                if(noiseLevel >=0 && noiseLevel<6){
                    noiseLevel++;
                }
                else noiseLevel =0;
                TextView noiseValueRMSdB=(TextView)findViewById(R.id.textView3);
                String noiseValueRMStxt=Double.toString(MyService.noiseValueLevel);
                noiseValueRMSdB.setText(noiseValueRMStxt);
                startMyService();
                break;
            case R.id.youtubeButton:
                Intent youtubeChannelIntent = new Intent(MainActivity.this, YouTubeChannelActivity.class);
                startActivity(youtubeChannelIntent);
                break;
            case R.id.menuButton:
                Intent menuIntent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(menuIntent);
                break;
            case R.id.speechToText:

                break;
            default:
                break;
        }

    }
    private void permissionCheck(){
        mRequestPermissionHandler.requestPermission(this, new String[] {
                Manifest.permission.RECORD_AUDIO
        }, 123, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "request permission success", Toast.LENGTH_SHORT).show();
                permission=true;
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "request permission failed", Toast.LENGTH_SHORT).show();
                permission=false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

