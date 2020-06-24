package com.example.api_application_v2;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.graphics.Color.RED;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<String> names;
    private ArrayList<Float> ratings;
    private ArrayList<String> images;
    static SharedPreferences sharedPref;
    static ArrayList<String> blkRestaurantList;
    static ArrayList<Float> blkRatingList;
    static ArrayList<String> blkUrlList;
    static ArrayList<String> favRestaurantList;
    static ArrayList<Float> favRatingList;
    static ArrayList<String> favUrlList;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RatingBar ratingBar;
        ImageView imgView;
        ImageButton favoriteBtn;
        ImageButton blckBtn;
        boolean blocked;
        boolean faved;

        //initializing the views
        public MyViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.ratingBar = itemView.findViewById(R.id.ratingCard);
            this.imgView = itemView.findViewById(R.id.restaurantImg);
            this.favoriteBtn = itemView.findViewById(R.id.favoriteButton);
            this.blckBtn = itemView.findViewById(R.id.blockButton);
            blocked = false;
        }
    }

    //Constructor that takes in data to be set
    public MyAdapter(Context mContext, ArrayList<String> restaurantName,  ArrayList<Float> rating,  ArrayList<String> url) {
        this.mContext = mContext;
        images = url;
        ratings = rating;
        names = restaurantName;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_card, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    //This method sets the actual data to each card view
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int i) {
        final int duration = Toast.LENGTH_SHORT;
        favRestaurantList = new ArrayList<>();
        favRatingList = new ArrayList<>();
        favUrlList = new ArrayList<>();
        blkRestaurantList = new ArrayList<>();
        blkRatingList = new ArrayList<>();
        blkUrlList = new ArrayList<>();
        // Initializing preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
        Gson gson = new Gson();
        if(sharedPref.contains("blkRestaurantList")) {
            String blkRestaurantJson = sharedPref.getString("blkRestaurantList", "null");
            String blkRatingJson = sharedPref.getString("blkRatingList", "null");
            String blkUrlJson = sharedPref.getString("blkUrlList", "null");
            if(!blkRestaurantJson.equals("null")) {
                blkRestaurantList = gson.fromJson(blkRestaurantJson, new TypeToken<ArrayList<String>>(){}.getType());
            }
            if(!blkRatingJson.equals("null")) {
                blkRatingList = gson.fromJson(blkRatingJson, new TypeToken<ArrayList<Float>>(){}.getType());
            }
            if(!blkUrlJson.equals("null")) {
                blkUrlList = gson.fromJson(blkUrlJson, new TypeToken<ArrayList<String>>(){}.getType());
            }
        }
        if(sharedPref.contains("favRestaurantList")) {
            String favRestaurantJson = sharedPref.getString("favRestaurantList", "null");
            String favRatingJson = sharedPref.getString("favRatingList", "null");
            String favUrlJson = sharedPref.getString("favUrlList", "null");
            if(!favRestaurantJson.equals("null")) {
                favRestaurantList = gson.fromJson(favRestaurantJson, new TypeToken<ArrayList<String>>(){}.getType());
            }
            if(!favRatingJson.equals("null")) {
                favRatingList = gson.fromJson(favRatingJson, new TypeToken<ArrayList<Float>>(){}.getType());
            }
            if(!favUrlJson.equals("null")) {
                favUrlList = gson.fromJson(favUrlJson, new TypeToken<ArrayList<String>>(){}.getType());
            }
        }
        // Setting up the view for each card
        if(names.size() > 0) {
            holder.title.setText(names.get(i));
            holder.ratingBar.setRating(ratings.get(i));
            Picasso.get().load(images.get(i)).transform(new RoundCornersTransformation(20, 0, true, true)).into(holder.imgView);
            if(favRestaurantList.contains(names.get(i))) {
                holder.faved = true;
                holder.favoriteBtn.setColorFilter(RED);
            }
            else if(blkRestaurantList.contains(names.get(i))) {
                holder.blocked = true;
                holder.blckBtn.setColorFilter(RED);
            }
        }
        // OnClickListener for fav button, onclick will update fav and block lists as appropriate
        holder.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!holder.faved) {
                    favRestaurantList.add(names.get(i));
                    favRatingList.add(ratings.get(i));
                    favUrlList.add(images.get(i));
                    holder.favoriteBtn.setColorFilter(RED);
                    holder.faved = true;
                    Toast toast = Toast.makeText(mContext, "Favorited", duration);
                    toast.show();
                    if(holder.blocked) {
                        holder.blocked = false;
                        holder.blckBtn.setColorFilter(Color.GRAY);
                        blkRestaurantList.remove(names.get(i));
                        blkRatingList.remove(ratings.get(i));
                        blkUrlList.remove(images.get(i));
                    }
                }
                else {
                    favRestaurantList.remove(names.get(i));
                    favRatingList.remove(ratings.get(i));
                    favUrlList.remove(images.get(i));
                    holder.favoriteBtn.setColorFilter(Color.GRAY);
                    holder.faved = false;
                    Toast toast = Toast.makeText(mContext, "Unfavorited", duration);
                    toast.show();
                }
                updatePreferences();
            }
        });
        holder.blckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!holder.blocked) {
                    blkRestaurantList.add(names.get(i));
                    blkRatingList.add(ratings.get(i));
                    blkUrlList.add(images.get(i));
                    if(holder.faved) {
                        favRestaurantList.remove(names.get(i));
                        favRatingList.remove(ratings.get(i));
                        favUrlList.remove(images.get(i));
                    }
                    holder.blckBtn.setColorFilter(RED);
                    holder.blocked = true;
                    holder.faved = false;
                    holder.favoriteBtn.setColorFilter(Color.GRAY);
                    Toast toast = Toast.makeText(mContext, "Blocked", duration);
                    toast.show();
                }
                else {
                    blkRestaurantList.remove(names.get(i));
                    blkRatingList.remove(ratings.get(i));
                    blkUrlList.remove(images.get(i));
                    holder.blocked = false;
                    holder.blckBtn.setColorFilter(Color.GRAY);
                    Toast toast = Toast.makeText(mContext, "Unblocked", duration);
                    toast.show();
                }
                updatePreferences();
            }
        });
    }

    @Override
    public void onViewDetachedFromWindow (MyViewHolder holder) {
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {

    }

    @Override
    public void onViewRecycled(@NonNull final MyViewHolder holder) {

    }

    @Override
    public int getItemCount() {
        if(names != null) {
            return names.size();
        }
        else {
            return 0;
        }
    }

    public void updatePreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String bnl = gson.toJson(blkRestaurantList);
        String brl = gson.toJson(blkRatingList);
        String burl = gson.toJson(blkUrlList);
        String fnl = gson.toJson(favRestaurantList);
        String frl = gson.toJson(favRatingList);
        String furl = gson.toJson(favUrlList);
        editor.putString("blkRestaurantList", bnl);
        editor.putString("blkRatingList", brl);
        editor.putString("blkUrlList", burl);
        editor.putString("favRestaurantList", fnl);
        editor.putString("favRatingList", frl);
        editor.putString("favUrlList", furl);
        editor.commit();
    }
}