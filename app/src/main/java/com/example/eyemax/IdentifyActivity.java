package com.example.eyemax;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesRequest;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/*
 * Activity to identify the celebrities in an image
 */

public class IdentifyActivity extends AppCompatActivity {

    //declare api string constants
    private String AWS_ACCESS_KEY;
    private String AWS_SECRET_KEY;

    //declare image
    private Bitmap image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        //setup and inflate the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //set toolbar text to white
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.backgroundColor));
        setSupportActionBar(toolbar);

        //declare array list containing names of found celebrities
        ArrayList<String> foundCelebs;

        //get api keys from config
        try {
            AWS_ACCESS_KEY = javaant.com.propertiesfile.Util.getProperty("AWS_ACCESS_KEY",
                    getApplicationContext());
            AWS_SECRET_KEY = javaant.com.propertiesfile.Util.getProperty("AWS_SECRET_KEY",
                    getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //check if getting celebrities from an image or from a name
        if(getIntent().hasExtra("searchedActor")) {
            //if we were passed a search name, we assign as the only element of foundCelebs
            foundCelebs = new ArrayList<>();
            String actorQuery = getIntent().getStringExtra("searchedActor");
            String[] actors = actorQuery.split(", ");
            for(String actor: actors){
                foundCelebs.add(actor);
            }
        } else {
            //if we were passed an image, we decode it and call getCelebrities to identify
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            foundCelebs = getCelebrities();
        }

        //initialize edittext for adding additional actors
        final EditText addText = (EditText)findViewById(R.id.addText);

        //instantiate custom adapter
        final NamesListAdapter adapter = new NamesListAdapter(foundCelebs, this);

        //handle listview and assign adapter
        ListView lView = (ListView) findViewById(R.id.listView);
        lView.setAdapter(adapter);

        Button addButton = findViewById(R.id.addBtn);

        addButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //when add button is clicked, add the actor in add text to the list
                foundCelebs.add(addText.getText().toString());
                adapter.notifyDataSetChanged();
            }
        });

        Button buttonNext = findViewById(R.id.buttonNext);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when button next is clicked, ensure that foundCelebs has content then pass it to
                //displayMovies to generate the next intent
                if(foundCelebs.size() > 0) {
                    displayMovies(v, foundCelebs);
                } else {
                    //if foundCelebs is empty, create a toast asking the user to add actors
                    Context context = getApplicationContext();
                    CharSequence text = "Please add actors before proceeding";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    //handle when the user selects a toolbar item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                settings();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //create intent for SettingsActivity and start it
    public void settings() {
        Intent settings = new Intent(this, SettingsActivity.class);

        startActivity(settings);
    }

    //get celebrities from AWS Rekognition
    public ArrayList<String> getCelebrities(){

        //initialize Rekognition client using AWS credentials
        AmazonRekognition rekognitionClient = new AmazonRekognitionClient(new BasicAWSCredentials(
                AWS_ACCESS_KEY, AWS_SECRET_KEY));

        //encode image bitmap as bytearray
        Bitmap bmap = image;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] b = stream.toByteArray();
        ByteBuffer imageBytes = ByteBuffer.wrap(b);

        //wrap image bytes in Rekognition Image object
        Image i = new Image().withBytes(imageBytes);

        //create request to retrieve celebrity identities
        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(i);

        //initialize recognize result
        RecognizeCelebritiesResult recognizeCelebritiesResult = null;

        //set thread policy to allow network requests
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        //get result
        try {
            recognizeCelebritiesResult = rekognitionClient.recognizeCelebrities(request);
        } catch (Exception e) {
            System.out.println("An exception has occurred");
            System.out.println(e.getMessage());
        }

        //retrieve list of identified celebrities
        List<Celebrity> celebDetails = recognizeCelebritiesResult.getCelebrityFaces();

        //get match sensitivity preference from shared preferences
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        int matchSensitivity = sharedPreferences.getInt("Match Sensitivity", 70);

        System.out.println("Identified the following celebrities: ");

        //iterate through celebDetails list
        ArrayList<String> foundCelebs = new ArrayList<>();
        for (Celebrity celeb: celebDetails){
            System.out.println(celeb.getName());
            System.out.println(celeb.getMatchConfidence());

            //prune results with match confidence less than match sensitivity
            if(celeb.getMatchConfidence() < matchSensitivity) {
                System.out.println("Low match confidence, consider discarding");
            } else {
                //if the result has high enough confidence add it to foundCelebs
                foundCelebs.add(celeb.getName());
            }
        }
        return foundCelebs;
    }

    //create intent for DisplayMoviesActivity and start it
    public void displayMovies(View v, ArrayList<String> foundCelebs) {
        Intent displayMovie = new Intent(this, DisplayMoviesActivity.class);

        //put foundCelebs in intent
        displayMovie.putExtra("foundCelebs", foundCelebs);
        startActivity(displayMovie);
    }
}