package com.example.eyemax;

/*
 * Class for storing details about a given movie.
 * Contains movie's title, imdb id, and release year.
 */

public class Movie {

    //object fields
    private String title;
    private String imdbId;
    private int releaseYear;

    //Constructor
    public Movie(String title, String imdbId, int releaseYear) {
        this.title = title;
        this.imdbId = imdbId;
        this.releaseYear = releaseYear;
    }

    //for parsing to a string
    public String toString(){
        return this.title;
    }

    //perform equality using move title
    public boolean equals(Object object){
        if(object == this){
            return true;
        }

        if (!(object instanceof Movie)) {
            return false;
        }

        Movie m = (Movie) object;

        return m.title.equals(this.title);
    }

    //getters
    public String getTitle(){
        return this.title;
    }

    public String getImdbId(){
        return this.imdbId;
    }

    public int getReleaseYear() {
        return this.releaseYear;
    }
}
