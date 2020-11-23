package com.example.teamproject.model;

import java.util.ArrayList;

// Used to pull Review versions from firebase.
public class FirebaseReview {
    private String review_uuid, owner, review_title;
    private long latest_version;
    private Boolean viewable, read_only;
    private ArrayList<String> num_versions = new ArrayList<String>();
    private ArrayList<FirebaseReviewVersion> versions = new ArrayList<FirebaseReviewVersion>();

    public FirebaseReview(String review_uuid, String owner, long latest_version, String review_title, Boolean read_only, Boolean viewable) {
        this.review_uuid = review_uuid;
        this.owner = owner;
        this.latest_version = latest_version;
        this.review_title = review_title;
        this.read_only = read_only;
        this.viewable = viewable;
    }

    /******************************************************
     *  The functions below are used for the dropdown list
     *  in UserPortal & DeletingReview activities.
     *******************************************************/
    public String UUID() { return this.review_uuid; }
    public String Owner() { return this.owner; }
    public long LatestVersion() { return this.latest_version; }
    public String ReviewTitle() { return this.review_title; }
    public Boolean Viewable() { return this.viewable; }
    public Boolean ReadOnly() { return this.read_only; }
    public void addVersionNumber(String num_version) {
        this.num_versions.add(num_version);
    }
    public ArrayList<String> getVersionNumbers() {
        // Returns all the version numbers.
        return this.num_versions;
    }

    // TODO: Do we really need this? Delete if not.
    public String getVersionNumber(int index) {
        // Returns the Version # at the given index.
        return this.num_versions.get(index);
    }

    public void addVersion(FirebaseReviewVersion version) {
        this.versions.add(version);
    }
    public FirebaseReviewVersion getVersion(int index) {
        return this.versions.get(index);
    }
    public ArrayList<FirebaseReviewVersion> getAllVersions() {
        return this.versions;
    }
}
