package com.example.teamproject.model;

import java.util.ArrayList;

// Used to Update the NoSQL Firebase entry for the given version.
public class UploadReview {
    private String firebase_review_uuid, owner, title, current_version, latest_version;
    private String annotations_file, pdf_file;
    private ArrayList<SingleComment> comments = new ArrayList<SingleComment>();

    public UploadReview(String firebase_review_uuid, String owner, String title,
                        String latest_version, String current_version, String pdf_file) {
        this.firebase_review_uuid = firebase_review_uuid;
        this.owner = owner;
        this.title = title;
        this.latest_version = latest_version;
        this.current_version = current_version;
        this.pdf_file = pdf_file;
    }

    public String ReviewUUID() {
        return firebase_review_uuid;
    }

    public String Owner() {
        return owner;
    }

    public String ReviewTitle() {
        return title;
    }

    public String LatestVersion() {
        return latest_version;
    }

    public String CurrentVersion() {
        return latest_version;
    }

    public ArrayList<SingleComment> Comments() {
        // Used to store all comments made by the current user.
        return comments;
    }

    public String FirebasePDFFile() {
        String pdf_filename = "gs://academiabeta-f8813.appspot.com/pdfs/" + this.firebase_review_uuid + "_v" + current_version + ".pdf";
        return pdf_filename;
    }

    public String FirebaseAnnotationsFile() {
        // Source: https://www.pdftron.com/documentation/web/guides/annotation/import-export/files/
        String annotations_filename = "gs://academiabeta-f8813.appspot.com/annotations/" + this.firebase_review_uuid + "_v" + current_version + ".xfdf";
        return annotations_filename;
    }

    public void LocalAnnotationsFile(String annotations_file) {
        this.annotations_file = annotations_file;
    }

    public void SetToLatestVersion() {
        this.latest_version = this.current_version;
    }

    public void SetComments(ArrayList<SingleComment> comments) {
        this.comments = new ArrayList<SingleComment>();
        this.comments = comments;
    }

    public void SetLocalAnnotationsFile(String annotations_file) {
        this.annotations_file = annotations_file;
    }

    public void SetPDFFile(String pdf_file) {
        this.pdf_file = pdf_file;
    }
}
