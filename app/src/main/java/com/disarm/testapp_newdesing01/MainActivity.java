package com.disarm.testapp_newdesing01;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;


public class MainActivity extends ActionBarActivity implements SelectItemFragment.OnFragmentInteractionListener {

    FragmentManager fragmentManager=getFragmentManager();
    Fragment loggerFragment=new GPSAndSensorLogger();
    Fragment busfinderFragment=new BusFinder();
    Fragment extrasFragment=new Extras();

    TextView loggerTxtView,busfinderTxtView,extrasTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.extras_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        loggerTxtView=(TextView)findViewById(R.id.section01);
        busfinderTxtView=(TextView)findViewById(R.id.section02);
        extrasTxtView=(TextView)findViewById(R.id.section03);

        loggerTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));
        busfinderTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));
        extrasTxtView.setBackgroundColor(Color.parseColor("#cc6600"));
        //fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(extrasFragment, "extras");
        //fragmentTransaction.add(R.id.layoutMain, loggerFragment, "logger");
        //fragmentTransaction.replace(R.id.layoutMain,loggerFragment, String.valueOf(R.string.loggerFrag_tag));
        fragmentTransaction.replace(R.id.layoutMain,extrasFragment, String.valueOf(R.string.extrasFrag_tag));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sectionBarAction(View view){

        //TextView textView=(TextView) findViewById(R.id.mainTextView);


        switch (view.getId()){
            case R.id.section01: {

                loggerTxtView.setBackgroundColor(Color.parseColor("#cc6600"));
                busfinderTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));
                extrasTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));
                loggerTxtView.setEnabled(false);
                busfinderTxtView.setEnabled(true);
                extrasTxtView.setEnabled(true);
                //fragmentManager.popBackStack();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //fragmentTransaction.add(R.id.layoutMain, loggerFragment, "logger");
                fragmentTransaction.replace(R.id.layoutMain,loggerFragment, String.valueOf(R.string.loggerFrag_tag));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            }
            case R.id.section02: {

                loggerTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));
                busfinderTxtView.setBackgroundColor(Color.parseColor("#cc6600"));
                extrasTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));

                loggerTxtView.setEnabled(true);
                busfinderTxtView.setEnabled(false);
                extrasTxtView.setEnabled(true);
                //fragmentManager.popBackStack();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //fragmentTransaction.add(R.id.layoutMain, busfinderFragment, "busfinder");
                fragmentTransaction.replace(R.id.layoutMain,busfinderFragment, String.valueOf(R.string.busfinderFrag_tag));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();


                /*FragmentTransaction ft2=fragmentManager.beginTransaction();
                ft2.replace(R.id.layoutMain,mapFragment,"map");
                ft2.addToBackStack(null);
                ft2.commit();*/
                break;
            }
            case R.id.section03:{

                loggerTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));
                busfinderTxtView.setBackgroundColor(Color.parseColor("#ffcc66"));
                extrasTxtView.setBackgroundColor(Color.parseColor("#cc6600"));
                loggerTxtView.setEnabled(true);
                busfinderTxtView.setEnabled(true);
                extrasTxtView.setEnabled(false);
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.layoutMain,extrasFragment,String.valueOf(R.string.extrasFrag_tag));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            }
        }


    }

    private String[] getAddress(){
        String[] val={"one","Two","Three"};
        return val;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //noiseCapture.stopRecording();
    }

    @Override
    public void onFragmentInteraction(String tag,String value) {
        //Toast.makeText(this,"FragmentInteraction Called",Toast.LENGTH_SHORT).show();

        if(tag.equals("SOURCE")){
            ((EditText)findViewById(R.id.txtSour)).setText(value);
        }else if(tag.equals("DEST")){
            ((EditText)findViewById(R.id.txtDest)).setText(value);
        }
    }
}
