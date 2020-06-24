package com.example.api_application_v2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;


public class HistoryFragment extends Fragment implements OnFragmentInteractionListener{

    private static HistoryFragment instance;
    RecyclerView rv;
    private OnFragmentInteractionListener mListener;
    MyAdapter ad;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    public static HistoryFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View historyView = inflater.inflate(R.layout.fragment_history, container, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
        Gson gson = new Gson();
        ArrayList<String> restaurantList;
        ArrayList<Float> ratingList;
        ArrayList<String> urlList;
        // Initialize preferences
        if(sharedPref.contains("restaurantList")) {
            String restaurant = sharedPref.getString("restaurantList", "");
            restaurantList = gson.fromJson(restaurant, ArrayList.class);
        }
        else {
            restaurantList = new ArrayList<>();
        }
        if(sharedPref.contains("ratingList")) {
            String rating = sharedPref.getString("ratingList", "");
            ratingList = gson.fromJson(rating, new TypeToken<ArrayList<Float>>(){}.getType());
        }
        else {
            ratingList = new ArrayList<>();
        }
        if(sharedPref.contains("urlList")) {
            String url = sharedPref.getString("urlList", "");
            urlList = gson.fromJson(url, ArrayList.class);
        }
        else {
            urlList = new ArrayList<>();
        }
        rv = historyView.findViewById(R.id.rv);
        ad = new MyAdapter(getActivity(),restaurantList,ratingList,urlList); //pass data into adapter
        rv.setAdapter(ad);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setHasFixedSize(true);
        return historyView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        rv.setAdapter(null);
        mListener = null;
    }
}
