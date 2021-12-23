package com.disarm.testapp_newdesing01;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.util.Log;
import android.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.disarm.testapp_newdesing01.Extras.subfolder;
import static com.disarm.testapp_newdesing01.ModeOfTransport.getMode;

/**
 * Created by disarm on 7/9/16.
 */
public class Rating extends DialogFragment  {
    RatingBar ratingBar;
    private FileOutputStream fosrate;

    public static Rating newInstance(int title) {
        Rating frag = new Rating();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (getActivity()).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.frag_rating, null);
        ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Extras.rating_final = (int) ratingBar.getRating();
                        Log.v("rating", String.valueOf(Extras.rating_final));
                                try{
                                    fosrate=new FileOutputStream(Extras.rateFile, true);
                                    long systemTimeInMilli=(new Date()).getTime();
                                    String timestampFormatted=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(systemTimeInMilli));
                                    fosrate=new FileOutputStream(Extras.rateFile, true);
                                    String rate =" , "+ Extras.rating_final +" , "+ timestampFormatted+ "\n";
                                    fosrate.write((rate).getBytes());
                                }catch (FileNotFoundException e2) {
                                    e2.printStackTrace();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                    }
        );

        return builder.create();

    }


}
