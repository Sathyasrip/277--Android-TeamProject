package com.example.teamproject.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.Serializable;

// Used to Store information about the Current User Profile
public class ProfileSettings implements Serializable {
    private String firebase_auth_uuid, firebase_profile_picture_name, existing_firebase_profile_picture_name;
    private String full_name, email, username, credentials, account_type, new_picture_location = "";
    private int theme_id = 0;

    public ProfileSettings() {};

    public ProfileSettings(String firebase_auth_uuid, String firebase_profile_picture_name,
                           String full_name, String email, String username, String credentials,
                           String account_type, int theme_id) {
        this.existing_firebase_profile_picture_name = firebase_profile_picture_name;
        this.firebase_profile_picture_name = "";  // To be filled in UserProfile.java.
        this.firebase_auth_uuid = firebase_auth_uuid;
        this.full_name = full_name;
        this.email = email;
        this.username = username;
        this.credentials = credentials;
        this.account_type = account_type;
        this.theme_id = theme_id;
    }

    public String UUID() {
        return firebase_auth_uuid;
    }

    public String FullName() {
        return full_name;
    }

    public String Email() {
        return email;
    }

    public String Username() {
        return username;
    }

    public String Credentials() {
        return credentials;
    }

    // Account Type can only be changed by Moderators.
    public String AccountType() {
        return account_type;
    }

    public int Theme() {
        // Returns an index number which will select the theme.
        return theme_id;
    }

    public String GetFirebaseProfilePicture() {
        String firebase_profile_picture = "/profile_pictures/" + this.firebase_profile_picture_name;
        return firebase_profile_picture;
    }

    public String GetExistingFirebaseProfilePicture() {
        String firebase_profile_picture = "/profile_pictures/" + this.existing_firebase_profile_picture_name;
        return firebase_profile_picture;
    }

    public String GetLocalProfilePicture() {
        return this.new_picture_location;
    }

    public String GetFirebaseProfilePictureName() {
        return this.firebase_profile_picture_name;
    }

    public void SetFullName(String full_name) {
        this.full_name = full_name;
    }

    public void SetCredentials(String credentials) {
        this.credentials = credentials;
    }

    public void SetAccountType(String account_type) {
        // Currently can only delegate Reviewer or Standard account type.
        if (account_type.equals("reviewer") || account_type.equals("standard")) {
            this.account_type = account_type;
        }
    }

    public void SetTheme(int theme_id) {
        this.theme_id = theme_id;
    }

    public void SetFirebaseProfilePictureNameUri(Context context, Uri photo) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = mime.getExtensionFromMimeType(cR.getType(photo));

        this.firebase_profile_picture_name = this.firebase_auth_uuid + "." + extension;
    }

    public void SetFirebaseProfilePictureName(String new_picture_location) {
        String[] segments = new_picture_location.split("[.]");
        this.firebase_profile_picture_name = this.firebase_auth_uuid + "." + segments[segments.length -1];
    }

    public void UploadNewProfilePicture(String new_picture_location) {
        // By default, we expect this to be empty. This is the file to replace profile picture.
        this.new_picture_location = new_picture_location;
        SetFirebaseProfilePictureName(this.new_picture_location);
    }

    public String GetFirebasePublicUrl(String encoded_firebase_path) {
        String firebase_public_header = "https://firebasestorage.googleapis.com";
        String firebase_access_footer = "?alt=media";
        return firebase_public_header + encoded_firebase_path + firebase_access_footer;
    }
}