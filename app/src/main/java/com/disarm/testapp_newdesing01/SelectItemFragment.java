package com.disarm.testapp_newdesing01;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.disarm.testapp_newdesing01.dummy.DummyContent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SelectItemFragment extends ListFragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private final String logTag="ListFragment";



    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters

    public static SelectItemFragment newInstance(String[] param1) {
        SelectItemFragment fragment = new SelectItemFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SelectItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS));*/
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(logTag,"OnDestroy has called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(logTag,"OnDestroyVIew has called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String[] vals=this.getArguments().getStringArray("vals");
        List<String> stops=new ArrayList<String>();
        for(int i=0;i<vals.length;i++){
            stops.add(vals[i]);
        }
       this.setListAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,stops));
        //ArrayAdapter adapter=ArrayAdapter.createFromResource(getActivity(),R.array.stops,android.R.layout.simple_list_item_1);
        //setListAdapter(adapter);
        return inflater.inflate(R.layout.list_layout1,container,false);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener ) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(this.getTag(),l.getItemAtPosition(position).toString());
        }
        finishList();
    }

    private void finishList() {
        getActivity().getFragmentManager().popBackStack();
        (getActivity().findViewById(R.id.section01)).setClickable(true);
        (getActivity().findViewById(R.id.section02)).setClickable(true);
        (getActivity().findViewById(R.id.section03)).setClickable(true);
        (getActivity().findViewById(R.id.btnReset)).setEnabled(true);
        (getActivity().findViewById(R.id.btnFind)).setEnabled(true);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        String[] address=null;
        public void onFragmentInteraction(String tag,String value);

    }

}
