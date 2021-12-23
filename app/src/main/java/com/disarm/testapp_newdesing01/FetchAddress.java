package com.disarm.testapp_newdesing01;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * Created by Surjith on 12-04-2015.
 */
public class FetchAddress {
    private static final String TAG="FetchAddress";


    private static final int MAX=3;
    public static void getAddressFromLocation(final double latitude,final double longitude,final Context context,final Handler handler,final String addressFor){
        Thread thread=new Thread(){

            @Override
            public void run() {
                boolean success=false;
                Geocoder geocoder=new Geocoder(context, Locale.getDefault());
                String[] result={null,null,null};
                try{
                    List<Address> addressList=geocoder.getFromLocation(latitude,longitude,MAX);
                    //Address[] address=new Address[MAX];
                    if(addressList!=null && addressList.size()>0){
                        for(int i=0;i<addressList.size();i++){
                            //address[i]=addressList.get(i);
                            result[i]=addressList.get(i).getAddressLine(0);
                        }
                        success=true;
                    }
                }catch (Exception ex){
                    Log.e(TAG,"Unable to Connect to Geocoder");
                }finally {
                    Message message=Message.obtain();
                    message.setTarget(handler);
                    if(success){
                        message.what=1;
                        Bundle bundle=new Bundle();
                        bundle.putStringArray("vals", result);
                        bundle.putString("addressFor",addressFor);
                        message.setData(bundle);
                    }else {
                        message.what=0;
                        Bundle bundle=new Bundle();
                        bundle.putString("addressFor",addressFor);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}
