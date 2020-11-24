package com.example.eyemax;

/*
 * Class for storing details about a given actor for a given movie.
 * Contains actor's name, the character they play in the given movie, and the URL to get their
 * photo from imdB.
 */

public class Actor {

    //object fields
    private String name;
    private String character;
    private String photoUrl;

    //Constructor
    public Actor(String name, String character, String photoUrl) {
        this.name = name;
        this.character = character;
        this.photoUrl = photoUrl;
    }

    //getters
    public String getName() {
        return this.name;
    }

    public String getCharacter() {
        return this.character;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public String toString(){
        return this.name;
    }

    //equality implementation, compares titles
    public boolean equals(Object object){
        if(object == this){
            return true;
        }

        if (!(object instanceof Actor)) {
            return false;
        }

        Actor m = (Actor) object;

        return m.name.equals(this.name);
    }

}
