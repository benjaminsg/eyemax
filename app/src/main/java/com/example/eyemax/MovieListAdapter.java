package com.example.eyemax;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/*
 * Custom list adapter for displaying flexible list view with movie information
 */

public class MovieListAdapter extends ArrayAdapter<Movie>{

    //Constructor
    public MovieListAdapter(Context context, ArrayList<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_movie, parent,
                    false);
        }

        // Get the data item for this position
        Movie currentmovie = getItem(position);

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_Moviename);
        TextView tvdate = (TextView) convertView.findViewById(R.id.tv_Moviedesc);
        // Populate the data into the template view using the data object
        tvName.setText(currentmovie.getTitle());
        String releaseYear = Integer.toString(currentmovie.getReleaseYear());
        if(!releaseYear.equals("-1")) {
            tvdate.setText(Integer.toString(currentmovie.getReleaseYear()));
        } else {
            tvdate.setText("");
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
