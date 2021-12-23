package com.disarm.testapp_newdesing01;

import android.app.DialogFragment;
import android.app.Fragment;
//import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.InputMismatchException;

/**
 * Created by Surjith on 10-04-2015.
 */
public class BusFinder extends Fragment{

    String LogTag="Surji",sourTag="SOURCE",destTag="DEST";
    GoogleMap map;
    EditText txtSour;
    EditText txtDest;
    EditText txtTime;
    Button btnFind;
    Button btnReset;
    TextView txtProgress;
    SelectItemFragment listFragment=new SelectItemFragment();

    double sLat,sLng,dLat,dLng;

    Marker sourceMarker=null,destMarker=null;

    //HttpClient httpClient;
    //HttpGet httpGet;


    int FLAG=1;
    final LatLng dgpLatLng= new LatLng(23.5446116667,87.3021116667);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        txtSour=(EditText)getActivity().findViewById(R.id.txtSour);
        txtDest=(EditText)getActivity().findViewById(R.id.txtDest);
        txtTime=(EditText)getActivity().findViewById(R.id.txtTime);



        btnFind=(Button)getActivity().findViewById(R.id.btnFind);
        btnReset=(Button)getActivity().findViewById(R.id.btnReset);
        txtProgress=(TextView) getActivity().findViewById(R.id.txtProgress);
        //map=((MapFragment)getActivity().getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        map=getMapFragment().getMap();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(dgpLatLng,12));


        /*Marker dgp=map.addMarker(new MarkerOptions()
            .position(dgpLatLng)
            .title("Durgapur"));*/

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                FLAG=FLAG*-1;//if FLAG=-1 then result is source, if FLAG is  1 result is for destination

                //Location stopLocation=new Location(latLng.toString());
                String strFLAG=null;
                if(FLAG==-1){
                    strFLAG=sourTag;
                    if(sourceMarker!=null){
                        Log.d(LogTag,"Comes inside SourceMarker if");
                        sourceMarker.remove();
                        map.clear();
                        txtSour.setText("");
                        txtDest.setText("");
                    }
                    sourceMarker=map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Source")
                            .snippet(latLng.latitude + ", " + latLng.longitude));
                }else if(FLAG==1) {
                    strFLAG = destTag;
                    if(destMarker!=null){
                        Log.d(LogTag,"Comes insider DestMarker if");
                        destMarker.remove();
                        //map.clear();
                    }

                    destMarker=map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Destination")
                            .snippet(latLng.latitude+", "+latLng.longitude));
                }
                if(latLng!=null && strFLAG!=null){
                    //startIntentService(resultReceiver,stopLocation,strFLAG);
                    map.addMarker(new MarkerOptions().position(latLng).title(strFLAG));
                    double latitude=latLng.latitude;
                    double longitude=latLng.longitude;
                    if(strFLAG.equals(sourTag)){
                        sLat=latitude;
                        sLng=longitude;
                    }else if(strFLAG.equals(destTag)){
                        dLat=latitude;
                        dLng=longitude;
                    }


                    FetchAddress fetchAddress=new FetchAddress();
                    fetchAddress.getAddressFromLocation(latitude,longitude,getActivity(),new GeocoderHandler(),strFLAG);
                }else{
                    Toast.makeText(getActivity(),"On GPS", Toast.LENGTH_SHORT).show();
                }
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });

        /**
        txtSour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] vals={"one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen"};
                Bundle bundle=new Bundle();
                bundle.putStringArray("vals",vals);
                try {
                    listFragment.setArguments(bundle);
                    (getActivity().findViewById(R.id.section01)).setClickable(false);
                    (getActivity().findViewById(R.id.section02)).setClickable(false);
                    (getActivity().findViewById(R.id.section03)).setClickable(false);
                    (getActivity().findViewById(R.id.btnReset)).setEnabled(false);
                    (getActivity().findViewById(R.id.btnFind)).setEnabled(false);
                    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                    //FragmentTransaction ft=getChildFragmentManager().beginTransaction();
                    ft.replace(R.id.busfinder_layout, listFragment, sourTag);
                    ft.addToBackStack(null);
                    ft.commit();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        txtDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] vals={"one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen"};
                Bundle bundle=new Bundle();
                bundle.putStringArray("vals",vals);
                try {
                    listFragment.setArguments(bundle);
                    (getActivity().findViewById(R.id.section01)).setClickable(false);
                    (getActivity().findViewById(R.id.section02)).setClickable(false);
                    (getActivity().findViewById(R.id.section03)).setClickable(false);
                    (getActivity().findViewById(R.id.btnReset)).setEnabled(false);
                    (getActivity().findViewById(R.id.btnFind)).setEnabled(false);


                    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                    //FragmentTransaction ft=getChildFragmentManager().beginTransaction();
                    ft.replace(R.id.busfinder_layout, listFragment, destTag);
                    ft.addToBackStack(null);
                    ft.commit();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
         ***/

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(getActivity(),"FindButton Clicked",Toast.LENGTH_SHORT).show();
                if (txtSour.getText().toString().equals("") || txtDest.getText().toString().equals("")) {
                    ShowMessage.ShowMessage(getActivity(),"Warning", "Please Provide Source and Destination");
                }else{
                    String sour=txtSour.getText().toString().replace(" ", "_").replace(",", "_");
                    String dest=txtDest.getText().toString().replace(" ","_").replace(",","_");
                    String time="nil";
                    if(!(txtTime.getText().toString().equals("")))
                        time=txtTime.getText().toString();

                    requestFromServer(sour, dest,time);
                    resetTextFields();
                }
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                FLAG=1;
                resetTextFields();
            }
        });
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker=new TimePickerFragment();
                timePicker.show(getActivity().getFragmentManager(),"Pick Time");
            }
        });
    }

    private void requestFromServer(String source, String destination, String time) {
        String url="http://192.168.1.12/getBusDetails.php?var1="+sLat+"+&var2="+sLng+"&var3="+dLat+"+&var4="+dLng+"&var5="+time;

        /***CODE WHICH MAY NEEDED LATER
         * protected String addLocationToUrl(String url){
         if(!url.endsWith("?"))
         url += "?";

         List<NameValuePair> params = new LinkedList<NameValuePair>();

         if (lat != 0.0 && lon != 0.0){
         params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
         params.add(new BasicNameValuePair("lon", String.valueOf(lon)));
         }

         if (address != null && address.getPostalCode() != null)
         params.add(new BasicNameValuePair("postalCode", address.getPostalCode()));
         if (address != null && address.getCountryCode() != null)
         params.add(new BasicNameValuePair("country",address.getCountryCode()));

         params.add(new BasicNameValuePair("user", agent.uniqueId));

         String paramString = URLEncodedUtils.format(params, "utf-8");

         url += paramString;
         return url;
         }

         */


        String msgString="Source:"+source+",LatLng:"+sLat+", "+sLng+"\nDest:"+destination+",LatLng:"+dLat+", "+dLng+"\nTime:"+time;
        ShowMessage.ShowMessage(getActivity(),"Details",msgString);
        new GetDataFromServer().execute(url);
        //ShowMessage.ShowMessage(getActivity(),"From Server","Source:"+source+"\nDestination:"+destination+"\nTime:"+time);
    }

    private void resetTextFields(){
        txtDest.setText("");
        txtSour.setText("");
        txtTime.setText("");
    }

    private MapFragment getMapFragment(){
        FragmentManager fm;
        Log.d(LogTag,"SDK: "+ Build.VERSION.SDK_INT);
        Log.d(LogTag,"Release: "+Build.VERSION.RELEASE);

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            Log.d(LogTag,"Using getFragmentManager()");
            fm=getFragmentManager();
        }else{
            Log.d(LogTag,"Using getChildFragmentManager()");
            fm=getChildFragmentManager();
        }
        return (MapFragment) fm.findFragmentById(R.id.map_fragment);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.busfinder_layout,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        FLAG=1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MapFragment mf=(MapFragment)getActivity().getFragmentManager().findFragmentById(R.id.map_fragment);
        try{
            if(mf!=null){
                getActivity().getFragmentManager().beginTransaction().remove(mf).commit();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        resetTextFields();
    }


    private class GeocoderHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            String locationAddress="";
            switch (msg.what){
                case 1:
                    Bundle bundle=msg.getData();
                    /*String[] result=bundle.getStringArray("address");

                    for(int i=0;i<result.length;i++){
                        locationAddress=locationAddress+"\n"+result[i];
                    }*/
                    try{
                        listFragment.setArguments(bundle);
                        (getActivity().findViewById(R.id.section01)).setClickable(false);
                        (getActivity().findViewById(R.id.section02)).setClickable(false);
                        (getActivity().findViewById(R.id.section03)).setClickable(false);

                        FragmentTransaction ft=getActivity().getFragmentManager().beginTransaction();
                        //FragmentTransaction ft=getChildFragmentManager().beginTransaction();
                        ft.replace(R.id.busfinder_layout, listFragment, bundle.getString("addressFor"));
                        ft.addToBackStack(null);
                        ft.commit();
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }


                    //ShowMessage.ShowMessage(getActivity(),"Addresses: "+locationAddress);
                    break;
                case 0:
                    ShowMessage.ShowMessage(getActivity(),"Sorry..!","No Address Received.\n Please Try Again.");
                    FLAG=FLAG*-1;
                    break;
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Stream Exception", Toast.LENGTH_SHORT).show();
        }
        return total.toString();
    }

    private class GetDataFromServer extends AsyncTask<String,TextView,String> {

        private Exception exception;
        HttpClient httpClient;
        HttpGet httpGet;

        @Override
        protected String doInBackground(String... params) {
            httpClient=new DefaultHttpClient();
            httpGet=new HttpGet(params[0]);
            //httpGet=new HttpGet("http://example.com/script.php?var1=androidprogramming");
            try{
                HttpResponse httpResponse=httpClient.execute(httpGet);
                if(httpResponse!=null){
                    String line="";
                    InputStream inputStream=httpResponse.getEntity().getContent();
                    line=convertStreamToString(inputStream);
                    return line;
                }else {
                    return "Unable to complete your request";
                }

            }catch (ClientProtocolException ex){
                ex.printStackTrace();
                return "Failed";
            }catch (IOException ex){
                //Toast.makeText(getActivity(),"IOException",Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                return "Failed";
            }catch (Exception ex){
                //Toast.makeText(getActivity(),"Some Exception",Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                return "Failed";
            }
            //return "Failed";
        }

        @Override
        protected void onProgressUpdate(TextView... values) {
            txtProgress.setText("Progressing");
        }

        @Override
        protected void onPostExecute(String s) {
            map.clear();
            BitmapDescriptor icon= BitmapDescriptorFactory.fromResource(R.drawable.mark_busstop1);

            String latLngs="";
            String test="";
            String[] stops;
            String[] stopDetails;
            LatLng position;
            float waitTime;
            int length=s.length();
            if(s.charAt(0)=='@'){
                s=s.substring(1,length);
                stops=s.split("@");
                for(int i=0;i<stops.length;i++){
                    stopDetails=stops[i].split(",");
                    //stopDetails[0]:stop name, [1]:lat, [2]:lng,[3]:type,[4]:waiting time
                    position= new LatLng(Double.parseDouble(stopDetails[1]),Double.parseDouble(stopDetails[2]));
                    waitTime=(float)Integer.parseInt(stopDetails[4])/60;

                    map.addMarker(new MarkerOptions()
                            .position(position)
                            .title(stopDetails[0])
                            .snippet("TypeOfStop:" + stopDetails[3] + "\nWaitingTime" + waitTime)
                            .icon(icon)
                    );
                }
                //ShowMessage.ShowMessage(getActivity(),"Result",test);
            }
            //Toast.makeText(getActivity(),test,Toast.LENGTH_SHORT).show();

        }
    }
}
