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


public class BlockedFragment extends Fragment implements OnFragmentInteractionListener{

    private static BlockedFragment instance;
    RecyclerView rv;
    private OnFragmentInteractionListener mListener;
    BlockedAdapter ad;

    public BlockedFragment() {
        // Required empty public constructor
    }

    public static BlockedFragment newInstance(String param1, String param2) {
        BlockedFragment fragment = new BlockedFragment();
        return fragment;
    }

    public static BlockedFragment getInstance() {
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
        View favoritesView = inflater.inflate(R.layout.fragment_blocked, container, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
        Gson gson = new Gson();
        ArrayList<String> blkRestaurantList;
        ArrayList<Float> blkRatingList;
        ArrayList<String> blkUrlList;
        if(sharedPref.contains("blkRestaurantList")) {
            String restaurant = sharedPref.getString("blkRestaurantList", "");
            blkRestaurantList = gson.fromJson(restaurant, ArrayList.class);
        }
        else {
            blkRestaurantList = new ArrayList<>();
        }
        if(sharedPref.contains("blkRatingList")) {
            String rating = sharedPref.getString("blkRatingList", "");
            blkRatingList = gson.fromJson(rating, new TypeToken<ArrayList<Float>>(){}.getType());
        }
        else {
            blkRatingList = new ArrayList<>();
        }
        if(sharedPref.contains("blkUrlList")) {
            String url = sharedPref.getString("blkUrlList", "");
            blkUrlList = gson.fromJson(url, ArrayList.class);
        }
        else {
            blkUrlList = new ArrayList<>();
        }
        rv = favoritesView.findViewById(R.id.rv);
        ad = new BlockedAdapter(getActivity(), blkRestaurantList, blkRatingList, blkUrlList); //pass data into adapter

        rv.setAdapter(ad);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setHasFixedSize(true);

        return favoritesView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

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
