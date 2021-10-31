package com.example.deaf_alert;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

public class MyService extends Service {
    private static final int sampleRate = 16000;
    public static int workingMode = 0;
    private static double maxAudioValue = 0;
    private static double audioAverageValue = 0;
    private static double offset = 2000;
    private static double audioScaleRatio = 100;
    private Thread audioAlertThread;
    private AudioRecord audioRecord;
    private int minBufferSize;
    private int bufferSize;
    private int lastLevel = 0;
    private int recDelay = 0;
    private boolean isRecord = false;
    public static boolean isAudioAlertThreadRunning = false;
    private int bufferReadResult;

    @Override
    public void onCreate() {
        super.onCreate();
        calcAudioBufferSize();

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

        getWorkingMode(intent);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            //noinspection deprecation
            v.vibrate(250);
        }
        if (!isAudioAlertThreadRunning) {
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
        stopTestThread();
        stopRecording();
        //koniec blokady przejścia CPU w tryb uspienia
    }

    private void startRecording() {
        if (!isRecord) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            audioRecord.startRecording();
            isRecord = true;
        }
    }

    private void stopRecording() {
        if (null != audioRecord && isRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            isRecord = false;
        }
    }

    private void testThread() {
/*
        PowerManager vibPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        assert vibPowerManager != null;
        @SuppressLint("InvalidWakeLockTag") final PowerManager.WakeLock vibWakeLock = vibPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vibWakeLock");
*/
        audioAlertThread = new Thread(new Runnable() {
            public void run() {
                while (audioAlertThread != null && !audioAlertThread.isInterrupted()) {
                    startRecording();
                    try {
                        Thread.sleep(recDelay);
                    } catch (InterruptedException e) {
                    }
                    readAudioBuffer();
                    calcMaxAudioBufferValue();
                    calcAlertAudioValue();
                    audioHighValueVibrationAlert();
                }
            }
        });
        audioAlertThread.setPriority(10);
        audioAlertThread.start();
        isAudioAlertThreadRunning = true;
    }

    private void stopTestThread() {
        if (isAudioAlertThreadRunning) {
            audioAlertThread.interrupt();
            audioAlertThread = null;
            isAudioAlertThreadRunning = false;
        }
    }

    private void getWorkingMode(Intent intent){

        String workingModeTxt = intent.getStringExtra("message");
        workingMode = Integer.parseInt(workingModeTxt);
        if (workingMode == 1) {
            offset = 700;
        } else if (workingMode == 2) {
            offset = 1500;
        } else if (workingMode == 3) {
            offset = 4000;
        } else if (workingMode == 4) {
            offset = 10000;
        } else offset = 20000;
    }

    private void calcAudioBufferSize(){

        try {
            minBufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }
        bufferSize=2*minBufferSize;
        recDelay = (bufferSize * 1000) / (2 * sampleRate);
    }

    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];
            if (audioRecord != null) {
                bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calcMaxAudioBufferValue(){
        int tempAudioValue=0;
        short[] tempAudioBuffer = new short[bufferSize];
        for (int i = 0; i < bufferReadResult; i++) {
            if (tempAudioBuffer[i] > tempAudioValue) {
                tempAudioValue = tempAudioBuffer[i];
            }
            tempAudioValue = Math.max(tempAudioValue, tempAudioBuffer[i]);
        }
        maxAudioValue = tempAudioValue;
    }

    private void calcAlertAudioValue(){

        double averageSampleLength = 10;
        audioAverageValue = (audioAverageValue * (averageSampleLength - 1) / averageSampleLength) + (maxAudioValue / averageSampleLength);

        //double ref1=Math.log10(1.1*ref);
        //double max2=Math.log10(max);

        if (audioAverageValue >= 0 && audioAverageValue < 1000) {
            audioScaleRatio = -(0.008) * audioAverageValue + 10;
        } else if (audioAverageValue >= 1000 && audioAverageValue < 5000) {
            audioScaleRatio = -(0.0001) * audioAverageValue + 2.125;
        } else if (audioAverageValue >= 5000 && audioAverageValue < 20000) {
            audioScaleRatio = -(0.000027) + 1.63;
        } else {
            audioScaleRatio = 1.1;
        }

    }
    private void audioHighValueVibrationAlert(){
        /*
        PowerManager vibPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        assert vibPowerManager != null;
        @SuppressLint("InvalidWakeLockTag") final PowerManager.WakeLock vibWakeLock = vibPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vibWakeLock");
*/
        if (maxAudioValue > offset + audioAverageValue * audioScaleRatio) {
            // uruchamia partial wake lock/ blokuje przejście CPU w tryb uspienia
            //if (!vibWakeLock.isHeld()) {
            //    vibWakeLock.acquire(5 * 1000L /*5 sekund*/);
            //}
            stopRecording();
            //max1=0;
            maxAudioValue = 0; //sprawdzic czy koneiczne!!!!!!!!!
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                int vibDelay = 500;
                v.vibrate(vibDelay);
            }
           /* try {
                Thread.sleep(MenuActivity.vibSleep); //wątek śpi przez czas wybrany w menu 1,2,5,10s
            } catch (InterruptedException e) {
            }*/
            // zwalnia partial wake lock
            //if (vibWakeLock.isHeld()) {
            //    vibWakeLock.release();
            //}
        }

    }

    // metoda wyświetla powiadomienie o wyłączeniu aplikacji
    private void sendNotification() {
/*
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
 */
    }

}
