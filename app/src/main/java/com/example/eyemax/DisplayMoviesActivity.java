package com.example.eyemax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.Nullable;

/*
 * Activity for getting and displaying all of the movies the group of passed in actors have in
 * common and then displaying them in a list view.
 */

public class DisplayMoviesActivity extends Activity {

    //Array list containing all of the movies which the actors have in common
    private ArrayList<Movie> sharedFilms;
    //Hash map mapping celebrity names to their imdB ids
    private HashMap<String, String> celebImdbIds;

    //declare the reqwuest queue and shared preferences
    private RequestQueue queue;
    private SharedPreferences sharedPref;

    //string constants for creating request urls
    private String MY_API_MOVIES_URL;
    private String ACTOR_SEARCH_ENDPOINT;
    private String MOVIE_SEARCH_ENDPOINT;
    private String MY_API_MOVIES_KEY;
    private String MY_API_MOVIES_KEY_BACKUP;
    private String actorQS = "&name=";

    //declare the listview
    private ListView LV_display;

    //counters to avoid completing activity before async requests are completed
    private int sharedCounter;
    private int numFoundCelebs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_movies);

        //list of celebrities is passed in via intent
        ArrayList<String> foundCelebs = getIntent().getStringArrayListExtra("foundCelebs");

        //initialize important counters, constants, and objects
        numFoundCelebs = foundCelebs.size();
        sharedCounter = 0;

        queue = Volley.newRequestQueue(this);

        sharedFilms = new ArrayList<Movie>();

        LV_display = (ListView) findViewById(R.id.LV_display);

        //fetch sensitive information (API urls and keys) from config file
        try {
            MY_API_MOVIES_URL = javaant.com.propertiesfile.Util.getProperty(
                    "MY_API_MOVIES_URL", getApplicationContext());
            ACTOR_SEARCH_ENDPOINT = javaant.com.propertiesfile.Util.getProperty(
                    "ACTOR_SEARCH_ENDPOINT", getApplicationContext());
            MOVIE_SEARCH_ENDPOINT = javaant.com.propertiesfile.Util.getProperty(
                    "MOVIE_SEARCH_ENDPOINT", getApplicationContext());
            MY_API_MOVIES_KEY = javaant.com.propertiesfile.Util.getProperty(
                    "MY_API_MOVIES_KEY", getApplicationContext());
            MY_API_MOVIES_KEY_BACKUP = javaant.com.propertiesfile.Util.getProperty(
                    "MY_API_MOVIES_KEY_BACKUP", getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        //hashmap mapping celebrity name to list of filmography
        HashMap<String, ArrayList<Movie>> storedCelebs = new HashMap<>();
        //hashmap mapping celebrities to their imdb ids
        celebImdbIds = new HashMap<>();

        //iterate through foundCelebs to see if they have been cached
        for (String celeb: foundCelebs) {
            //if a celeb exists in the cahce fetch their cached values from shared preferences
            if(sharedPref.getString(celeb + " Movie Titles", null) != null) {
                System.out.println("found " + celeb + " in storage");
                String celebMovieTitlesSet = sharedPref.getString(celeb + " Movie Titles",
                        null);
                String celebMovieIdsSet = sharedPref.getString(celeb + " Movie Ids",
                        null);
                String celebMovieYearSet = sharedPref.getString(celeb + " Movie Years",
                        null);

                String[] celebMovieTitles = celebMovieTitlesSet.split("%20");
                String[] celebMovieIds = celebMovieIdsSet.split("%20");
                String[] celebMovieYears = celebMovieYearSet.split("%20");

                ArrayList<Movie> movies = new ArrayList<>();

                for(int i=0; i < celebMovieTitles.length; i++){
                    String movieYearName = celebMovieYears[i];
                    int movieYear = Integer.parseInt(movieYearName);
                    Movie movie = new Movie(celebMovieTitles[i], celebMovieIds[i], movieYear);
                    movies.add(movie);
                }

                String celebImdbId = "";

                if(sharedPref.getString(celeb + " imdbId", null) != null) {
                    System.out.println("found imdbId for " + celeb + " in storage");
                    celebImdbId = sharedPref.getString(celeb + " imdbId", null);
                }

                storedCelebs.put(celeb, movies);
                celebImdbIds.put(celeb, celebImdbId);
            } else {
                System.out.println("did not find " + celeb + " in storage");
            }
        }

        //bool to check requests are being made
        boolean gettingFromApi = false;

        //fetch filmographies for each celeb
        for (int i = 0; i < foundCelebs.size(); i++) {
            System.out.println("name is " + foundCelebs.get(i));
            String celeb = foundCelebs.get(i);
            System.out.println("current stored celebs");
            System.out.println(storedCelebs.keySet().toString());
            if(!storedCelebs.containsKey(celeb)) {
                //if the celeb has not been cached, get their filmography from the api
                getFilmography(celeb);
            } else {
                sharedCounter++;
                if(celebImdbIds.get(celeb).equals("")) {
                    //if the celeb does not have their imdb id stored, fetch it from the api
                    gettingFromApi = true;
                    storeCelebImdbId(celeb);
                }
                if(sharedFilms.size() == 0) {
                    //if this is the first celeb being stored, assign sharedfilms to their
                    //filmography
                    sharedFilms = storedCelebs.get(celeb);
                } else {
                    //otherwise get the overlap in the current shared filmography and the current
                    //celeb's filmography using retainAll()
                    System.out.println("Looking at " + celeb +" common films");
                    System.out.println("Sharedfilms was");
                    System.out.println(sharedFilms);
                    sharedFilms.retainAll(storedCelebs.get(celeb));
                    System.out.println("new shared films from storage");
                    System.out.println(sharedFilms);
                    //if there are no shared films, create a placeholder object in sharedFilms to
                    //indicate this
                    if(sharedFilms.size() == 0) {
                        sharedFilms.add(new Movie("No common movies", "",
                                -1));
                    }
                }
                System.out.println("Got films from storage for " + celeb + ": " +
                        storedCelebs.get(celeb));
            }
        }

        System.out.println("About to display sharedFilms:");
        System.out.println(sharedFilms);

        //if no filmographies are being retrieved from the api, sharedFilms is complete and we can
        //display it
        if(!gettingFromApi) {

            //remove any duplicate movies from sharedFilms
            sharedFilms = removeDuplicates(sharedFilms);

            //initialize the adapter
            MovieListAdapter displayAdapter = new MovieListAdapter(
                    DisplayMoviesActivity.this, sharedFilms);

            LV_display.setAdapter(displayAdapter);

            LV_display.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    //pass information about the given movie to the expanded result activity
                    Intent intent = new Intent(getBaseContext(), ExpandedResult.class);
                    Movie clickedMovie = sharedFilms.get(position);

                    intent.putExtra("Movie_Name", clickedMovie.getTitle());
                    intent.putExtra("IMDB_ID", clickedMovie.getImdbId());
                    intent.putExtra("year", clickedMovie.getReleaseYear());

                    System.out.println("listview movie name is: " + clickedMovie.getTitle());
                    System.out.println("listview movie imdbid is: " + clickedMovie.getImdbId());
                    System.out.println("listview movie year is: " + clickedMovie.getReleaseYear());

                    //check if the actors have common movies
                    if(clickedMovie.getTitle().equals("No common " +
                            "movies")) {
                        //display a toast if there are none
                        Context context =
                                getApplicationContext();
                        CharSequence text = "No common " +
                                "movies were found for " +
                                "the provided actors";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(
                                context, text, duration);
                        toast.show();
                    } else {
                        startActivity(intent);
                    }
                }
            });
        }
    }

    //get the filmography for a given celeb
    private void getFilmography(String celeb) {

        //initalize array list of movies for the celeb
        ArrayList<Movie> movies = new ArrayList<>();

        //pares the celeb name to remove spaces to be used in the query string
        final String searchName = celeb.replaceAll(" ", "%20");

        //create request to get actor imdb id
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                MY_API_MOVIES_URL + ACTOR_SEARCH_ENDPOINT + MY_API_MOVIES_KEY + actorQS +
                        searchName,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            JSONObject actor = jsonArray.getJSONObject(0);
                            String actorImdbID = actor.getString("imdbId");
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(celeb + " imdbId", actorImdbID);
                            editor.apply();
                            System.out.println(celeb + "'s ID is " + actorImdbID);

                            //create request to get filmography
                            JsonObjectRequest filmRequest = new JsonObjectRequest(
                                    Request.Method.GET,
                                    MY_API_MOVIES_URL + "/v1/name/" + actorImdbID +
                                            "/filmographies" + MY_API_MOVIES_KEY,
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response1) {
                                            try {
                                                JSONArray filmArray = response1.getJSONArray(
                                                        "data");
                                                System.out.println("trying to get filmography");
                                                System.out.println(response1.toString());
                                                String toCheck = "Actor";
                                                JSONArray filmography = new JSONArray();

                                                //get the filmography only for movies the actor
                                                //acted in
                                                for (int j = 0; j < filmArray.length(); j++) {
                                                    JSONObject actorSection = filmArray
                                                            .getJSONObject(j);

                                                    String fieldName = actorSection.getString(
                                                            "section");

                                                    if (fieldName.equals(toCheck) || fieldName.
                                                            equals("Actress")) {
                                                        filmography = actorSection.getJSONArray(
                                                                "filmographiesNames");
                                                        System.out.println(fieldName);
                                                    }
                                                }

                                                //initalize strings to be stored in shared
                                                // preferences as caching layer
                                                String filmNames = "";
                                                String filmIds = "";
                                                String filmYears = "";

                                                for (int j = 0; j < filmography.length(); j++) {
                                                    JSONObject film = filmography.getJSONObject(j);
                                                    //add film information to cache strings
                                                    String title = film.getString("title");
                                                    String filmImdbId = film.getString(
                                                            "imdbId");
                                                    String year;
                                                    if(film.has("year")) {
                                                        year = film.getString("year");
                                                    } else {
                                                        //for films without a year, store year as -1
                                                        year = "-1";
                                                    }

                                                    //insert separator character into cache strings
                                                    filmNames += title + "%20";
                                                    filmIds += filmImdbId + "%20";
                                                    filmYears += year + "%20";

                                                    //add retrieved data to movies array list by
                                                    //wrapping in it Movie object
                                                    movies.add(new Movie(title, filmImdbId, Integer
                                                            .parseInt(year)));
                                                }

                                                //add cache strings to shared preferences cache
                                                System.out.println("Storing celebs");
                                                SharedPreferences.Editor editor = sharedPref.edit();
                                                editor.putString(celeb + " Movie Titles",
                                                        filmNames);
                                                editor.putString(celeb + " Movie Ids", filmIds);
                                                editor.putString(celeb + " Movie Years", filmYears);
                                                editor.apply();

                                                //update sharedfilms with overlap of movies lists
                                                System.out.println("looking at films for " + celeb);
                                                System.out.println("update shared films");
                                                System.out.println(movies);
                                                if(sharedFilms.size() == 0) {
                                                    sharedFilms = movies;
                                                } else {
                                                    System.out.println("sharedFilms was");
                                                    System.out.println(sharedFilms);
                                                    sharedFilms.retainAll(movies);
                                                    if(sharedFilms.size() == 0) {
                                                        sharedFilms.add(new Movie(
                                                                "No common films", "",
                                                                -1));
                                                    }
                                                }
                                                System.out.println("new sharedfilms");
                                                if(sharedFilms.size() != 0) {
                                                    System.out.println(sharedFilms.get(0));
                                                } else {
                                                    System.out.println("no items in sharedfilms");
                                                }
                                                System.out.println(sharedFilms);
                                                sharedCounter++;

                                                //if all requests have resolved, finalize
                                                // sharedfilms and pass to list view
                                                if(sharedCounter == numFoundCelebs) {

                                                    sharedFilms = removeDuplicates(sharedFilms);

                                                    MovieListAdapter Displayadapter = new
                                                            MovieListAdapter(
                                                                    DisplayMoviesActivity
                                                                            .this, sharedFilms);

                                                    LV_display.setAdapter(Displayadapter);

                                                    LV_display.setOnItemClickListener(
                                                            new AdapterView.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(AdapterView<?>
                                                                                        parent, View
                                                                                        view, int
                                                                                        position,
                                                                                long id) {
                                                            String nameOfMovie;

                                                            //Parent refers to the parent of the
                                                            // item, the ListView.  position is the
                                                            // index of the item clicked.
                                                            nameOfMovie = String.valueOf(parent.
                                                                    getItemAtPosition(position));

                                                            // pass the name of the movie to next
                                                            // activity and put the name of selected
                                                            // movie
                                                            Intent intent = new Intent(
                                                                    getBaseContext(), MainActivity
                                                                    .class);
                                                            intent.putExtra("Movie_Name",
                                                                    nameOfMovie);
                                                            Movie clickedMovie = sharedFilms.get(
                                                                    position);
                                                            intent.putExtra("IMDB_ID",
                                                                    clickedMovie.getImdbId());
                                                        }
                                                    });
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.d("ERROR", "error => " + error
                                                    .toString());
                                        }
                                    }
                            );

                            queue.add(filmRequest);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        );

        queue.add(objectRequest);
    }


    //store imdb id of celebrity in cache
    public void storeCelebImdbId(String celeb) {
        //create search name
        final String searchName = celeb.replaceAll(" ", "%20");

        //create request to get imdb id
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                MY_API_MOVIES_URL + ACTOR_SEARCH_ENDPOINT + MY_API_MOVIES_KEY + actorQS +
                        searchName,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            JSONObject actor = jsonArray.getJSONObject(0);
                            String actorImdbID = actor.getString("imdbId");
                            System.out.println(celeb + "'s ID is " + actorImdbID);
                            celebImdbIds.put(celeb, actorImdbID);
                            System.out.println("Storing celeb imdbid");

                            //store imdb id
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(celeb + " imdbId", actorImdbID);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        );

        queue.add(objectRequest);
    }

    //remove duplicate movies from list
    public ArrayList<Movie> removeDuplicates(ArrayList<Movie> list) {
        //initialize hash map mapping titles to movies
        HashMap<String, Movie> moviesMap = new HashMap<>();
        //initialize duplicate-free movie list
        ArrayList<Movie> noDups = new ArrayList<>();

        //for each movie, add it if it is not a duplicate
        for  (Movie movie : list) {
            if(!moviesMap.containsKey(movie.getTitle())) {
                moviesMap.put(movie.getTitle(), movie);
                noDups.add(movie);
            }
        }

        //return the duplicate-free movie list
        return noDups;
    }

}
