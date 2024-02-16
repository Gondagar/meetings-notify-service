package com.serfer.notify.tranfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public final class Meeting {
    public  String title;
    public  String description;
    public  LocalDateTime startTime;
    public  ArrayList<Participant> participants;
    public  Owner owner;

    @JsonIgnore
    private static final String DATE_FORMATTER= "yyyy-MM-ddTHH:mm:ss"; //2020-06-03T16:10:43.176

    @JsonCreator
    public Meeting(@JsonProperty("title") String title, @JsonProperty("description") String description, @JsonProperty("startTime") String startTime, @JsonProperty("participants") ArrayList<Participant> participants, @JsonProperty("owner") Owner owner){
        this.title = title;
        this.description = description;
        this.startTime = LocalDateTime.parse(startTime);
        this.participants = participants;
        this.owner = owner;
    }

    @Data
    @NoArgsConstructor
    public static final class Participant {
        public  String userFirstName;
        public  String userLastName;
        public  String userEmail;
        public  boolean isOwner;

        @JsonCreator
        public Participant(@JsonProperty("userFirstName") String userFirstName, @JsonProperty("userLastName") String userLastName, @JsonProperty("userEmail") String userEmail, @JsonProperty("isOwner") boolean isOwner){
            this.userFirstName = userFirstName;
            this.userLastName = userLastName;
            this.userEmail = userEmail;
            this.isOwner = isOwner;
        }
    }

    @Data
    @NoArgsConstructor
    public static final class Owner {
        public  String userFirstName;
        public  String userLastName;
        public  String userEmail;
        public  boolean isOwner;

        @JsonCreator
        public Owner(@JsonProperty("userFirstName") String userFirstName, @JsonProperty("userLastName") String userLastName, @JsonProperty("userEmail") String userEmail, @JsonProperty("isOwner") boolean isOwner){
            this.userFirstName = userFirstName;
            this.userLastName = userLastName;
            this.userEmail = userEmail;
            this.isOwner = isOwner;
        }
    }
}