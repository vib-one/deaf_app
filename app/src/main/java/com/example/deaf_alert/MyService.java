package com.example.deaf_alert;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    private static final int sampleRate = 8000;
    public static boolean isrunning = false;
    public static PowerManager.WakeLock mWakeLock;
    public static int noiseValueLevel = 0;
    private static double noiseValueMAX = 0;
    public static double noiseValueMAXdB=0;
    public static double noiseValueRMS=0;
    public static double noiseValueRMSdB=0;
    private static double referenceNoiseLeveldB=1;
    // --Commented out by Inspection (2018-05-31 11:00):public static double max1 = 0;
    private static double reference = 0;
    private double referenceAudioValue =0;
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

    public static final String logTag = "myLogs";

    public MyService() {
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            minBufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            Log.e(logTag, "minBufferSize calculation error", e);
        }

        bufferSize=5*minBufferSize;
        recDelay = (bufferSize * 1000) / (sampleRate);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert powerManager != null;
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vibWakeLockTag");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String channelID= "100";
        NotificationChannel channel= new NotificationChannel(
                channelID,
                channelID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification notification = new Notification.Builder(this,channelID)
                .setContentText("Vib One is running")
                .setContentTitle("Vib One enabled")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        else startForeground(1001,notification);

        String sensitivityMessageTxt = intent.getStringExtra("message");
         if (!mWakeLock.isHeld()) {
             mWakeLock.acquire();
         }

        noiseValueLevel = Integer.parseInt(sensitivityMessageTxt);
        if (noiseValueLevel == 1) {
            referenceNoiseLeveldB=55.0;
        } else if (noiseValueLevel == 2) {
            referenceNoiseLeveldB=60;
        } else if (noiseValueLevel == 3) {
            referenceNoiseLeveldB=70;
        } else if (noiseValueLevel == 4) {
            referenceNoiseLeveldB=80;
        } else if (noiseValueLevel == 5) {
            referenceNoiseLeveldB=85;
        } else if (noiseValueLevel == 6) {
            referenceNoiseLeveldB=90;
        } else referenceNoiseLeveldB=100;

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            //noinspection deprecation
            v.vibrate(250);
        }
        if (!isrunning) {
            testThread();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWakeLock.isHeld()) {
               mWakeLock.release();
            }
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            //noinspection deprecation
            v.vibrate(2000);
        }
        if (PowerStateChangedReceiver.BatteryLow) {
            sendNotification();
        }
        stoptestThread();
        stopRecording();
        //mAudioManager.stopBluetoothSco();
        //mAudioManager.setBluetoothScoOn(false);
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

        testThread = new Thread(() -> {
            while (testThread != null && !testThread.isInterrupted()) {
                startRecording(); //rozpoczynamy nagrywanie
                try {
                    Thread.sleep(recDelay);
                } catch (InterruptedException e) {
                }
                readAudioBuffer();
                calcAlarmValue();
            }
        });
        testThread.setPriority(8);
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

    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];
            int bufferReadResult = 1;
            int square=0;
            double mean=0.0;
            int temp = 0;
            if (audio != null) {
                bufferReadResult = audio.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    if (buffer[i] > temp) {
                       temp = buffer[i];
                    }
                    square += Math.pow(buffer[i], 2);
                    temp = Math.max(temp, buffer[i]);
                }
                mean=(square/(float)(bufferReadResult));
                noiseValueMAX = temp;
                //noiseValueRMS=Math.sqrt(mean);
                //noiseValueRMSdB=10*Math.log10(noiseValueRMS);
                noiseValueMAXdB=20*Math.log10(noiseValueMAX);
            }
        } catch (Exception e) {
            Log.e(logTag, "read Audio Buffer error", e);
        }
    }

    private void calcAlarmValue(){
       /* PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        assert powerManager != null;
        @SuppressLint("InvalidWakeLockTag") final PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vib");
*/
        if (noiseValueMAXdB >= referenceNoiseLeveldB) {
            // uruchamia partial wake lock/ blokuje przejście CPU w tryb uspienia
           // if (!mWakeLock.isHeld()) {
           //     mWakeLock.acquire(5 * 1000L /*5 sekund*/);
           // }
            stopRecording();

            Vibrator vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrate != null) {
                vibrate.vibrate(vibDelay);
            }
            sleep();
            // zwalnia partial wake lock
            //if (mWakeLock.isHeld()) {
            //   mWakeLock.release();
           // }
        }
    }
    private void sleep(){
        try {
            Thread.sleep(recDelay);
        } catch (InterruptedException e) {
            Log.e(logTag, "Thread sleep error",e);
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
