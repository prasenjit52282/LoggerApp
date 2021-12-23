package com.disarm.testapp_newdesing01;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ajalan on 20/12/16.
 */

public class ModeOfTransport extends Activity{
    private static final int PERMISSION_ALL = 1;
    private Button submit;
    private RadioGroup rg;
    private RadioButton radio;
    public static String mode="";
    private TextView show;
    private EditText other_mode;
    String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECORD_AUDIO
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermissions(this, PERMISSIONS)) {
                Log.i("permission", "request permissions");
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }
            setContentView(R.layout.activity_mode_of_transport);
            submit = (Button) findViewById(R.id.button);
            rg = (RadioGroup) findViewById(R.id.radio_group);
            show = (TextView) findViewById(R.id.transport);
            show.setText("Select a mode of transport:");
            other_mode = (EditText) findViewById(R.id.editText);
            other_mode.setEnabled(false);
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton others = (RadioButton) findViewById(R.id.rb_others);
                    RadioButton checked = (RadioButton) findViewById(checkedId);
                    if (others == checked) {
                        other_mode.setEnabled(true);
                    }
                }
            });


            OnSubmitClick();
        }
    public void OnSubmitClick() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected_id = rg.getCheckedRadioButtonId();
                radio = (RadioButton) findViewById(selected_id);
                RadioButton others = (RadioButton) findViewById(R.id.rb_others);
                if(radio==others){
                    mode=other_mode.getText().toString();
                    other_mode.setEnabled(false);
                    other_mode.setText("");
                }else {
                    mode = radio.getText().toString();
                }
                Intent intent = new Intent(ModeOfTransport.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }
    private boolean checkPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getBaseContext(),"Permissions Denied!!! Give permissions to access app",Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public static String getMode(){
        return mode;
    }

}
