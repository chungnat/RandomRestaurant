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


public class FavoritesFragment extends Fragment implements OnFragmentInteractionListener{

    private static FavoritesFragment instance;
    RecyclerView rv;
    private OnFragmentInteractionListener mListener;
    FavAdapter ad;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
        return fragment;
    }

    public static FavoritesFragment getInstance() {
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
        View favoritesView = inflater.inflate(R.layout.fragment_favorites, container, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
        Gson gson = new Gson();
        ArrayList<String> favRestaurantList;
        ArrayList<Float> favRatingList;
        ArrayList<String> favUrlList;
        if(sharedPref.contains("favRestaurantList")) {
            String restaurant = sharedPref.getString("favRestaurantList", "");
            favRestaurantList = gson.fromJson(restaurant, ArrayList.class);
        }
        else {
            favRestaurantList = new ArrayList<>();
        }
        if(sharedPref.contains("favRatingList")) {
            String rating = sharedPref.getString("favRatingList", "");
            favRatingList = gson.fromJson(rating, new TypeToken<ArrayList<Float>>(){}.getType());
        }
        else {
            favRatingList = new ArrayList<>();
        }
        if(sharedPref.contains("favUrlList")) {
            String url = sharedPref.getString("favUrlList", "");
            favUrlList = gson.fromJson(url, ArrayList.class);
        }
        else {
            favUrlList = new ArrayList<>();
        }
        rv = favoritesView.findViewById(R.id.rv);
        ad = new FavAdapter(getActivity(), favRestaurantList, favRatingList, favUrlList); //pass data into adapter

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
