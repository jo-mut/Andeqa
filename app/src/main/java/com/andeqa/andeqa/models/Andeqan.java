package com.andeqa.andeqa.models;

/**
 * Created by J.EL on 5/4/2017.
 */

public class Andeqan {
    String profile_image;
    String profile_cover;
    String bio;
    String username;
    String user_id;
    String email;
    String first_name;
    String second_name;
    String device_id;

    public Andeqan(){
        //EMPTY CONSTRUCTOR REQUIRED
    }


    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getProfile_cover() {
        return profile_cover;
    }

    public void setProfile_cover(String profile_cover) {
        this.profile_cover = profile_cover;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getSecond_name() {
        return second_name;
    }

    public void setSecond_name(String second_name) {
        this.second_name = second_name;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }
}
