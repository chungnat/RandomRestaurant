package com.example.api_application_v2;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static androidx.core.content.ContextCompat.getDrawable;
import static java.lang.Integer.parseInt;

public class HomeFragment extends Fragment implements OnFragmentInteractionListener {

    private FusedLocationProviderClient fusedLocationClient; // Client for retrieving user location
    private Animator picAnimator;
    private int shortAnimationDuration;
    private static HomeFragment instance;

    // UI Elements
    private EditText locationEdit;
    private static TextView locationErrorTv;
    private EditText categoryEdit;
    private static Button randomBtn;
    private static TextView restaurantTv;
    private static RatingBar ratingBar;
    private static View progressBar;
    private static ImageView restaurantPicture;
    private static ImageView enlargedPicture;
    private static ImageView backgroundPicture;
    private static Button navigationBtn;

    // Local variables
    private static String location;
    private static String category;
    private static String price;
    private static String businessName;
    private static String businessCity;
    private static String coordinateMap; // Coordinate String for Google Maps to use
    private static Double lat;
    private static Double longit;
    private static boolean useUserLoc; // Boolean for whether user location is in use
    private static Float rating;
    private static String pictureURL;
    private boolean open;
    private String range;
    private ArrayList<String> restaurantList;
    private ArrayList<Float> ratingList;
    private ArrayList<String> urlList;
    private OnFragmentInteractionListener mListener;
    private static RetrieveFeedback retrieveFeedback;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public static HomeFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retrieve preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        open = sharedPref.getBoolean("switch", false);
        range = sharedPref.getString("range", "0");

        // Checking if app has location permissions
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Confirming if User wants to deny permission
                AlertDialog.Builder locationAlert = new AlertDialog.Builder(getActivity());
                locationAlert.setTitle("Location");
                locationAlert.setMessage("You won't be able to search based on your location if you do not grant permission. Are you sure you want to deny permission?");
                locationAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If denying, make no further actions
                    }
                });
                locationAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //If accepting, re-asking user for permissions
                        getPermission();
                    }
                });
                AlertDialog alertDialog = locationAlert.create();
                alertDialog.show();
            } else {
                // No explanation needed; request the permission
                getPermission();
            }
        }
        instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);

        // Toggle Buttons for price category
        final ToggleButton priceTog1 = homeView.findViewById(R.id.priceTog1);
        final ToggleButton priceTog2 = homeView.findViewById(R.id.priceTog2);
        final ToggleButton priceTog3 = homeView.findViewById(R.id.priceTog3);
        final ToggleButton priceTog4 = homeView.findViewById(R.id.priceTog4);

        ImageButton locationBtn = homeView.findViewById(R.id.locationBtn); // Button for requesting user location

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity()); // Initialize location client

        // Initialize UI Variables
        locationEdit = homeView.findViewById(R.id.locationET);
        locationErrorTv = homeView.findViewById(R.id.locationErrorTv);
        categoryEdit = homeView.findViewById(R.id.categoryET);
        randomBtn = homeView.findViewById(R.id.randomBtn);
        restaurantTv = homeView.findViewById(R.id.restaurantTv);
        ratingBar = homeView.findViewById(R.id.ratingBar);
        progressBar = homeView.findViewById(R.id.progressBar);
        restaurantPicture = homeView.findViewById(R.id.restaurantPicture);
        enlargedPicture = homeView.findViewById(R.id.enlargedPicture);
        backgroundPicture = homeView.findViewById(R.id.backgroundPicture);
        navigationBtn = homeView.findViewById(R.id.navigationBtn);

        //
        // Set certain UI elements to invisible, as no search has yet been made
        restaurantTv.setVisibility(View.INVISIBLE);
        ratingBar.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        restaurantPicture.setVisibility(View.INVISIBLE);
        restaurantPicture.setImageResource(0);
        enlargedPicture.setVisibility(View.INVISIBLE);
        backgroundPicture.setVisibility(View.INVISIBLE);
        backgroundPicture.setImageResource(0);
        backgroundPicture.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        navigationBtn.setVisibility(View.INVISIBLE);

        //Retrieve history of restaurants
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
        if(sharedPref.contains("restaurantList")) {
            Gson gson = new Gson();
            String restaurantJson = sharedPref.getString("restaurantList", "");
            String ratingJson = sharedPref.getString("ratingList", "");
            String urlJson = sharedPref.getString("urlList", "");
            restaurantList = gson.fromJson(restaurantJson, ArrayList.class);
            ratingList = gson.fromJson(ratingJson, new TypeToken<ArrayList<Float>>(){}.getType());
            urlList = gson.fromJson(urlJson, ArrayList.class);
        }
        else {
            restaurantList = new ArrayList<>();
            ratingList = new ArrayList<>();
            urlList = new ArrayList<>();
        }


        // Restore saved state if one has been stored
        if (savedInstanceState != null) {
            boolean ExistSave = savedInstanceState.getBoolean("ExistingSave");
            if(ExistSave) {
                rating = savedInstanceState.getFloat("Rating");
                location = savedInstanceState.getString("Location");
                category = savedInstanceState.getString("Category");
                businessName = savedInstanceState.getString("Restaurant");
                pictureURL = savedInstanceState.getString("Picture_URL");
                coordinateMap = savedInstanceState.getString("Coordinates");
                businessCity = savedInstanceState.getString("Business_City");
                setText(businessName, rating, pictureURL, coordinateMap, businessCity);
                locationEdit.setText(location);
                categoryEdit.setText(category);
                doneLoading();
            }
        }


        // Listener for the random search button
        // Gets the category entered, checks which price buttons are on, gets the location entered,
        // Then makes a method call for retrieving feedback from a API call
        randomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = categoryEdit.getText().toString();
                ArrayList<String> prices = new ArrayList<>();
                if(priceTog1.isChecked()) {
                    prices.add("1");
                }
                if(priceTog2.isChecked()) {
                    prices.add("2");
                }
                if(priceTog3.isChecked()) {
                    prices.add("3");
                }
                if(priceTog4.isChecked()) {
                    prices.add("4");
                }
                String priceArray = prices.toString();
                price = priceArray.substring(1,priceArray.length() - 1);
                if(!useUserLoc) { // If not using user location, then get location inputted
                    location = locationEdit.getText().toString();
                    if(location.isEmpty()) {
                        locationErrorTv.setText("Location is Required");
                    }else {
                        locationErrorTv.setText("");
                        feedback();
                    }
                }
                else { // location has been handled elsewhere, make api call
                    feedback();
                }
                locationEdit.onEditorAction(EditorInfo.IME_ACTION_DONE); //closes keyboard after clicking button
                locationEdit.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });

        if(useUserLoc) {
            getLocation();
            locationEdit.setText("");
            locationEdit.setHint("Using current location");
            locationEdit.setEnabled(false);
            locationErrorTv.setVisibility(View.INVISIBLE);
        }
        // Listener for using user location
        // Calls a method to handle the retrieval of the user's location
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askUserLocation();
            }
        });

        // Listener for navigation button to google maps
        // Uses coordinates to search for a specific restaurant in google maps
        navigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse(coordinateMap + Uri.encode(businessName + " " + businessCity));
                Intent navigate = new Intent(Intent.ACTION_VIEW, gmmIntentUri );
                navigate.setPackage("com.google.android.apps.maps");
                if(navigate.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(navigate);
                }

            }
        });

        // listener that checks if toggle is on or off, changing the background accordingly
        priceTog1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog1.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.left_price_box_on));
                } else {
                    priceTog1.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.left_price_box));
                }
            }
        });

        priceTog2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog2.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.price_box_on));
                } else {
                    priceTog2.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.price_box));
                }
            }
        });

        priceTog3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog3.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.price_box_on));
                } else {
                    priceTog3.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.price_box));
                }
            }
        });

        priceTog4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceTog4.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.right_price_box_on));
                } else {
                    priceTog4.setBackgroundDrawable(getDrawable(getActivity(),R.drawable.right_price_box));
                }
            }
        });

        // Listener for if restaurantPicture is clicked, calling zoom method
        final View thumb1View = restaurantPicture;
        thumb1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageFromThumb(thumb1View, pictureURL);
            }
        });

        // Gets the device's animation duration
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        return homeView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        mListener = null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Sets the results within the UI from the parameters passed from the API handler
    // Parameters : String result = name of business chosen
    //              Float rating = rating of restaurant
    //              String pictureUrl = url of the business picture
    //              String coordinate = coordinate string that is saved for navigation uses
    //              String businessCity = city business is located in, for navigation uses
    public void setText(String result, Float rating, String picture_Url, String coordinate, String business_City) {
        if(!picture_Url.isEmpty()) {
            Picasso.get().load(picture_Url).transform(new RoundCornersTransformation(30, 0, true, true))
                    .into(restaurantPicture); // Uses Picasso to set the image view directly from a URL
            if(!restaurantList.contains(result)) {
                urlList.add(0, picture_Url);
            }
        }
        else {
            restaurantPicture.setImageResource(0);
        }
        businessName = result;
        restaurantTv.setText(businessName);
        ratingBar.setRating(rating);
        coordinateMap = coordinate;
        businessCity = business_City;
        this.rating = rating;
        pictureURL = picture_Url;
        if(!restaurantList.contains(result)) {
            restaurantList.add(0, businessName);
            ratingList.add(0, rating);
            storeRestaurantInfo();
        }
    }

    // Makes a retrieve feedback object used to make an API call
    public void feedback() {
        retrieveFeedback = new RetrieveFeedback();
        retrieveFeedback.execute(location, category, price, lat + "", longit + "",
                                String.valueOf(useUserLoc), String.valueOf(open), range);
    }

    // Sets the UI elements to a loading state by showing a progress bar and hiding other elements
    public static void loading() {
        randomBtn.setClickable(false);
        navigationBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        restaurantTv.setVisibility(View.INVISIBLE);
        ratingBar.setVisibility(View.INVISIBLE);
        restaurantPicture.setVisibility(View.INVISIBLE);
        enlargedPicture.setVisibility(View.INVISIBLE);
    }

    // Sets the UI elements to a done loading state
    // Hides progress bar and shows certain UI elements
    public static void doneLoading() {
        randomBtn.setClickable(true);
        navigationBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        restaurantTv.setVisibility(View.VISIBLE);
        ratingBar.setVisibility(View.VISIBLE);
        restaurantPicture.setVisibility(View.VISIBLE);
        retrieveFeedback.cancel(true);
    }

    public static void errorLoading() {
        restaurantTv.setText("No results");
        restaurantTv.setVisibility(View.VISIBLE);
    }

    // Directly requests permission from user to access location
    public void getPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    // Prompts user if they want to use their location
    public void askUserLocation () {
        AlertDialog.Builder locationAlert = new AlertDialog.Builder(getActivity());
        if(useUserLoc) {
            locationAlert.setTitle("Location");
            locationAlert.setMessage("Stop using your current location?");
            locationAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    locationEdit.setEnabled(true);
                    locationEdit.setHint("City, Zip Code, etc.");
                    locationEdit.setText("");
                    useUserLoc = false;
                    locationErrorTv.setVisibility(View.VISIBLE);

                }
            });
            locationAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        else {
            locationAlert.setTitle("Location");
            locationAlert.setMessage("Use your current location instead?");
            locationAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(getLocation()) {
                        locationEdit.setText("");
                        locationEdit.setHint("Using current location");
                        locationEdit.setEnabled(false);
                        locationErrorTv.setVisibility(View.INVISIBLE);
                        useUserLoc = true;
                    }
                }
            });
            locationAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        AlertDialog alertDialog = locationAlert.create();
        alertDialog.show();
    }

    // Uses a location client to get a location, along with its coordinates
    public boolean getLocation() {
        Task<Location> task = fusedLocationClient.getLastLocation();
        while(!task.isComplete()) {
            randomBtn.setClickable(false);
        }
        if(task.isComplete()) {
            Location userLocation = task.getResult();
            if(userLocation != null) {
                lat = userLocation.getLatitude();
                longit = userLocation.getLongitude();
                randomBtn.setClickable(true);
                return true;
            }
            else {
                AlertDialog.Builder locationError = new AlertDialog.Builder(getActivity());
                locationError.setTitle("Error");
                locationError.setMessage("Could not retrieve your location, please make sure your location services are turned on and try again.");
                locationError.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                AlertDialog alertDialog = locationError.create();
                alertDialog.show();
            }
            return false;
        }
        return false;
    }

    // Handles animation from zooming and unzooming a given view
    // Parameters : thumbView = given view to zoom
    //              pictureUrl = url of picture to set the zoomed image view
    // Source : Android Studio
    public void zoomImageFromThumb(final View thumbView, String pictureUrl) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (picAnimator != null) {
            picAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        Picasso.get().load(pictureUrl).transform(new RoundCornersTransformation(30,0,true,true))
                .into(enlargedPicture); // Uses Picasso to set the image view directly from a URL

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getActivity().findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        enlargedPicture.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        enlargedPicture.setPivotX(0f);
        enlargedPicture.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(enlargedPicture, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(enlargedPicture, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(enlargedPicture, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(enlargedPicture,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                picAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                picAnimator = null;
            }
        });
        backgroundPicture.setVisibility(View.VISIBLE);
        set.start();
        picAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        enlargedPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (picAnimator != null) {
                    picAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(enlargedPicture, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(enlargedPicture,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(enlargedPicture,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(enlargedPicture,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        enlargedPicture.setVisibility(View.GONE);
                        picAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        enlargedPicture.setVisibility(View.GONE);
                        picAnimator = null;
                    }
                });
                backgroundPicture.setVisibility(View.INVISIBLE);
                set.start();
                picAnimator = set;
            }
        });
    }

    //Stores information of each restaurant into sharedpreferences as a json string
    protected void storeRestaurantInfo() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String restaurant = gson.toJson(restaurantList);
        String rating = gson.toJson(ratingList);
        String urls = gson.toJson(urlList);
        editor.putString("restaurantList", restaurant);
        editor.putString("ratingList", rating);
        editor.putString("urlList", urls);
        editor.commit();
    }

    public void clearRestaurantInfo() {
        restaurantList.clear();
        ratingList.clear();
        urlList.clear();
    }

    void makeToast(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }

    // Saves variables in case instance state is destroyed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(businessName != null) {
            savedInstanceState.putFloat("Rating", rating);
            savedInstanceState.putString("Location", location);
            savedInstanceState.putString("Category", category);
            savedInstanceState.putString("Restaurant", businessName);
            savedInstanceState.putString("Picture_URL", pictureURL);
            savedInstanceState.putString("Coordinates", coordinateMap);
            savedInstanceState.putString("Business_City", businessCity);
            savedInstanceState.putBoolean("ExistingSave", true);
        }
        else {
            savedInstanceState.putBoolean("ExistingSave", false);
        }
        super.onSaveInstanceState(savedInstanceState);
    }


    private class RetrieveFeedback extends AsyncTask<String, Integer, String> {
        Response response;

        protected void onPreExecute() {
            loading();
        }

        @Override
        protected String doInBackground(String... strings) {
            //String location, String category, String price, Double lat, Double longit, boolean useUserLoc, boolean open, String range
            String url = "https://api.yelp.com/v3/businesses/search?";
            // Add location to request
            if(strings[5].equals("true")) {
                url += "latitude=" + strings[3];
                url += "&longitude=" + strings[4];
            }
            else {
                url += "location=" + strings[0];
            }
            // Add type of food to request
            if(!strings[1].isEmpty()) {
                url += "&term=" + strings[1];
            }
            // Add price to request
            if(!strings[2].isEmpty()) {
                url += "&price=" + strings[2];
            }
            // Limit results to 50
            url += "&limit=50";
            if(strings[6].equals("true")) {
                url += "&open_now=true";
            }
            if(!strings[7].equals("0")) {
                url += "&radius=" + strings[7];
            }
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            // Don't steal this
            String APIKey = "Bearer jH9lGEx7tYvtD5A5tVfMIeRMbc0VeXsXa4wqWBq1BuP-VbLJF5u0vmUzQYhQa39_t3Qptf5Tw19gzidvuk7jBtrStlhR9MpHCizfsCWHQUga7JO7KTLBz2MgWFd5XnYx";
            okhttp3.Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .addHeader("Authorization", APIKey)
                    .build();
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String Result) {
            while(response == null) {}
            try {
                String resp = response.body().string();
                try {
                    JSONObject obj = new JSONObject(resp);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeFragment.getInstance().getActivity());
                    Gson gson = new Gson();
                    ArrayList<String> blkRestaurantList;
                    if(sharedPref.contains("blkRestaurantList")) {
                        String restaurant = sharedPref.getString("blkRestaurantList", "");
                        blkRestaurantList = gson.fromJson(restaurant, ArrayList.class);
                    }
                    else {
                        blkRestaurantList = new ArrayList<>();
                    }
                    boolean isBlocked = true;
                    JSONArray arr = obj.getJSONArray("businesses");
                    JSONObject business = arr.getJSONObject(0);
                    String name = "";
                    Random rand = new Random();
                    int totalRestaurants = obj.getInt("total");
                    int bound = Math.min(50, totalRestaurants);
                    int count = 0;
                    while(isBlocked) {
                        int randomInt = rand.nextInt(bound);
                        business = arr.getJSONObject(randomInt);
                        name = business.getString("name");
                        if(!blkRestaurantList.contains(name)) {
                            isBlocked = false;
                        }
                        count++;
                        if(count >= bound) {
                            errorLoading();
                            break;
                        }
                    }
                    if(!isBlocked) {
                        String imageURL = business.getString("image_url");
                        float rating = business.getInt("rating");
                        JSONObject location = business.getJSONObject("location");
                        String address = location.getString("address1");
                        String city = location.getString("city");
                        JSONObject coordinates = business.getJSONObject("coordinates");
                        double latitude = coordinates.getInt("latitude");
                        double longitude = coordinates.getInt("longitude");
                        String coordinate = "geo:" + latitude + "," + longitude + "?q=";
                        setText(name, rating, imageURL, coordinate, city);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            response = null;
            doneLoading();
        }
    }
}

