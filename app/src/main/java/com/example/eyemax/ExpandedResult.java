package com.example.eyemax;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

//Butterknife imports
import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Activity for getting and displaying information about a given movie, including relevant genre,
 * cast, and images.
 */

public class ExpandedResult extends Activity {

    //declare important objects
    private RequestQueue queue;

    private SharedPreferences sharedPref;

    private static final String LOGTAG = "logme";

    //declare string constants
    private String MY_API_MOVIES_URL;
    private String MY_API_MOVIES_KEY;

    //bind UI with butterknife
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvYear)
    TextView tvYear;
    @BindView(R.id.tvGenre)
    TextView tvGenre;
    @BindView(R.id.ivPoster)
    TextView ivPoster;

    //declare important class variables
    private String imdbId;
    private String movieTitle;
    private int movieYear;
    private String genre;
    private String posterUrl;
    private ArrayList<Integer> tvNameID = new ArrayList<>();
    private ArrayList<Integer> tvCharID = new ArrayList<>();
    private ArrayList<Integer> ivCastID = new ArrayList<>();
    ArrayList<Actor> actors;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expanded_results);

        //bind UI with butterknife
        ButterKnife.bind(this);

        //initialize shared preferences
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        //get passed movie info from intent
        imdbId = getIntent().getStringExtra("IMDB_ID");
        movieTitle = getIntent().getStringExtra("Movie_Name");
        movieYear = getIntent().getIntExtra("year", -1);

        //run initialize function for all views
        initViews();

        queue = Volley.newRequestQueue(this);

        //initialize information fields
        genre = "";
        posterUrl = "";

        actors = new ArrayList<>();

        //check if movie information exists in cache and retrieve it
        if(sharedPref.getString(movieTitle + " Genre", null) != null) {
            System.out.println("found " + movieTitle + " in storage");
            genre = sharedPref.getString(movieTitle + " Genre", null);
            posterUrl = sharedPref.getString(movieTitle + " Poster URL", null);
            String actorNamesSet = sharedPref.getString(movieTitle + " Actor Names",
                    null);
            String actorCharactersSet = sharedPref.getString(movieTitle + " Actor Characters",
                    null);

            String[] actorNames = actorNamesSet.split("%20");
            String[] actorCharacters = actorCharactersSet.split("%20");

            for(int i=0; i < actorNames.length; i++){
                String actorPhotoUrl = sharedPref.getString(actorNames[i] + " Photo URL",
                        "");
                Actor actor = new Actor(actorNames[i], actorCharacters[i], actorPhotoUrl);
                actors.add(actor);
            }
        } else {
            System.out.println("did not find " + movieTitle + " in storage");
        }

        //get sensitive api credentials from config
        try {
            MY_API_MOVIES_URL = javaant.com.propertiesfile.Util.getProperty(
                    "MY_API_MOVIES_URL", getApplicationContext());
            MY_API_MOVIES_KEY = javaant.com.propertiesfile.Util.getProperty(
                    "MY_API_MOVIES_KEY", getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Populate views with info from cache or APIs
        try {
            updateViews();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //lots of views to store for referencing
    private void initViews(){
        //ID arrays for referencing in loops
        tvNameID.add(R.id.tvName1);
        tvNameID.add(R.id.tvName2);
        tvNameID.add(R.id.tvName3);
        tvNameID.add(R.id.tvName4);
        tvNameID.add(R.id.tvName5);
        tvNameID.add(R.id.tvName6);
        tvNameID.add(R.id.tvName7);
        tvNameID.add(R.id.tvName8);
        tvNameID.add(R.id.tvName9);
        tvNameID.add(R.id.tvName10);
        tvCharID.add(R.id.tvChar1);
        tvCharID.add(R.id.tvChar2);
        tvCharID.add(R.id.tvChar3);
        tvCharID.add(R.id.tvChar4);
        tvCharID.add(R.id.tvChar5);
        tvCharID.add(R.id.tvChar6);
        tvCharID.add(R.id.tvChar7);
        tvCharID.add(R.id.tvChar8);
        tvCharID.add(R.id.tvChar9);
        tvCharID.add(R.id.tvChar10);
        ivCastID.add(R.id.ivCast1);
        ivCastID.add(R.id.ivCast2);
        ivCastID.add(R.id.ivCast3);
        ivCastID.add(R.id.ivCast4);
        ivCastID.add(R.id.ivCast5);
        ivCastID.add(R.id.ivCast6);
        ivCastID.add(R.id.ivCast7);
        ivCastID.add(R.id.ivCast8);
        ivCastID.add(R.id.ivCast9);
        ivCastID.add(R.id.ivCast10);
    }

    private void updateViews() throws IOException {
        //set movie title, passed from an intent
        tvTitle.setText(movieTitle);
        if(movieYear == -1) {
            tvYear.setText("");
        } else {
            tvYear.setText("Released: " + movieYear);
        }

        //set thread policy for making network requests
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //set movie poster, release year, genre, and cast

        if(posterUrl.equals("")) {
            //if posterUrl is not in cache, retrieve it from api
            getMoviePoster();
        } else {
            //if posterUrl is cached, get image from url as a drawable and display it
            InputStream is = (InputStream) new URL(posterUrl).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            ivPoster.setImageDrawable(d);
        }

        if(genre.equals("")) {
            //if genre is not in cache, retrieve it from api
            getMovieGenre();
        } else {
            //if genre is cached, display it
            tvGenre.setText("Genre: " + genre);
        }

        //if actors have been cached, get their information and display them
        if(actors.size() > 0) {
            for (int i = 0; i < 10 && i < actors.size(); i++){
                System.out.println("looking at index " + i);
                Actor actor = actors.get(i);

                TextView tempName = findViewById(tvNameID.get(i));
                TextView tempChar = findViewById(tvCharID.get(i));
                String actorName = actor.getName();
                tempName.setText(actorName);
                String actorCharacter = actor.getCharacter();
                tempChar.setText(actorCharacter);
                String photoUrl = actor.getPhotoUrl();

                //if actor has a photo, retrieve it by url, get it as a drawable, and display it
                if(!photoUrl.equals("")) {
                    ImageView tempImage = findViewById(ivCastID.get(i));

                    InputStream is = (InputStream) new URL(photoUrl).getContent();
                    Drawable d = Drawable.createFromStream(is, "src name");
                    tempImage.setImageDrawable(d);
                }

            }
        } else {
            //if movie cast info is not in cache, get it from api
            getMovieCast();
        }
    }

    //iterate through top 10 (or less) cast members and set text views
    private void getMovieCast() {
        //actors endpoint string
        String castQS = "/actors";

        //create request to retrieve cast information
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                MY_API_MOVIES_URL + "/v1/movie/" + imdbId + castQS + MY_API_MOVIES_KEY,
                null,
                response ->  {
                    try {
                        JSONArray actorsArray = response.getJSONArray("data");

                        //cap at 10 actors or however many actors are in the movie, whichever is
                        // lower

                        //initialize strings to cache
                        String actorNames = "";
                        String actorCharacters = "";

                        //for loop for getting cast information and displaying it
                        for (int i = 0; i < 10 && i < actorsArray.length(); i++){
                            System.out.println("looking at index " + i);
                            JSONObject jsonObject = actorsArray.getJSONObject(i);
                            JSONObject name = jsonObject.getJSONObject("name");

                            //get relevant strings and insert separators

                            TextView tempName = findViewById(tvNameID.get(i));
                            TextView tempChar = findViewById(tvCharID.get(i));
                            String actorName = name.getString("name");
                            tempName.setText(actorName);
                            actorNames += actorName + "%20";
                            String actorCharacter = jsonObject.getString("character");
                            tempChar.setText(actorCharacter);
                            actorCharacters += actorCharacter + "%20";

                            //initialize image view here to access on response
                            ImageView tempImage = findViewById(ivCastID.get(i));

                            //create request to retrieve actor photoUrl
                            JsonObjectRequest photoRequest = new JsonObjectRequest(
                                    Request.Method.GET,
                                    MY_API_MOVIES_URL + "/v1/name/" + name.getString(
                                            "imdbId") + MY_API_MOVIES_KEY,
                                    null,
                                    response1 -> {
                                        try {
                                            System.out.println(response1);
                                            JSONObject data = response1.getJSONObject("data");
                                            String photoURL = data.getString("photoUrl");

                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putString(actorName + " Photo URL", photoURL);
                                            editor.apply();

                                            //get image from url as drawable and display it
                                            InputStream is = (InputStream) new URL(photoURL)
                                                    .getContent();
                                            Drawable d = Drawable.createFromStream(is,
                                                    "src name");
                                            tempImage.setImageDrawable(d);
                                        } catch (JSONException | MalformedURLException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    },
                                    error -> Log.d("ERROR", "error => " + error
                                            .toString())
                            );

                            queue.add(photoRequest);

                        }

                        //store cast in shared preferences to cache
                        System.out.println("Storing actors");
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(movieTitle + " Actor Names", actorNames);
                        editor.putString(movieTitle + " Actor Characters", actorCharacters);
                        editor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i(LOGTAG, response.toString());
                },
                error -> {
                    Log.d("ERROR", "error => "+error.toString());
                }
        );

        queue.add(objectRequest);
    }

    //set movie poster
    private void getMoviePoster() {

        //request to retrieve posterUrl
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                MY_API_MOVIES_URL + "/v1/movie/" + imdbId + MY_API_MOVIES_KEY,
                null,
                response ->  {
                    try {
                        JSONObject jsonObject = response.getJSONObject("data");
                        String posterURL = jsonObject.getString("posterUrl");
                        System.out.println(posterURL);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(movieTitle + " Poster URL", posterURL);
                        editor.apply();

                        //get movie poster as drawable from posterUrl and display it

                        InputStream is = (InputStream) new URL(posterURL).getContent();
                        Drawable d = Drawable.createFromStream(is, "src name");
                        ivPoster.setImageDrawable(d);

                    } catch (JSONException | MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(LOGTAG, response.toString());
                },
                error -> {
                    Log.d("ERROR", "error => "+error.toString());
                }
        );

        queue.add(objectRequest);
    }

    //set most relevant genre of movie
    private void getMovieGenre() {

        //request to retrieve genre
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                MY_API_MOVIES_URL + "/v1/movie/" + imdbId + "/genres" + MY_API_MOVIES_KEY,
                null,
                response ->  {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        genre = jsonObject.getString("genre");

                        //store genre in cache
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(movieTitle + " Genre", genre);
                        editor.apply();

                        System.out.println(genre);

                        //display genre
                        tvGenre.setText("Genre: " + genre);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i(LOGTAG, response.toString());
                },
                error -> {
                    Log.d("ERROR", "error => "+error.toString());
                }
        );

        queue.add(objectRequest);
    }

}