package com.example.teamproject;

// Used for our Custom ListView Adapter to display comments loaded from the saved comments JSON file.
public class SingleComment {
    String full_name, username, details, creation_date;

    public SingleComment(String full_name, String username, String details, String creation_date) {
        this.full_name = full_name;
        this.username = username;
        this.details = details;
        this.creation_date = creation_date;
    }

    // "John Doe" as full_name.
    public String getFullName() {
        return full_name;
    }

    // "johndoe" as username.
    public String getUsername() {
        return username;
    }

    // "This is an example comment" as details.
    public String getComment() {
        return details;
    }

    // "11/02/2020 14:56" as creation_date.
    public String getCreationDate() {
        return creation_date;
    }
}
