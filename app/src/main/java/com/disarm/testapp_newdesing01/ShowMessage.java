package com.disarm.testapp_newdesing01;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Surjith on 20-02-2015.
 */
public class ShowMessage{

    public static void ShowMessage(Context context,String title,String message){
        //Toast.makeText(context,message,Toast.LENGTH_LONG).show();
        final AlertDialog.Builder dialogueBuilder=new AlertDialog.Builder(context);

        dialogueBuilder.setMessage(message)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog=dialogueBuilder.create();
        alertDialog.show();

    }

}
