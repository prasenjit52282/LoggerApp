package com.disarm.testapp_newdesing01;

//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
//import android.support.v7.internal.widget.AppCompatPopupWindow;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * Created by ajalan065 on 30-10-2016.
 */
public class GPSAndSensorLogger extends Fragment {

    private FileOutputStream fosGPS,fosACC,fosLACC,fosCOM,fosGYR,fosGSM, fosWiFi , fosLGT,fosBatteryLog;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SensorManager accSensorManager, laccSensorManager,comSensorManager,gyrSensorManager, lightSensorManager;
    private TelephonyManager mTelManager;
    private SensorEventListener accSensorEventListener,laccSensorEventListener,comSensorEventListener,gyrSensorEventListener, lightSensorEventListener;
    PhoneStateListener mSignalListener;
    MediaRecorder mRecorder = null;
    Timer t = new  Timer();
    private Sensor accSensor,laccSensor,comSensor,gyrSensor, lightSensor;

    private int fbumper=0, fbusStop=0, fpothole=0,fbusyRoad=0,fbrokeRoad=0,fjunction=0,fturn=0,f_overTake=0,f_island=0,f_dummy=0;
    private int cbumper=0, cbusStop=0,cpothole=0,cbusyRoad=0,cbrokeRoad=0,cjunction=0,cturn=0,c_overTake=0,c_island=0,c_dummy=0;
    private String bumperStr="Bumper" ,busstopStr="BusStop",potholeStr="PothHole",busyroadStr="BusyRoad",brokeroadStr="BrokenRoad", junctionStr="Junction", turnStr="Turn",overtakeStr="OverTake",islandStr="Island", dummyStr="Dummy";

    private String marker="";
    private Map<String, Integer> landmark =new HashMap<String,Integer>();

    private String appFolderName="GPSAndSensorRecorder";
    private File folder,subfolder;
    private Timestamp timestamp;

    private String timestampStr;
    private Date accStartTime,laccStartTime,comStartTime,gyrStartTime,accStopTime,laccStopTime,comStopTime,gyrStopTime, lightStartTime;
    private float batteryUsage;
    private Date date;
    private Long time;
    private WifiManager mainWifi;
    private WifiReceiver receiverWifi;

    TextView errorTextView;
    TextView detailsTextView,statusTextView;

    Button bumperBtn,potholeBtn,busStopBtn,junctionBtn,busyRoadBtn,brokeRoadBtn,turnBtn,overTakeBtn,islandBtn,dummyBtn;
    ImageView bumperImg,potholeImg,busStopImg,junctionImg,busyRoadImg,brokeRoadImg,turnImg,overTakeImg,islandImg,dummyImg;
    CheckBox checkACC,checkLACC,checkGPS,checkGYR,checkCOM, checkGSM, checkWiFi,checkLight;
    private boolean lightStarted = false, gpsStarted = false, gsmStarted = false, accStarted = false, laccStarted = false, comStarted = false, gyrStarted = false, wifiStarted = false;
    //boolean GPS_STARTED=false;

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    Logger logger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //called only once in the lifetime of fragment. When the fragment is added to the app first.
        super.onCreate(savedInstanceState);
        Log.d("Surji","LoggerFrag_OnCreate Has Called");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Called whenever the view is recreated. ie: whenever selecting the logger tab

        Log.d("Surji","LoggerFrag_OnCreateView Has Called");
        return inflater.inflate(R.layout.logger_layout,container,false);
    }

    @Override
    public void onStop() {
        //Called whenever the fragment view is destroyed.ie: when selecting other section tabs and whenever home button is pressed.
        //ie: the app is working in background
        super.onStop();
        Log.d("Surji","LoggerFrag_OnStop Has Called");

    }

    @Override
    public void onDestroyView() {
        //Called whenever the fragment view is destroyed.ie: when selecting other section tabs.

        if(((Button)getActivity().findViewById(R.id.btnStopAll)).isEnabled()){
            ShowMessage.ShowMessage(getActivity(),"Alert..!","Recording has stopped");
            stopRecordingAll();
        }

        super.onDestroyView();

        Log.d("Surji","LoggerFrag_OnDestroyView Has Called");
    }

    @Override
    public void onDestroy() {
        //Only Called once when the App is closed completely
        super.onDestroy();
        Log.d("Surji","LoggerFrag_OnDestroy Has Called");
    }

    @Override
    public void onDetach() {
        //Only Called once when the App is closed completely
        super.onDetach();
        Log.d("Surji","LoggerFrag_OnDetach Has Called");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        errorTextView=(TextView)getActivity().findViewById(R.id.txtError);
        //detailsTextView=(TextView)getActivity().findViewById(R.id.txtDetails);
        statusTextView=(TextView)getActivity().findViewById(R.id.txtStatus);

        initiateLandMarks();
        checkAvailableSensors();
        //initiateDetails();

        powerManager=(PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"LoggerWakeLock");


        bumperBtn=(Button)getActivity().findViewById(R.id.btnLM1);
        potholeBtn=(Button)getActivity().findViewById(R.id.btnLM2);
        busStopBtn=(Button)getActivity().findViewById(R.id.btnLM3);
        junctionBtn=(Button)getActivity().findViewById(R.id.btnLM4);
        busyRoadBtn=(Button)getActivity().findViewById(R.id.btnLM5);
        brokeRoadBtn=(Button)getActivity().findViewById(R.id.btnLM6);
        turnBtn=(Button)getActivity().findViewById(R.id.btnLM7);
        overTakeBtn=(Button)getActivity().findViewById(R.id.btnLM8);
        islandBtn=(Button)getActivity().findViewById(R.id.btnLM9);
        dummyBtn=(Button)getActivity().findViewById(R.id.btnLM10);

        checkACC=(CheckBox)getActivity().findViewById(R.id.checkACC);
        checkLACC=(CheckBox)getActivity().findViewById(R.id.checkLACC);
        checkGYR=(CheckBox)getActivity().findViewById(R.id.checkGYR);
        checkCOM=(CheckBox)getActivity().findViewById(R.id.checkCOM);
        checkGPS=(CheckBox)getActivity().findViewById(R.id.checkGPS);
        checkGSM=(CheckBox)getActivity().findViewById(R.id.checkGSM);
        checkWiFi=(CheckBox)getActivity().findViewById(R.id.checkWiFi);
        checkLight = (CheckBox)getActivity().findViewById(R.id.checkLGT);

        bumperImg=(ImageView)getActivity().findViewById(R.id.imgLM1);
        potholeImg=(ImageView)getActivity().findViewById(R.id.imgLM2);
        busStopImg=(ImageView)getActivity().findViewById(R.id.imgLM3);
        junctionImg=(ImageView)getActivity().findViewById(R.id.imgLM4);
        busyRoadImg=(ImageView)getActivity().findViewById(R.id.imgLM5);
        brokeRoadImg=(ImageView)getActivity().findViewById(R.id.imgLM6);
        turnImg=(ImageView)getActivity().findViewById(R.id.imgLM7);
        overTakeImg=(ImageView)getActivity().findViewById(R.id.imgLM8);
        islandImg=(ImageView)getActivity().findViewById(R.id.imgLM9);
        dummyImg=(ImageView)getActivity().findViewById(R.id.imgLM10);

        /*
        checkGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                }
            }
        });
        checkACC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    accStartRecord();
                }else{
                    try{
                        accSensorManager.unregisterListener(accSensorEventListener);
                        fosACC.close();
                        batteryUsage=accSensor.getPower();
                        batteryLoger("ACC", batteryUsage, accStartTime, date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
        */

        bumperBtn.setOnClickListener(new LMButtonClickListener());
        potholeBtn.setOnClickListener(new LMButtonClickListener());
        busStopBtn.setOnClickListener(new LMButtonClickListener());
        junctionBtn.setOnClickListener(new LMButtonClickListener());
        busyRoadBtn.setOnClickListener(new LMButtonClickListener());
        brokeRoadBtn.setOnClickListener(new LMButtonClickListener());
        turnBtn.setOnClickListener(new LMButtonClickListener());
        overTakeBtn.setOnClickListener(new LMButtonClickListener());
        islandBtn.setOnClickListener(new LMButtonClickListener());
        dummyBtn.setOnClickListener(new LMButtonClickListener());



        final ToggleButton pauseBtn=(ToggleButton)getActivity().findViewById(R.id.btnPause);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state=(String) pauseBtn.getText();
                Log.d("test1", state);

                if(state.equals("Continue")){
                    //recording paused
                    Log.d("test","Paused");
                    statusTextView.setText("Recording Paused");

                    if(gpsStarted){
                        locationManager.removeUpdates(locationListener);
                    }
                    if(accStarted){
                        accSensorManager.unregisterListener(accSensorEventListener, accSensor);
                    }
                    if(laccStarted){
                        laccSensorManager.unregisterListener(laccSensorEventListener, laccSensor);
                    }
                    if(gyrStarted){
                        gyrSensorManager.unregisterListener(gyrSensorEventListener, gyrSensor);
                    }
                    if(comStarted){
                        comSensorManager.unregisterListener(comSensorEventListener, comSensor);
                    }
                    if(gsmStarted){
                        mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_NONE);
                    }
                    if(wifiStarted){
                        getActivity().unregisterReceiver(receiverWifi);
                    }


                    //Calling batteryLoger function
                    date=new Date();
                    try{
                        batteryUsage=accSensor.getPower();
                        batteryLoger("ACC",batteryUsage,accStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    try{
                        batteryUsage=laccSensor.getPower();
                        batteryLoger("LACC",batteryUsage,laccStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    try{
                        batteryUsage=gyrSensor.getPower();
                        batteryLoger("GYR",batteryUsage,gyrStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    try{
                        batteryUsage=comSensor.getPower();
                        batteryLoger("COM",batteryUsage,comStartTime,date);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }



                }else{
                    //recording continuous
                    Log.d("test", "Continue");
                    date=new Date();
                    accStartTime=date;
                    laccStartTime=date;
                    gyrStartTime=date;
                    comStartTime=date;
                    if(gpsStarted)
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                    if(accStarted)
                        accSensorManager.registerListener(accSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(laccStarted)
                        laccSensorManager.registerListener(laccSensorEventListener, laccSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(gyrStarted)
                        gyrSensorManager.registerListener(gyrSensorEventListener, gyrSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(comStarted)
                        comSensorManager.registerListener(comSensorEventListener, comSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if(gsmStarted)
                        mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                    if(wifiStarted)
                        getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    statusTextView.setText("Recording Continuing");
                }
            }
        });


        /*final Button gpsStartBtn=(Button)getActivity().findViewById(R.id.btnGpsStart);
        final Button accStartBtn=(Button)getActivity().findViewById(R.id.btnAccStart);
        final Button laccStartBtn=(Button)getActivity().findViewById(R.id.btnLaccStart);
        final Button gyrStartBtn=(Button)getActivity().findViewById(R.id.btnGyrStart);
        final Button comStartBtn=(Button)getActivity().findViewById(R.id.btnComStart);*/

        final Button startAllBtn=(Button)getActivity().findViewById(R.id.btnStartAll);

        /*final Button gpsStopBtn=(Button)getActivity().findViewById(R.id.btnGpsStop);
        final Button accStopBtn=(Button)getActivity().findViewById(R.id.btnAccStop);
        final Button laccStopBtn=(Button)getActivity().findViewById(R.id.btnLaccStop);
        final Button gyrStopBtn=(Button)getActivity().findViewById(R.id.btnGyrStop);
        final Button comStopBtn=(Button)getActivity().findViewById(R.id.btnComStop);*/
        final Button stopAllBtn=(Button)getActivity().findViewById(R.id.btnStopAll);

        startAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                startRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).show();

            }
        });
        stopAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                stopRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).setCancelable(false).show();

            }
        });

        // For the light sensor
        final ToggleButton pauseLightBtn = (ToggleButton)getActivity().findViewById(R.id.btnlightPause);
        pauseLightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state = (String) pauseLightBtn.getText();
                Log.d("test1", state);

                if (state.equals("Continue")) {
                    Log.d("test", "Paused");
                    statusTextView.setText("Recording Paused");

                    if (lightStarted) {
                        lightSensorManager.unregisterListener(lightSensorEventListener, lightSensor);
                    }

                    //Calling batteryLoger function
                    date = new Date();
                    try {
                        batteryUsage = lightSensor.getPower();
                        batteryLoger("Light", batteryUsage, lightStartTime, date);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else{
                    //recording continuous
                    Log.d("test", "Continue");
                    date=new Date();
                    lightStartTime = date;
                    if(accStarted)
                        lightSensorManager.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    statusTextView.setText("Recording Continuing");
                }
            }
        });

        final Button startlightAllBtn=(Button)getActivity().findViewById(R.id.btnlightStartAll);

        final Button stoplightAllBtn=(Button)getActivity().findViewById(R.id.btnlightStopAll);
        logger = new Logger();
        startlightAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                startLightRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).show();

            }
        });
        stoplightAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                stopLightRecordingAll();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).setCancelable(false).show();

            }
        });

        // For sound applications
        final Button startsoundAllBtn=(Button)getActivity().findViewById(R.id.btnsoundStartAll);

        final Button stopsoundAllBtn=(Button)getActivity().findViewById(R.id.btnsoundStopAll);
        logger = new Logger();

        startsoundAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                getActivity().findViewById(R.id.btnsoundStartAll).setEnabled(false);
                                getActivity().findViewById(R.id.btnsoundPause).setEnabled(true);
                                getActivity().findViewById(R.id.btnsoundStopAll).setEnabled(true);
                                statusTextView.setText("Recording in Progress");
                                wakeLock.acquire();

                                if (mRecorder == null) {
                                    mRecorder = new MediaRecorder();
                                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                                    mRecorder.setOutputFile("/dev/null");
                                    try {
                                        mRecorder.prepare();
                                        mRecorder.start();
                                    } catch (IOException | IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }

                                t.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        double rec = 0.0;
                                        if (mRecorder != null) {
                                            rec = 20 * Math.log(mRecorder.getMaxAmplitude() / 2700.0);
                                            if (rec < 0.0)
                                                rec = 0.0;
                                        }
                                        logger.addRecordToLog(rec);
                                    }
                                }, 0, 1000);
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {


                                        try{
                                            t.cancel();
                                        }
                                        catch (Exception e){}
                                    }
                                }, ((1000 * 60) /2)); //30 sec
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).show();

            }
        });

        stopsoundAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes Button Clicked
                                try {
                                    if (mRecorder != null) {
                                        mRecorder.stop();
                                        mRecorder.release();
                                        mRecorder = null;
                                    }
                                    t.cancel();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                }
                                getActivity().findViewById(R.id.btnsoundStartAll).setEnabled(true);
                                getActivity().findViewById(R.id.btnsoundPause).setEnabled(false);
                                getActivity().findViewById(R.id.btnsoundStopAll).setEnabled(false);
                                statusTextView.setText("");
                                wakeLock.release();

                                date=new Date();
                                if(((ToggleButton)getActivity().findViewById(R.id.btnsoundPause)).isChecked()) {
                                    ((ToggleButton) getActivity().findViewById(R.id.btnsoundPause)).setChecked(false);
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you Sure").setPositiveButton("Yes",dialogListener).setNegativeButton("No",dialogListener).setCancelable(false).show();

            }
        });
    }

    private void initiateLandMarks(){
        landmark.put(bumperStr,0);
        landmark.put(busstopStr,0);
        landmark.put(potholeStr,0);
        landmark.put(busyroadStr,0);
        landmark.put(brokeroadStr,0);
        landmark.put(junctionStr,0);
        landmark.put(turnStr,0);
        landmark.put(islandStr,0);
        landmark.put(overtakeStr,0);
        landmark.put(dummyStr,0);
    }

    /**
     * Lists the available sensors on the phone.
     */
    private void checkAvailableSensors(){
        String sensors="" + System.lineSeparator();
        SensorManager testManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if(testManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensors += "Accelerometer" + System.lineSeparator();
        }
        if(testManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensors += "Linear Accelerometer" + System.lineSeparator();
        }
        if(testManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensors += "Gyroscope" + System.lineSeparator();
        }
        if(testManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensors += "Compass" + System.lineSeparator();
        }
        if (testManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sensors += "Light" + System.lineSeparator();
        }
        //detailsTextView.setText("Available Sensors:"+sensors);
        ShowMessage.ShowMessage(getActivity(),"Caution","Available Sensors:"+sensors+"\nRecord will be only done if the sensor is available in your device");
    }

    private void batteryLoger(String sensor,float usedMA,Date start,Date stop){

        String fileName="BATTERY_CONSUMPTION.txt";
        long timeB1,timeB2;
        timeB1=start.getTime();
        timeB2=stop.getTime();
        folder= new File(Environment.getExternalStorageDirectory()+"/"+appFolderName);
        final File batteryLogFile =new File(folder,fileName);

        if(!batteryLogFile.exists()){
            try{
                fosBatteryLog=new FileOutputStream(batteryLogFile);
                fosBatteryLog.write("#sensor,startTime,stopTime,usageConst,batteryUsage".getBytes());
            }catch (FileNotFoundException e2) {
                e2.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            try{
                fosBatteryLog=new FileOutputStream(batteryLogFile, true);
            }catch(FileNotFoundException ex){
                ex.printStackTrace();
            }
        }
        long timeDiff=stop.getTime()-start.getTime();
        float timeDiffInHr=(float)(timeDiff*0.000000278);
        float timeDiffInSec=(float)(timeDiff*0.001);
        float usage=usedMA*timeDiffInHr;

        String logDetails="\n"+sensor+","+new Timestamp(timeB1).toString()+","+new Timestamp(timeB2).toString()+","+usedMA+","+usage;
        try{
            fosBatteryLog.write(logDetails.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean isAnyOptionChecked(){
        if ((checkACC.isChecked() || checkLACC.isChecked() || checkCOM.isChecked() || checkGYR.isChecked() ||checkGPS.isChecked()) ||checkGSM.isChecked() || checkLight.isChecked()){
            return true;
        }else {
            return false;
        }
    }

    /**
     * Start recording the light data
     */
    public void startLightRecordingAll() {
        folder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName);
        boolean folder_exists = true;
        if (isAnyOptionChecked()) {
            if (!folder.exists()) {
                folder_exists = folder.mkdir();
            }
            if (folder_exists) {
                boolean subfolder_exists=true;
                date=new Date();
                time=date.getTime();
                timestamp=new Timestamp(time);
                timestampStr=timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');
                String subFolderName="DATA_"+timestampStr;

                locationManager =(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if(checkGPS.isChecked() && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    ShowMessage.ShowMessage(getActivity(),"Warning..!","Your GPS is disabled. Please Enable GPS and try again.");
                }
                else {
                    subfolder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName + "/" + subFolderName);
                    if (!subfolder.exists()) {
                        subfolder_exists = subfolder.mkdir();
                    }
                    if (subfolder_exists) {
                        getActivity().findViewById(R.id.btnlightStartAll).setEnabled(false);
                        getActivity().findViewById(R.id.btnlightPause).setEnabled(true);
                        getActivity().findViewById(R.id.btnlightStopAll).setEnabled(true);
                        statusTextView.setText("Recording in Progress");
                        wakeLock.acquire();

                        if (checkLight.isChecked()) {
                            lightStartRecord();
                        }
                    }
                    else{
                        ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
                    }
                }
            }
            else{
                ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
            }
        }
        else{
            ShowMessage.ShowMessage(getActivity(),"Caution..!","Please Check at least one Option to Record");
        }

    }

    public void startRecordingAll(){
        folder= new File(Environment.getExternalStorageDirectory()+"/"+appFolderName);
        boolean folder_exists=true;

        if(isAnyOptionChecked()){
            if(!folder.exists()){
                folder_exists=folder.mkdir();
            }
            if(folder_exists){
                boolean subfolder_exists=true;
                date=new Date();
                time=date.getTime();
                timestamp=new Timestamp(time);
                timestampStr=timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');

                String subFolderName="DATA_"+timestampStr;

                locationManager =(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if(checkGPS.isChecked() && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    ShowMessage.ShowMessage(getActivity(),"Warning..!","Your GPS is disabled. Please Enable GPS and try again.");
                } else {
                    subfolder=new File(Environment.getExternalStorageDirectory()+"/"+appFolderName+"/"+subFolderName);
                    if(!subfolder.exists()){
                        subfolder_exists=subfolder.mkdir();
                    }
                    if(subfolder_exists){
                        getActivity().findViewById(R.id.btnStartAll).setEnabled(false);
                        getActivity().findViewById(R.id.btnPause).setEnabled(true);
                        getActivity().findViewById(R.id.btnStopAll).setEnabled(true);
                        statusTextView.setText("Recording in Progress");
                        wakeLock.acquire();
                        //Getting GPS Data and writing into a file
                        //getActivity().findViewById(R.id.btnGpsStart).setEnabled(false);

                        if(checkGPS.isChecked())
                            gpsStartRecord();

                        //Getting AcceleroMeter Data and writing to file
                        //getActivity().findViewById(R.id.btnAccStart).setEnabled(false);
                        if(checkACC.isChecked())
                            accStartRecord();

                        //Getting Linear AcceleroMeter Data and writing to file
                        //getActivity().findViewById(R.id.btnLaccStart).setEnabled(false);
                        if(checkLACC.isChecked())
                            laccStartRecord();

                        //Getting Compass Data and writing to file
                        //getActivity().findViewById(R.id.btnComStart).setEnabled(false);
                        if(checkCOM.isChecked())
                            comStartRecord();

                        //Getting GYROSCOPE data and writing it to file
                        //getActivity().findViewById(R.id.btnGyrStart).setEnabled(false);
                        if(checkGYR.isChecked())
                            gyrStartRecord();

                        if(checkGSM.isChecked())
                            gsmStartRecord();

                        if(checkWiFi.isChecked())
                            wifiStartRecord();
                    }
                    else{
                        ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
                    }
                }

            }else{
                ShowMessage.ShowMessage(getActivity(),"Failed..!","Failed to create Folder for Application.\nPlease retry.");
                //errorTag.setText("Failed to create Folder for application");
            }

        }else{
            ShowMessage.ShowMessage(getActivity(),"Caution..!","Please Check at least one Option to Record");
        }

    }

    private void gsmStartRecord() {
        String gsmFilename="GSM_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File gsmFile=new File(subfolder,gsmFilename);

        mTelManager=(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        String carrierName=mTelManager.getNetworkOperatorName();
        try {
            fosGSM=new FileOutputStream(gsmFile);
            fosGSM.write((carrierName + "\n#signalStrength,time").getBytes());
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSignalListener=new PhoneStateListener(){

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                int gsmStrength=signalStrength.getGsmSignalStrength();

                long systemTimeInMilli=(new Date()).getTime();
                String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));
                marker="";
                for(String key: landmark.keySet())
                {
                    if(landmark.get(key)!=0)
                    {
                        marker+=key+"_"+landmark.get(key)+"+";
                    }
                }
                String gsmSignalDetails="\n"+gsmStrength+","+timestampFormatted+","+marker;
                try{
                    fosGSM.write(gsmSignalDetails.getBytes());
                }catch(Exception e){
                    e.printStackTrace();
                }
                super.onSignalStrengthsChanged(signalStrength);
            }

        };
        mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        gsmStarted=true;
    }

    private void wifiStartRecord(){

        String wifiFileName;
        date=new Date();
        time=date.getTime();
        timestamp=new Timestamp(time);
        timestampStr=timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_');
        wifiFileName="WiFi_"+timestampStr+".txt";
        File WifiLog =new File(subfolder,wifiFileName);
        try {
            fosWiFi=new FileOutputStream(WifiLog);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiStarted=true;
    }
    private void gpsStartRecord(){

        String gpsFilename="GPS_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        File gpsFile=new File(subfolder,gpsFilename);
        //locationManager =(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        /*if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //getActivity().findViewById(R.id.btnGpsStart).setEnabled(true);
            ShowMessage.ShowMessage(getActivity(),"Alert..!","Your GPS is disabled. GPS Recording wont be done.\n Restart after enabling GPS if you want to record GPS data");
        } else {*/

        try{
            fosGPS=new FileOutputStream(gpsFile);
            fosGPS.write("#lat,long,speed,altitude,time".getBytes());
            locationListener=new LocationListener(){

                @Override
                public void onLocationChanged(Location location) {
                    //float accuracy=location.getAccuracy();
                    double altitude=location.getAltitude();
                    double latitude=location.getLatitude();
                    double longitude=location.getLongitude();
                    double speed=location.getSpeed();

                        /*long time=location.getTime();
                        Date date1=new Date(time);
                        SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm:ss");
                        String timeStamp=dateFormat.format(date1);
                        **/

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }

                    String locDetails="\n"+latitude+","+longitude+","+speed+","+altitude+","+timestampFormatted+","+marker;

                    try {
                        fosGPS.write(locDetails.getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                    ShowMessage.ShowMessage(getActivity(),"Alert..!","GPS is got disabled. GPS Recording will be stopped");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }

            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            gpsStarted=true;
        } catch(Exception e){
            e.printStackTrace();
        }
        /*}*/
    }

    private void accStartRecord(){
        String accFilename="ACC_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File accFile=new File(subfolder,accFilename);

        try {
            fosACC=new FileOutputStream(accFile);
            fosACC.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e2) {

            e2.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        accSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accSensor=accSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        date=new Date();
        accStartTime=date;
        if(accSensor!=null){
            accSensorEventListener = new SensorEventListener(){
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub
                }
                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //float timestamp=event.timestamp/1000000;
                    //long timeInMilli=(new Date()).getTime()+((event.timestamp-System.nanoTime())/1000000L);

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }
                    String accSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;
                    try{
                        fosACC.write(accSensorDetails.getBytes());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            };
            accSensorManager.registerListener(accSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            accStarted=true;
        }
    }

    private void laccStartRecord(){
        String laccFilename="LACC_"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File laccFile=new File(subfolder,laccFilename);

        try {
            fosLACC=new FileOutputStream(laccFile);
            fosLACC.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e2) {

            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        laccSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        laccSensor=laccSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        date=new Date();
        laccStartTime=date;

        if(laccSensor!=null){

            laccSensorEventListener = new SensorEventListener(){

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //long timestamp=event.timestamp/1000000;
                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }
                    String laccSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;

                    try{

                        fosLACC.write(laccSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }


            };
            laccSensorManager.registerListener(laccSensorEventListener, laccSensor, SensorManager.SENSOR_DELAY_NORMAL);
            laccStarted=true;
        }
    }

    private void comStartRecord(){
        String comFilename="COM"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File comFile=new File(subfolder,comFilename);

        try {
            fosCOM=new FileOutputStream(comFile);
            fosCOM.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e2) {

            e2.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        comSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        comSensor=comSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        date=new Date();
        comStartTime=date;

        if(comSensor!=null){

            comSensorEventListener = new SensorEventListener(){

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //long timestamp=event.timestamp/1000000;

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }
                    String comSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;

                    try{

                        fosCOM.write(comSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }


            };
            comSensorManager.registerListener(comSensorEventListener, comSensor, SensorManager.SENSOR_DELAY_NORMAL);
            comStarted=true;

        }
    }

    private void gyrStartRecord(){
        String gyrFilename="GYR"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File gyrFile=new File(subfolder,gyrFilename);
        try {
            fosGYR=new FileOutputStream(gyrFile);
            fosGYR.write("#x,y,z,time".getBytes());
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        gyrSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        gyrSensor=gyrSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        date=new Date();
        gyrStartTime=date;

        if(gyrSensor!=null){

            gyrSensorEventListener = new SensorEventListener(){

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onSensorChanged(SensorEvent event) {

                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    //float timestamp=event.timestamp/1000000000;

                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";
                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }

                    String gyrSensorDetails="\n"+x+","+y+","+z+","+timestampFormatted+","+marker;

                    try{

                        fosGYR.write(gyrSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }


            };
            gyrSensorManager.registerListener(gyrSensorEventListener, gyrSensor, SensorManager.SENSOR_DELAY_NORMAL);
            gyrStarted=true;
        }
    }

    /**
     * Light record
     */
    private void lightStartRecord() {
        String lgtFilename="LIGHT"+timestamp.toString().replace(' ', '_').replace('-', '_').replace(':', '_').replace('.', '_')+".txt";
        final File gyrFile=new File(subfolder,lgtFilename);
        try {
            fosLGT=new FileOutputStream(gyrFile);
            fosLGT.write("#x,time".getBytes());
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        lightSensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor=lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        date=new Date();
        lightStartTime = date;

        if (lightSensor != null) {
            lightSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float x = event.values[0];
                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    marker="";

                    for(String key: landmark.keySet())
                    {
                        if(landmark.get(key)!=0)
                        {
                            marker+=key+"_"+landmark.get(key)+"+";
                        }
                    }

                    String lightSensorDetails="\n"+x+","+timestampFormatted+","+marker;

                    try{

                        fosLGT.write(lightSensorDetails.getBytes());


                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }

            };

            lightSensorManager.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            lightStarted = true;
        }
    }

    /**
     *  Stop Light Recordings
     */
    public void stopLightRecordingAll() {
        getActivity().findViewById(R.id.btnlightStartAll).setEnabled(true);
        getActivity().findViewById(R.id.btnlightPause).setEnabled(false);
        getActivity().findViewById(R.id.btnlightStopAll).setEnabled(false);
        statusTextView.setText("");
        wakeLock.release();

        date=new Date();
        if(((ToggleButton)getActivity().findViewById(R.id.btnlightPause)).isChecked()){
            ((ToggleButton)getActivity().findViewById(R.id.btnlightPause)).setChecked(false);
        }

        if(lightStarted){
            try{
                lightSensorManager.unregisterListener(lightSensorEventListener);
                fosLGT.close();

                batteryUsage=lightSensor.getPower();
                batteryLoger("LIGHT", batteryUsage, lightStartTime, date);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }

    }

    public void stopRecordingAll(){
        getActivity().findViewById(R.id.btnStartAll).setEnabled(true);
        getActivity().findViewById(R.id.btnPause).setEnabled(false);
        getActivity().findViewById(R.id.btnStopAll).setEnabled(false);
        statusTextView.setText("");
        wakeLock.release();

        initiateLandMarks();

        date=new Date();
        if(((ToggleButton)getActivity().findViewById(R.id.btnPause)).isChecked()){
            ((ToggleButton)getActivity().findViewById(R.id.btnPause)).setChecked(false);
        }

        if(gpsStarted){
            try {
                locationManager.removeUpdates(locationListener);

                fosGPS.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        if(accStarted){
            try{
                accSensorManager.unregisterListener(accSensorEventListener);
                fosACC.close();

                batteryUsage=accSensor.getPower();
                batteryLoger("ACC", batteryUsage, accStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(laccStarted){
            try{
                laccSensorManager.unregisterListener(laccSensorEventListener);
                fosLACC.close();

                batteryUsage=laccSensor.getPower();
                batteryLoger("LACC", batteryUsage, laccStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(gyrStarted){
            try{
                gyrSensorManager.unregisterListener(gyrSensorEventListener);
                fosGYR.close();
                batteryUsage=gyrSensor.getPower();
                batteryLoger("GYR", batteryUsage, gyrStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(comStarted){
            try{
                comSensorManager.unregisterListener(comSensorEventListener);
                fosCOM.close();

                batteryUsage=comSensor.getPower();
                batteryLoger("COM", batteryUsage, comStartTime, date);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(gsmStarted){
            try{
                mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_NONE);
                fosGSM.close();

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        if(wifiStarted){
            try{
                getActivity().unregisterReceiver(receiverWifi);
                fosWiFi.close();

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    class LMButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            int buttonId=v.getId();
            switch (buttonId){
                case R.id.btnLM1:{
                    if(fbumper==0){
                        bumperBtn.setTextColor(Color.RED);
                        bumperImg.setImageResource(R.drawable.btn_bumper1);
                        fbumper=1;
                        cbumper++;
                        landmark.put(bumperStr,cbumper);
                    }else{
                        bumperBtn.setTextColor(Color.BLACK);
                        bumperImg.setImageResource(R.drawable.btn_bumper0);
                        fbumper=0;
                        landmark.put(bumperStr,0);
                    }
                    break;
                }
                case R.id.btnLM2:{
                    if(fpothole==0){
                        potholeBtn.setTextColor(Color.RED);
                        potholeImg.setImageResource(R.drawable.btn_pothole1);
                        fpothole=1;
                        cpothole++;
                        landmark.put(potholeStr,cpothole);
                    }else{
                        potholeBtn.setTextColor(Color.BLACK);
                        potholeImg.setImageResource(R.drawable.btn_pothole0);
                        fpothole=0;
                        landmark.put(potholeStr,0);
                    }
                    break;
                }
                case R.id.btnLM3:{
                    if(fbusStop==0){
                        busStopBtn.setTextColor(Color.RED);
                        busStopImg.setImageResource(R.drawable.btn_busstop1);
                        fbusStop=1;
                        cbusStop++;
                        landmark.put(busstopStr,cbusStop);
                    }else{
                        busStopBtn.setTextColor(Color.BLACK);
                        busStopImg.setImageResource(R.drawable.btn_busstop0);
                        fbusStop=0;
                        landmark.put(busstopStr,0);
                    }
                    break;
                }
                case R.id.btnLM4:{
                    if(fjunction==0){
                        junctionBtn.setTextColor(Color.RED);
                        junctionImg.setImageResource(R.drawable.btn_junction1);
                        fjunction=1;
                        cjunction++;
                        landmark.put(junctionStr,cjunction);
                    }else{
                        junctionBtn.setTextColor(Color.BLACK);
                        junctionImg.setImageResource(R.drawable.btn_junction0);
                        fjunction=0;
                        landmark.put(junctionStr,0);
                    }
                    break;
                }
                case R.id.btnLM5:{
                    if(fbusyRoad==0){
                        busyRoadBtn.setTextColor(Color.RED);
                        busyRoadImg.setImageResource(R.drawable.btn_busyroad1);
                        fbusyRoad=1;
                        cbusyRoad++;
                        landmark.put(busyroadStr,cbusyRoad);
                    }else{
                        busyRoadBtn.setTextColor(Color.BLACK);
                        busyRoadImg.setImageResource(R.drawable.btn_busyroad0);
                        fbusyRoad=0;
                        landmark.put(busyroadStr,0);
                    }
                    break;
                }
                case R.id.btnLM6:{
                    if(fbrokeRoad==0){
                        brokeRoadBtn.setTextColor(Color.RED);
                        brokeRoadImg.setImageResource(R.drawable.btn_brokeroad1);
                        fbrokeRoad=1;
                        cbrokeRoad++;
                        landmark.put(brokeroadStr,cbrokeRoad);
                    }else{
                        brokeRoadBtn.setTextColor(Color.BLACK);
                        brokeRoadImg.setImageResource(R.drawable.btn_brokeroad0);
                        fbrokeRoad=0;
                        landmark.put(brokeroadStr,0);
                    }
                    break;
                }
                case R.id.btnLM7:{
                    if(fturn==0){
                        turnBtn.setTextColor(Color.RED);
                        turnImg.setImageResource(R.drawable.btn_turn1);
                        fturn=1;
                        cturn++;
                        landmark.put(turnStr,cturn);
                    }else{
                        turnBtn.setTextColor(Color.BLACK);
                        turnImg.setImageResource(R.drawable.btn_turn0);
                        fturn=0;
                        landmark.put(turnStr,0);
                    }
                    break;
                }

                case R.id.btnLM8:{
                    if(f_overTake==0){
                        overTakeBtn.setTextColor(Color.RED);
                        overTakeImg.setImageResource(R.drawable.btn_overtake1);
                        f_overTake=1;
                        c_overTake++;
                        landmark.put(overtakeStr,c_overTake);
                    }else{
                        overTakeBtn.setTextColor(Color.BLACK);
                        overTakeImg.setImageResource(R.drawable.btn_overtake0);
                        f_overTake=0;
                        landmark.put(overtakeStr,0);
                    }
                    break;
                }
                case R.id.btnLM9:{
                    if(f_island==0){
                        islandBtn.setTextColor(Color.RED);
                        islandImg.setImageResource(R.drawable.btn_islend1);
                        f_island=1;
                        c_island++;
                        landmark.put(islandStr,c_island);
                    }else{
                        islandBtn.setTextColor(Color.BLACK);
                        islandImg.setImageResource(R.drawable.btn_islend0);
                        f_island=0;
                        landmark.put(islandStr,0);
                    }
                    break;
                }
                case R.id.btnLM10:{
                    if(f_dummy==0){
                        dummyBtn.setTextColor(Color.RED);
                        dummyImg.setImageResource(R.drawable.btn_dummy1);
                        f_dummy=1;
                        c_dummy++;
                        landmark.put(dummyStr,c_dummy);
                    }else{
                        dummyBtn.setTextColor(Color.BLACK);
                        dummyImg.setImageResource(R.drawable.btn_dummy0);
                        f_dummy=0;
                        landmark.put(dummyStr,0);
                    }
                    break;
                }
            }
        }
    }
    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List wifiList = mainWifi.getScanResults();

            for (int i = 0; i < wifiList.size(); i++) {
                ScanResult scanResult = (ScanResult)wifiList.get(i);
// I have to put a try catch block here otherwise it returns an exception.
                Toast.makeText(getActivity(), scanResult.BSSID, Toast.LENGTH_SHORT).show();
                try{
                    long systemTimeInMilli=(new Date()).getTime();
                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));

                    fosWiFi.write((scanResult.BSSID+","+scanResult.SSID+","+scanResult.level+","+timestampFormatted+"\n").getBytes());

                }
                catch(Exception e){

                }
            }
        }
    }
}
