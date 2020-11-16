package com.example.teamproject.model;

import java.util.ArrayList;

// Used to Update the NoSQL Firebase entry for the given version.
public class UploadReview {
    String owner;
    String review_id;
    ArrayList<SingleComment> comments = new ArrayList<SingleComment>();

    public UploadReview(String owner, String review_id, ArrayList<SingleComment> comments) {
        this.owner = owner;
        this.review_id = review_id;
        this.comments = comments;
    }

    public String getOwner() {
        // Current owner of the Review. May be used to modify some entry in an NoSQL Database.
        return owner;
    }

    public String getReviewID() {
        // The current 'Review' has all its files listed under UUID_v#.
        // UUID = UUID of title, v# = Version #. Example: abcdajkjah123kh_v1.
        return review_id;
    }

    public String getCommentsDatabase(String review_id) {
        // This is the NoSQL database that will store the comments for the current Review version.
        String comments_database = "comments_" + review_id;
        return comments_database;
    }

    public String getAnnotationsFilename(String review_id) {
        // Source: https://www.pdftron.com/documentation/web/guides/annotation/import-export/files/
        // This is the NoSQL database that will store the comments for the current Review version.
        String annotations_filename = review_id + ".xfdf";
        return annotations_filename;
    }

    public ArrayList<SingleComment> getComments() {
        // These comments will be used to update entries in the database given by
        // getCommentsDatabase;
        return comments;
    }
}
