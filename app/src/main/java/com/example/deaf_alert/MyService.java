package com.example.deaf_alert;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    private static final int sampleRate = 8000;
    public static boolean isrunning = false;
    //public static PowerManager.WakeLock mWakeLock;
    public static int noiseLevel = 0;
    private static double max = 0;
    // --Commented out by Inspection (2018-05-31 11:00):public static double max1 = 0;
    private static double ref = 0;
    private double refAudioValue=0;
    // --Commented out by Inspection (2018-05-31 11:00):private static double max_length = 2;
    private static double offset = 2000;
    private static double ratio = 100;
    private Thread testThread;
    private final double refLength = 10;
    private AudioRecord audio;
    private int minBufferSize;
    private int bufferSize;
    private int lastLevel = 0;
    private int recDelay = 0;
    private final int vibDelay = 500;
    // --Commented out by Inspection (2018-05-31 11:00):private int czulosc = 0;
    private boolean isRecording = false;
    private AudioManager mAudioManager;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //bufferSize = 512;

        try {
            minBufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_8BIT);
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }

        bufferSize=2* minBufferSize;

        recDelay = (bufferSize * 1000) / (2 * sampleRate);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "");

        // inicjalizuje partial wake lock/ blokadę przejścia CPU w tryb uśpienia

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this,"vib one uruchomiono",Toast.LENGTH_LONG).show();

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.startBluetoothSco();


        //odczytuję wartość czułości z activity
        String trybtxt = intent.getStringExtra("message");


        noiseLevel = Integer.parseInt(trybtxt);
        if (noiseLevel == 1) {
            offset = 700;
        } else if (noiseLevel == 2) {
            offset = 1500;
        } else if (noiseLevel == 3) {
            offset = 4000;
        } else if (noiseLevel == 4) {
            offset = 10000;
        } else offset = 20000;


        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            //noinspection deprecation
            v.vibrate(250);
        }

        if (!isrunning) {
            testThread();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(this,"vib one zatrzymano",Toast.LENGTH_SHORT).show();
        super.onDestroy();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            //noinspection deprecation
            v.vibrate(500);
        }
        if (PowerStateChangedReceiver.BatteryLow) {
            sendNotification();
        }
        stoptestThread();
        stopRecording();
        mAudioManager.stopBluetoothSco();
        mAudioManager.setBluetoothScoOn(false);
        //koniec blokady przejścia CPU w tryb uspienia
    }

    private void startRecording() {
        if (!isRecording) {
            audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            audio.startRecording();
            isRecording = true;
        }
    }

    private void stopRecording() {
        if (null != audio && isRecording) {
            audio.stop();
            audio.release();
            audio = null;
            isRecording = false;
        }
    }

    private void testThread() {
/*
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        assert powerManager != null;
        @SuppressLint("InvalidWakeLockTag") final PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vib");
*/
        testThread = new Thread(new Runnable() {
            public void run() {
                while (testThread != null && !testThread.isInterrupted()) {
                    startRecording(); //rozpoczynamy nagrywanie
                   /*
                    try {
                        Thread.sleep(rec_DELAY);
                    } catch (InterruptedException e) {
                    }
                    */
                    readAudioBuffer();
                    //max1 = (max1 * (max_length - 1) / max_length) + (max / max_length);
                    /*ref = (ref * (ref_length - 1) / ref_length) + (max / ref_length);

                    //double ref1=Math.log10(1.1*ref);
                    //double max2=Math.log10(max);

                    if (ref >= 0 && ref < 1000) {
                        ratio = -(0.008) * ref + 10;
                    } else if (ref >= 1000 && ref < 5000) {
                        ratio = -(0.0001) * ref + 2.125;
                    } else if (ref >= 5000 && ref < 20000) {
                        ratio = -(0.000027) + 1.63;
                    } else {
                        ratio = 1.1;
                    }
*/
                    refAudioBuffer();
                    //ratio=-(1/1000)*ref+5;
                    /*
                    if (max > offset + ref * ratio) {
                        // uruchamia partial wake lock/ blokuje przejście CPU w tryb uspienia
                        if (!mWakeLock.isHeld()) {
                            mWakeLock.acquire(5 * 1000L);
                        }
                        stopRecording();
                        //max1=0;
                        max = 0;
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            v.vibrate(vib_DELAY);
                        }
                        try {
                            Thread.sleep(MenuActivity.vibSleep); //wątek śpi przez czas wybrany w menu 1,2,5,10s
                        } catch (InterruptedException e) {
                        }
                        // zwalnia partial wake lock
                        if (mWakeLock.isHeld()) {
                            mWakeLock.release();
                        }
                    }
                    */
                    calcAlarmValue();
                }
            }
        });
        testThread.setPriority(10);
        testThread.start();
        isrunning = true;
    }

    private void stoptestThread() {
        if (isrunning) {
            testThread.interrupt();
            testThread = null;
            isrunning = false;
        }
    }

    //metoda oblicza wartość natężenia dźwięku
    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];
            int bufferReadResult = 1;
            int temp = 0;
            if (audio != null) {
                bufferReadResult = audio.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    if (buffer[i] > temp) {
                        temp = buffer[i];
                    }
                    temp = Math.max(temp, buffer[i]);
                }
                max = temp;
            }
            if (lastLevel < 0) lastLevel = -lastLevel;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refAudioBuffer(){
        ref = (ref * (refLength - 1) / refLength) + (max / refLength);

        //double ref1=Math.log10(1.1*ref);
        //double max2=Math.log10(max);

        if (ref >= 0 && ref < 1000) {
            ratio = -(0.008) * ref + 10;
        } else if (ref >= 1000 && ref < 5000) {
            ratio = -(0.0001) * ref + 2.125;
        } else if (ref >= 5000 && ref < 20000) {
            ratio = -(0.000027) + 1.63;
        } else {
            ratio = 1.1;
        }
        refAudioValue= offset + ref * ratio;
    }
    private void calcAlarmValue(){
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        assert powerManager != null;
        @SuppressLint("InvalidWakeLockTag") final PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vib");

        if (max > refAudioValue) {
            // uruchamia partial wake lock/ blokuje przejście CPU w tryb uspienia
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire(5 * 1000L /*5 sekund*/);
            }
            stopRecording();
            //max1=0;
            max = 0;
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(vibDelay);
            }
            try {
                Thread.sleep(MenuActivity.vibSleep); //wątek śpi przez czas wybrany w menu 1,2,5,10s
            } catch (InterruptedException e) {
            }
            // zwalnia partial wake lock
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }
    private void sleep(){
        try {
            Thread.sleep(recDelay);
        } catch (InterruptedException e) {
        }
    }

    // metoda wyświetla powiadomienie o wyłączeniu aplikacji
    private void sendNotification() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.menu)
                        .setContentTitle("vib one")
                        .setContentText("Aplikacja wyłączona");

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mBuilder.build());
        }

    }

}
