package com.disarm.testapp_newdesing01;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Surjith on 19-04-2015.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //use current time as the default for picker
        final Calendar c= Calendar.getInstance();
        int hour=c.get(Calendar.HOUR_OF_DAY);
        int minute=c.get(Calendar.MINUTE);

        //create new instance of time picker dialog and return it
        return new TimePickerDialog(getActivity(),this,hour,minute, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String time=hourOfDay+":"+minute;
        ((TextView)(getActivity().findViewById(R.id.txtTime))).setText(time);
    }
}
