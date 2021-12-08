package com.example.deaf_alert;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.View;


import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class YouTubeChannelActivity extends AppCompatActivity {

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtubechannel);

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent backIntent = new Intent(YouTubeChannelActivity.this, MainActivity.class);
                startActivity(backIntent);

            }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}