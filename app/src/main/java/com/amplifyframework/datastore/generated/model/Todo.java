package com.amplifyframework.datastore.generated.model;


import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Todo type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Todos")
public final class Todo implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField ACTOR_NAME = field("actor_name");
  public static final QueryField IMDB_ID = field("imdbId");
  public static final QueryField FILMOGRAPHY = field("filmography");
  public static final QueryField FILMOGRAPHY_IDS = field("filmographyIds");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String actor_name;
  private final @ModelField(targetType="String", isRequired = true) String imdbId;
  private final @ModelField(targetType="String", isRequired = true) List<String> filmography;
  private final @ModelField(targetType="String", isRequired = true) List<String> filmographyIds;
  public String getId() {
      return id;
  }
  
  public String getActorName() {
      return actor_name;
  }
  
  public String getImdbId() {
      return imdbId;
  }
  
  public List<String> getFilmography() {
      return filmography;
  }
  
  public List<String> getFilmographyIds() {
      return filmographyIds;
  }
  
  private Todo(String id, String actor_name, String imdbId, List<String> filmography, List<String> filmographyIds) {
    this.id = id;
    this.actor_name = actor_name;
    this.imdbId = imdbId;
    this.filmography = filmography;
    this.filmographyIds = filmographyIds;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Todo todo = (Todo) obj;
      return ObjectsCompat.equals(getId(), todo.getId()) &&
              ObjectsCompat.equals(getActorName(), todo.getActorName()) &&
              ObjectsCompat.equals(getImdbId(), todo.getImdbId()) &&
              ObjectsCompat.equals(getFilmography(), todo.getFilmography()) &&
              ObjectsCompat.equals(getFilmographyIds(), todo.getFilmographyIds());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getActorName())
      .append(getImdbId())
      .append(getFilmography())
      .append(getFilmographyIds())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Todo {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("actor_name=" + String.valueOf(getActorName()) + ", ")
      .append("imdbId=" + String.valueOf(getImdbId()) + ", ")
      .append("filmography=" + String.valueOf(getFilmography()) + ", ")
      .append("filmographyIds=" + String.valueOf(getFilmographyIds()))
      .append("}")
      .toString();
  }
  
  public static ActorNameStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Todo justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Todo(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      actor_name,
      imdbId,
      filmography,
      filmographyIds);
  }
  public interface ActorNameStep {
    ImdbIdStep actorName(String actorName);
  }
  

  public interface ImdbIdStep {
    FilmographyStep imdbId(String imdbId);
  }
  

  public interface FilmographyStep {
    FilmographyIdsStep filmography(List<String> filmography);
  }
  

  public interface FilmographyIdsStep {
    BuildStep filmographyIds(List<String> filmographyIds);
  }
  

  public interface BuildStep {
    Todo build();
    BuildStep id(String id) throws IllegalArgumentException;
  }
  

  public static class Builder implements ActorNameStep, ImdbIdStep, FilmographyStep, FilmographyIdsStep, BuildStep {
    private String id;
    private String actor_name;
    private String imdbId;
    private List<String> filmography;
    private List<String> filmographyIds;
    @Override
     public Todo build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Todo(
          id,
          actor_name,
          imdbId,
          filmography,
          filmographyIds);
    }
    
    @Override
     public ImdbIdStep actorName(String actorName) {
        Objects.requireNonNull(actorName);
        this.actor_name = actorName;
        return this;
    }
    
    @Override
     public FilmographyStep imdbId(String imdbId) {
        Objects.requireNonNull(imdbId);
        this.imdbId = imdbId;
        return this;
    }
    
    @Override
     public FilmographyIdsStep filmography(List<String> filmography) {
        Objects.requireNonNull(filmography);
        this.filmography = filmography;
        return this;
    }
    
    @Override
     public BuildStep filmographyIds(List<String> filmographyIds) {
        Objects.requireNonNull(filmographyIds);
        this.filmographyIds = filmographyIds;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String actorName, String imdbId, List<String> filmography, List<String> filmographyIds) {
      super.id(id);
      super.actorName(actorName)
        .imdbId(imdbId)
        .filmography(filmography)
        .filmographyIds(filmographyIds);
    }
    
    @Override
     public CopyOfBuilder actorName(String actorName) {
      return (CopyOfBuilder) super.actorName(actorName);
    }
    
    @Override
     public CopyOfBuilder imdbId(String imdbId) {
      return (CopyOfBuilder) super.imdbId(imdbId);
    }
    
    @Override
     public CopyOfBuilder filmography(List<String> filmography) {
      return (CopyOfBuilder) super.filmography(filmography);
    }
    
    @Override
     public CopyOfBuilder filmographyIds(List<String> filmographyIds) {
      return (CopyOfBuilder) super.filmographyIds(filmographyIds);
    }
  }
  
}
