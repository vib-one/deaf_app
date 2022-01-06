package com.example.deaf_alert;


 //Created by Zych on 2017-10-03.


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PowerStateChangedReceiver  extends BroadcastReceiver {

    public static boolean BatteryLow=false;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"Niski poziom baterii",Toast.LENGTH_LONG).show();
        BatteryLow=true;
            if(MyService.isrunning){
                Intent stopServiceIntent = new Intent(context, MyService.class);
                context.stopService(stopServiceIntent);

            }
    }

}

