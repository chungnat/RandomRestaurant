package com.example.api_application_v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    protected TextView aboutTv;
    protected String aboutString;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboutString = "By Nathan Chung \nPowered by Yelp Fusion API \n\nGithub: https://github.com/chungnat/APIApplication_v2";
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View aboutView = inflater.inflate(R.layout.fragment_about, container, false);
        aboutTv = aboutView.findViewById(R.id.aboutTv);
        aboutTv.setText(aboutString);

        return aboutView;
    }
}