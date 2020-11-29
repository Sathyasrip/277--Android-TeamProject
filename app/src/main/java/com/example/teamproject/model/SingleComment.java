package com.example.teamproject.model;

import java.text.SimpleDateFormat;
import java.util.Date;

// Used for our Custom ListView Adapter to display comments loaded from the saved comments JSON file.
public class SingleComment {
    String comment_number, full_name, username, details, creation_date;
    String annotation_id;

    public SingleComment(String comment_number, String full_name, String username, String details, String creation_date) {
        this.comment_number = comment_number;
        this.full_name = full_name;
        this.username = username;
        this.details = details;
        this.creation_date = creation_date;
    }
    // "1" as the comment_number
    public String CommentNumber() {
        return comment_number;
    }

    // "John Doe" as full_name.
    public String FullName() {
        return full_name;
    }

    // "johndoe" as username.
    public String Username() {
        return username;
    }

    // "This is an example comment" as details.
    public String Comment() {
        return details;
    }

    // "11/02/2020 14:56" as creation_date.
    public String CreationDate() {
        return creation_date;
    }

    // "1123ed38756d4a6e8a34eb14f53e782a"
    public String AnnotationID() {
        return annotation_id;
    }

    /******************************************************
     *  The functions below are used for Firebase uploads
     *******************************************************/
    public void setComment(String details) {
        this.details = details;
    }
    public void setAnnotationID(String annotation_id) { this.annotation_id = annotation_id; }
    public void setCreation_date() {
        Date date = new Date();
        SimpleDateFormat DateFor = new SimpleDateFormat("MM/dd/yyyy H:m");
        this.creation_date = DateFor.format(date);
    }
}
