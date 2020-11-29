package com.example.teamproject.model;

import java.io.Serializable;
import java.util.List;

// Stores information about the Firebase Review Version.
public class FirebaseReviewVersion implements Serializable {
    String version_number, pdf_file, annotation_file;
    List<SingleComment> comments;

    public FirebaseReviewVersion() {};
    public FirebaseReviewVersion(String version_number, String pdf_file) {
        this.version_number = version_number;
        this.pdf_file = pdf_file;
    }
    public String Version() {
        return this.version_number;
    }
    public void setAnnotationFile(String annotation_file) {
        this.annotation_file = annotation_file;
    }
    /******************************************************************
     *  The functions below are used for StartTheReview activity only.
     ******************************************************************/
    public String PDF() {
        String firebase_pdf_location = "/pdfs/" + this.pdf_file;
        return firebase_pdf_location;
    }
    public String AnnotationFile() {
        String firebase_annotations_location = "/annotations/" + this.annotation_file;
        return firebase_annotations_location;
    }
    public void addComment(SingleComment comment) {
        comments.add(comment);
    }
    public SingleComment getComment(int index) {
        return this.comments.get(index);
    }
    public List<SingleComment> getAllComments() {
        return this.comments;
    }

    public String GetFirebasePublicUrl(String encoded_firebase_path) {
        String firebase_public_header = "https://firebasestorage.googleapis.com";
        String firebase_access_footer = "?alt=media";
        return firebase_public_header + encoded_firebase_path + firebase_access_footer;
    }
}
