package com.example.teamproject.model;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FirebaseUserProfile {
    private String account_type;
    private String credentials;
    private String email;
    private String full_name;
    private String picture;
    private String username;
    private long theme_id;

    public FirebaseUserProfile() {};

    public FirebaseUserProfile(String account_type, String credentials, String email,
                               String full_name, String picture, String username,
                               long theme_id) {
        this.account_type = account_type;
        this.credentials = credentials;
        this.email = email;
        this.full_name = full_name;
        this.picture = picture;
        this.username = username;
        this.theme_id = theme_id;
    }
}